package com.upthink.qms.service.response;

import gson.GsonDTO;

import java.util.List;

public class QmAnalyticsResponse extends GsonDTO {

    private final List<PersonAnalyticsResponse.PersonAnalyticsDTO> personAnalytics;

    private final String error;

    public QmAnalyticsResponse(
            List<PersonAnalyticsResponse.PersonAnalyticsDTO> personAnalytics, String error) {
        this.personAnalytics = personAnalytics;
        this.error = error;
    }

    public List<PersonAnalyticsResponse.PersonAnalyticsDTO> getPersonAnalyticsDTOList() {
        return personAnalytics;
    }
}