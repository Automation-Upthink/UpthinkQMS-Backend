package com.upthink.qms.service.response;

import com.upthink.qms.domain.EssayDetails;
import gson.GsonDTO;

import java.util.List;

public class PersonAnalyticsResponse extends GsonDTO {

    public static class PersonAnalyticsDTO extends GsonDTO {
        private final String personId;
        private final String personName;
        private final String personEmail;
        private int totalCheckOuts, totalCheckIns, totalReuploads;
        private int avgGradeTime;

        public PersonAnalyticsDTO(
                String personId,
                String personName,
                String personEmail,
                int totalCheckOuts,
                int totalCheckIns,
                int totalReuploads,
                int avgGradeTime) {
            this.personId = personId;
            this.personName = personName;
            this.totalCheckOuts = totalCheckOuts;
            this.totalCheckIns = totalCheckIns;
            this.totalReuploads = totalReuploads;
            this.avgGradeTime = avgGradeTime;
            this.personEmail = personEmail;
        }

        public String getPersonId() {
            return personId;
        }

        public String getPersonName() {
            return personName;
        }

        public int getTotalCheckOuts() {
            return totalCheckOuts;
        }

        public int getTotalCheckIns() {
            return totalCheckIns;
        }

        public int getTotalReuploads() {
            return totalReuploads;
        }

        public int getAvgGradeTime() {
            return avgGradeTime;
        }

        public String getPersonEmail() {
            return personEmail;
        }
    }

    public static class EssayDetailsDTO extends GsonDTO {
        private List<EssayDetails> essayLogs;

        public EssayDetailsDTO(List<EssayDetails> essayLogs) {
            this.essayLogs = essayLogs;
        }
    }

    private final PersonAnalyticsDTO personAnalytics;
    private final EssayDetailsDTO essayLogs;
    private final String error;

    public PersonAnalyticsResponse(
            PersonAnalyticsDTO personAnalytics, EssayDetailsDTO essayLogs, String error) {
        this.personAnalytics = personAnalytics;
        this.essayLogs = essayLogs;
        this.error = error;
    }
}
