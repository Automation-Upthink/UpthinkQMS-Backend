package com.upthink.qms.service.request;

public class UpdateEssayClientRequest extends CreateEssayClientRequest{

    private final int clientId;

    public UpdateEssayClientRequest(TokenValidation id, int clientId, String name, int downloadCap) {
        super(id, name, downloadCap);
        this.clientId = clientId;
    }

    public int getClientId() {
        return clientId;
    }
}
