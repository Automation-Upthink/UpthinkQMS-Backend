package com.upthink.qms.service.request;

public class ExportPersonAnalyticsRequest extends AuthenticatedRequest {

    public ExportPersonAnalyticsRequest(TokenValidation id, String timezone) {
        super(id);
        this.timezone = timezone;
    }

    public String getTimezone() {
        return timezone;
    }

    private final String timezone;
}