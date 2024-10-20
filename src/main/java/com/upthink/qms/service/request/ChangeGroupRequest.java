package com.upthink.qms.service.request;

public class ChangeGroupRequest extends AuthenticatedRequest {

    private String email;
    private String newGroup;

    // Constructors
//    public ChangeGroupRequest() {}

    public ChangeGroupRequest(TokenValidation id, String email, String newGroup) {
        super(id);
        this.email = email;
        this.newGroup = newGroup;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNewGroup() {
        return newGroup;
    }

    public void setNewGroup(String newGroup) {
        this.newGroup = newGroup;
    }
}
