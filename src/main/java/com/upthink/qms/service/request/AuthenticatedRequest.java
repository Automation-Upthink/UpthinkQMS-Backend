package com.upthink.qms.service.request;

import gson.GsonDTO;
import com.upthink.qms.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;

public class AuthenticatedRequest extends GsonDTO {

    // Protected field for TokenValidation
    protected TokenValidation id;

//    public AuthenticatedRequest(){}

    // Constructor that takes TokenValidation
    public AuthenticatedRequest(TokenValidation id) {
        this.id = id;
    }

    // Getter for TokenValidation object
    public TokenValidation getId() {
        return this.id;
    }

    // Convenience method to get the subject from TokenValidation
    public String getSubject() {
        return this.id.getSub();
    }

    public void setId(TokenValidation id){
        this.id = id;
    }
}
