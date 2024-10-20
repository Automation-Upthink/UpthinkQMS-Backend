package com.upthink.qms.service.request;

import gson.GsonDTO;

public class EssayPresignedURLRequest extends GsonDTO {

    public enum FileType {
        file,
        screenshot
    }

    public enum FileAction {
        GET,
        PUT
    }

    private final String fileId, fileName;

    private final FileType fileType;

    private final FileAction fileAction;

    public EssayPresignedURLRequest(
            String fileId, String fileName, FileType fileType, FileAction fileAction) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileAction = fileAction;
    }

    public EssayPresignedURLRequest(String fileId, String fileName, FileType fileType) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileAction = FileAction.PUT;
    }

    public String getFileId() {
        return fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public FileType getFileType() {
        return fileType;
    }

    public FileAction getFileAction() {
        return fileAction;
    }
}