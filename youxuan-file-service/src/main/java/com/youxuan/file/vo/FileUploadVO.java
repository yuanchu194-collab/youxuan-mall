package com.youxuan.file.vo;

/**
 * 文件上传结果。
 */
public class FileUploadVO {

    private String url;
    private String bucket;
    private String objectName;

    public FileUploadVO() {
    }

    public FileUploadVO(String url, String bucket, String objectName) {
        this.url = url;
        this.bucket = bucket;
        this.objectName = objectName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }
}
