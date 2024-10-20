package com.upthink.qms.service.request;

public class QmAnalyticsRequest extends AuthenticatedRequest {

    public enum TimeSpan {
        daily,
        weekly,
        monthly,
        overall
    }

    private final TimeSpan timespan;

    private final int clientId;
    private final String timezone;

    public QmAnalyticsRequest(TokenValidation id, TimeSpan timespan, String timezone) {
        super(id);
        this.timespan = timespan;
        this.clientId = 0;
        this.timezone = timezone;
    }

    public QmAnalyticsRequest(
            TokenValidation id, TimeSpan timespan, int clientId, String timezone) {
        super(id);
        this.timespan = timespan;
        this.clientId = clientId;
        this.timezone = timezone;
    }

    public int getClientId() {
        return clientId;
    }

    public TimeSpan getTimespan() {
        return timespan;
    }

    public String getTimezone() {
        return timezone;
    }
}