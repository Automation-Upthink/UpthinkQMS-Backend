package com.upthink.qms.service.request;

public class DownloadAnalyticsRequest extends AuthenticatedRequest {

    private final String startTime;
    private final String endTime;

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public DownloadAnalyticsRequest(TokenValidation id, String startTime, String endTime) {
        super(id);
        this.startTime = startTime;
        this.endTime = endTime;
    }
}