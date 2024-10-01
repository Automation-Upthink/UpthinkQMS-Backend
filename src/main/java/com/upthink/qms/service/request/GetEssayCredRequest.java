package com.upthink.qms.service.request;

import gson.GsonDTO;

public class GetEssayCredRequest extends GsonDTO {

    private final String clientName;

    public GetEssayCredRequest(String clientName) {
        this.clientName = clientName;
    }

    public String getClientName() {
        return clientName;
    }
}