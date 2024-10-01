package com.upthink.qms.service.request;

public class EssayStatusChangeRequest extends AuthenticatedRequest{

    private final String essayId;
    private final String status;

    public EssayStatusChangeRequest(TokenValidation id, String personId, String essayId, String status) {
        super(id);
        this.essayId = essayId;
        this.status = status;
    }

    public String getEssayId() {
        return essayId;
    }

    public String getStatus() {
        return status;
    }
}
