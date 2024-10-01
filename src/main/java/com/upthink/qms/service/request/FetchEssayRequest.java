package com.upthink.qms.service.request;

import org.springframework.security.core.context.SecurityContextHolder;

public class FetchEssayRequest {

    private final String clientName;

    public FetchEssayRequest(String clientName) {
        this.clientName = clientName;
    }

    public String getClientName() {
        return clientName;
    }

    public String getUserId() {
        // Retrieve the user ID from the Security Context
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
