package com.upthink.qms.service.request;

public class ChangeStatusRequest extends AuthenticatedRequest{

    private String email;

    // Constructors
//    public ChangeStatusRequest() {}

    public ChangeStatusRequest(TokenValidation id, String email) {
        super(id);
        this.email = email;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }


}
