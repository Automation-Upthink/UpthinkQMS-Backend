package com.upthink.qms.service.request;

import com.upthink.qms.service.JwtService;
import gson.GsonDTO;
import jakarta.persistence.Column;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


public class TokenValidation extends GsonDTO {
    private boolean validated;
    private String sub;
    private List<String> groups;
    private String name;
    private String email;
    private String phoneNumber;
    private String userName;
    private String validationError;
    private Map<String, String> customAttributes;
    private static String jwtToken;

    public TokenValidation(){}

    public TokenValidation(boolean validated, String sub, List<String> groups, String name, String email, String phoneNumber, String userName, String validationError, Map<String, String> customAttributes, String jwtToken) {
        this.validated = validated;
        this.sub = sub;
        this.groups = groups;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.userName = userName;
        this.validationError = validationError;
        this.customAttributes = customAttributes;
        this.jwtToken = jwtToken;
    }

    public boolean isValidated() {
        return this.validated;
    }

    public String getSub() {
        return this.sub;
    }

    public List<String> getGroups() {
        return this.groups;
    }

    public String getName() {
        return this.name;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getValidationError() {
        return this.validationError;
    }

    public Map<String, String> getCustomAttributes() {
        return this.customAttributes;
    }

    public String getJwtToken() {
        return this.jwtToken;
    }

    public static TokenValidation defaultToken() {
        return new TokenValidation(true, (String)null, (List)null, (String)null, (String)null, (String)null, (String)null, (String)null, (Map)null, jwtToken);
    }

    public static TokenValidation failedToken() {
        return new TokenValidation(false, (String)null, (List)null, (String)null, (String)null, (String)null, (String)null, (String)null, (Map)null, jwtToken);
    }

    public TokenValidation withGroups(List<String> groups) {
        return new TokenValidation(this.validated, this.sub, groups, this.name, this.email, this.phoneNumber, this.userName, this.validationError, this.customAttributes, jwtToken);
    }

    public TokenValidation withName(String name) {
        return new TokenValidation(this.validated, this.sub, this.groups, name, this.email, this.phoneNumber, this.userName, this.validationError, this.customAttributes, jwtToken);
    }
}

