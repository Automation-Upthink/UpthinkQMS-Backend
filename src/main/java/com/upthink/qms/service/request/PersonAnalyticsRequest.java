package com.upthink.qms.service.request;

public class PersonAnalyticsRequest extends AuthenticatedRequest {

    public enum TimeSpan {
        daily,
        weekly,
        monthly,
        overall
    }

    public enum PersonTimeSpan {
        daily,
        weekly,
        monthly,
        overall
    }

    private final PersonTimeSpan timespan;
    private final String personId;
    private final String timezone;

    public PersonAnalyticsRequest(
            TokenValidation id, PersonTimeSpan timespan, String personId, String timezone) {
        super(id);
        this.timespan = timespan;
        this.personId = personId;
        this.timezone = timezone;
    }

    public PersonTimeSpan getTimespan() {
        return timespan;
    }

    public String getPersonId() {
        return personId;
    }

    public String getTimezone() {
        return timezone;
    }
}