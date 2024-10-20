package com.upthink.qms.service.response;

import gson.GsonDTO;

import java.sql.Timestamp;
import java.util.List;

public class QMAnalyticsDownloadResponse extends GsonDTO {

    public static class QMAnalyticsDownloadDTO extends GsonDTO {

        private final String personId;
        private final String essayId;

        private final String userAction;

        private final Timestamp createdAt;

        private final String clientName;

        private final String credName;

        private final Timestamp downloadedAt;

        public QMAnalyticsDownloadDTO(
                String personId,
                String essayId,
                String userAction,
                Timestamp createdAt,
                String clientName,
                String credName,
                Timestamp downloadedAt) {
            this.personId = personId;
            this.essayId = essayId;
            this.userAction = userAction;
            this.createdAt = createdAt;
            this.clientName = clientName;
            this.credName = credName;
            this.downloadedAt = downloadedAt;
        }

        public String getPersonId() {
            return personId;
        }

        public String getEssayId() {
            return essayId;
        }

        public String getUserAction() {
            return userAction;
        }

        public Timestamp getCreatedAt() {
            return createdAt;
        }

        public String getClientName() {
            return clientName;
        }

        public String getCredName() {
            return credName;
        }

        public Timestamp getDownloadedAt() {
            return downloadedAt;
        }
    }

    private final List<QMAnalyticsDownloadDTO> essayAnalytics;

    private final String error;

    public String getError() {
        return error;
    }

    public QMAnalyticsDownloadResponse(List<QMAnalyticsDownloadDTO> essayAnalytics, String error) {
        this.essayAnalytics = essayAnalytics;
        this.error = error;
    }

    public List<QMAnalyticsDownloadDTO> getEssayAnalytics() {
        return essayAnalytics;
    }
}