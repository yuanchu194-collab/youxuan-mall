package com.youxuan.file.service;

import com.youxuan.file.vo.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    FileUploadVO uploadProductImage(MultipartFile file);

    void deleteProductImage(String objectName);
}
