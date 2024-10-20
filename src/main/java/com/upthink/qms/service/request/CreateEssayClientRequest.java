package com.upthink.qms.service.request;

public class CreateEssayClientRequest extends AuthenticatedRequest{

    private final String name;
    private final int downloadCap;



    public CreateEssayClientRequest(TokenValidation id, String name, int downloadCap) {
        super(id);
        this.name = name;
        this.downloadCap = downloadCap;
    }

    public String getName() {
        return name;
    }

    public int getDownloadCap() {
        return downloadCap;
    }
}
