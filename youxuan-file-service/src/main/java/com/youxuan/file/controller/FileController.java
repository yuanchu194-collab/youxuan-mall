package com.youxuan.file.controller;

import com.youxuan.common.result.Result;
import com.youxuan.file.service.FileService;
import com.youxuan.file.vo.FileUploadVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 商品图片接口，统一由 Gateway 的 /api/file/** 访问。
 */
@RestController
@RequestMapping("/product")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/image")
    public Result<FileUploadVO> uploadProductImage(@RequestParam("file") MultipartFile file) {
        return Result.success(fileService.uploadProductImage(file));
    }

    @DeleteMapping("/image")
    public Result<Void> deleteProductImage(@RequestParam("objectName") String objectName) {
        fileService.deleteProductImage(objectName);
        return Result.success();
    }
}
