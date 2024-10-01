package com.upthink.qms.service.request;

public class ChangeEventRequest extends AuthenticatedRequest {

    private final String ruleName;
    private final String clientName;
    private final boolean start;

    public ChangeEventRequest(
            TokenValidation id, String ruleName, String clientName, boolean start) {
        super(id);
        this.ruleName = ruleName;
        this.clientName = clientName;
        this.start = start;
    }

    public String getClientName() {
        return clientName;
    }

    public String getRuleName() {
        return ruleName;
    }

    public boolean isStart() {
        return start;
    }
}