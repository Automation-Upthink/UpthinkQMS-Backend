package com.upthink.qms.service.request;

public class UpdateEssayClientCredRequest extends CreateEssayClientCredRequest{

    private final int clientId;

    public UpdateEssayClientCredRequest(TokenValidation id,
                                        String name,
                                        String clientName,
                                        String password,
                                        int downloadLimit,
                                        int clientId) {
        super(id, name, clientName, password, downloadLimit);
        this.clientId = clientId;
    }

    public int getClientId() {
        return clientId;
    }
}
