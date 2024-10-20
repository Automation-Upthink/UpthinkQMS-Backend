package com.upthink.qms.service.request;

public class ScriptStatusRequest extends AuthenticatedRequest {

    private final String ruleName;

    public ScriptStatusRequest(TokenValidation id, String ruleName) {
        super(id);
        this.ruleName = ruleName;
    }

    public String getRuleName() {
        return ruleName;
    }
}