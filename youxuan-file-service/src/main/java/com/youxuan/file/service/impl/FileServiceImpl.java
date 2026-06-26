package com.youxuan.file.service.impl;

import com.youxuan.common.exception.BusinessException;
import com.youxuan.common.result.ErrorCode;
import com.youxuan.file.config.MinioProperties;
import com.youxuan.file.service.FileService;
import com.youxuan.file.vo.FileUploadVO;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 商品图片上传到 MinIO，不落本地磁盘，不保存 base64。
 */
@Service
public class FileServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);
    private static final long MAX_FILE_SIZE = 5L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final MinioClient minioClient;
    private final MinioProperties properties;

    public FileServiceImpl(MinioClient minioClient, MinioProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    @PostConstruct
    public void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(properties.getBucket())
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(properties.getBucket())
                        .build());
                log.info("MinIO bucket 创建成功 bucket={}", properties.getBucket());
            }
            setPublicReadPolicy();
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 MinIO bucket 失败：" + exception.getMessage());
        }
    }

    @Override
    public FileUploadVO uploadProductImage(MultipartFile file) {
        validateImage(file);
        String extension = getExtension(file.getOriginalFilename());
        String objectName = buildObjectName(extension);
        String contentType = StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream";

        try (InputStream inputStream = file.getInputStream()) {
            // 核心上传逻辑：对象直接写入 MinIO，商品表只保存返回的 URL。
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(contentType)
                    .build());
            String url = buildFileUrl(objectName);
            log.info("商品图片上传成功 bucket={}, objectName={}", properties.getBucket(), objectName);
            return new FileUploadVO(url, properties.getBucket(), objectName);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "上传商品图片失败：" + exception.getMessage());
        }
    }

    @Override
    public void deleteProductImage(String objectName) {
        if (!StringUtils.hasText(objectName)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "objectName不能为空");
        }
        if (objectName.startsWith("http://") || objectName.startsWith("https://")) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "objectName应为MinIO对象名，不是完整URL");
        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectName)
                    .build());
            log.info("商品图片删除成功 bucket={}, objectName={}", properties.getBucket(), objectName);
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "删除商品图片失败：" + exception.getMessage());
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "商品图片不能超过5MB");
        }
        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "商品图片只支持jpg、jpeg、png、webp格式");
        }
    }

    private String getExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件名缺少扩展名");
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private String buildObjectName(String extension) {
        LocalDate now = LocalDate.now();
        return String.format("products/%d/%02d/%s.%s",
                now.getYear(), now.getMonthValue(), UUID.randomUUID(), extension);
    }

    private String buildFileUrl(String objectName) {
        String publicEndpoint = StringUtils.hasText(properties.getPublicEndpoint())
                ? properties.getPublicEndpoint()
                : properties.getEndpoint();
        return trimTrailingSlash(publicEndpoint) + "/" + properties.getBucket() + "/" + objectName;
    }

    private String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private void setPublicReadPolicy() {
        String policy = """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": {"AWS": ["*"]},
                      "Action": ["s3:GetObject"],
                      "Resource": ["arn:aws:s3:::%s/*"]
                    }
                  ]
                }
                """.formatted(properties.getBucket());
        try {
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(properties.getBucket())
                    .config(policy)
                    .build());
            log.info("MinIO bucket 公开读策略设置成功 bucket={}", properties.getBucket());
        } catch (Exception exception) {
            log.warn("MinIO bucket 公开读策略设置失败，上传接口仍可用 bucket={}, error={}",
                    properties.getBucket(), exception.getMessage());
        }
    }
}
