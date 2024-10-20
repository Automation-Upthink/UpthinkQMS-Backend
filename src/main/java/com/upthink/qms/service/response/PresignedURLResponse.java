package com.upthink.qms.service.response;

import gson.GsonDTO;

public class PresignedURLResponse extends GsonDTO {

    private final String url;
    private final String uuid;

    private final String fileStatus;
    private final String fileUploadError;

    private final String assetName;

    public PresignedURLResponse(String url, String uuid, String assetName) {
        this(url, uuid, null, null, assetName);
    }

    public PresignedURLResponse(
            String url, String uuid, String fileStatus, String fileUploadError, String assetName) {
        this.url = url;
        this.uuid = uuid;
        this.fileStatus = fileStatus;
        this.fileUploadError = fileUploadError;
        this.assetName = assetName;
    }

    public String getUrl() {
        return url;
    }
}