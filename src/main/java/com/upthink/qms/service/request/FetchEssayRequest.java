package com.upthink.qms.service.request;

import gson.GsonDTO;
import org.springframework.security.core.context.SecurityContextHolder;

public class FetchEssayRequest extends AuthenticatedRequest {

    private final String clientName;

    public FetchEssayRequest(TokenValidation id, String clientName) {
        super(id);
        this.clientName = clientName;
    }

    public String getClientName() {
        return clientName;
    }

//    public String getUserId() {
//        // Retrieve the user ID from the Security Context
//        return SecurityContextHolder.getContext().getAuthentication().getName();
//    }
}
