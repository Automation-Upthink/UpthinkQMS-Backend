package com.upthink.qms.service.response;

import gson.GsonDTO;

import java.sql.Timestamp;
import java.util.List;

public class EssayResponse extends GsonDTO {

    public static class EssayDTO extends GsonDTO {

        private final String clientName;
        private final String essayName;
        private final String essayId;
        private final String credName;
        private final String checkedOutBy;
        private final String status;
        private final String fileLink;
        private final long dueDate;
        private final String downloadTime, checkedOutTime;

        public EssayDTO(String clientName,
                        String essayName,
                        String essayId,
                        String credName,
                        String checkedOutBy,
                        String status,
                        String fileLink,
                        long dueDate,
                        String downloadTime,
                        String checkedOutTime) {
            this.clientName = clientName;
            this.essayName = essayName;
            this.essayId = essayId;
            this.credName = credName;
            this.checkedOutBy = checkedOutBy;
            this.status = status;
            this.fileLink = fileLink;
            this.dueDate = dueDate;
            this.downloadTime = downloadTime;
            this.checkedOutTime = checkedOutTime;
        }

        public EssayDTO(
                String clientName,
                String essayName,
                String essayId,
                String credName,
                String status,
                String downloadTime,
                String fileLink,
                long dueDate) {
            this.clientName = clientName;
            this.essayName = essayName;
            this.essayId = essayId;
            this.credName = credName;
            this.status = status;
            this.downloadTime = downloadTime;
            this.fileLink = fileLink;
            this.checkedOutBy = null;
            this.checkedOutTime = null;
            this.dueDate = dueDate;
        }
    }

    private final List<EssayDTO> essayList;
    private boolean success;
    private String error;

    public EssayResponse(List<EssayDTO> essayList, boolean success, String error){
        this.essayList = essayList;
        this.success = success;
        this.error = error;
    }
}
