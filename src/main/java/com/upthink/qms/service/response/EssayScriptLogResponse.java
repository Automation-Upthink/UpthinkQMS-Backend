package com.upthink.qms.service.response;

import gson.GsonDTO;

import java.sql.Timestamp;
import java.util.List;

public class EssayScriptLogResponse extends GsonDTO {
    public static class EssayScriptLogDTO extends GsonDTO {

        public int getId() {
            return id;
        }

        public String getPersonId() {
            return personId;
        }

        public String getActionPerformed() {
            return actionPerformed;
        }

        public String getClientName() {
            return clientName;
        }

        public String getType() {
            return type;
        }

        public Timestamp getCreatedAt() {
            return createdAt;
        }

        public int getTimeDiff() {
            return timeDiff;
        }

        private final int id;
        private final String personId;
        private final String actionPerformed;
        private final String clientName;
        private final String type;
        private final Timestamp createdAt;

        private int timeDiff;

        public EssayScriptLogDTO(
                int id,
                String personId,
                String clientName,
                String actionPerformed,
                String type,
                Timestamp createdAt,
                int timeDiff) {
            this.id = id;
            this.clientName = clientName;
            this.actionPerformed = actionPerformed;
            this.personId = personId;
            this.type = type;
            this.createdAt = createdAt;
            this.timeDiff = timeDiff;
        }
    }

    private final List<EssayScriptLogDTO> essayScriptLogList;
    private boolean success;
    private String error;

    public EssayScriptLogResponse(
            List<EssayScriptLogDTO> essayScriptLogList, boolean success, String error) {
        this.essayScriptLogList = essayScriptLogList;
        this.success = success;
        this.error = error;
    }

    public static class ScriptStatusResponse extends GsonDTO {
        private final String status;
        private boolean success;

        public ScriptStatusResponse(Boolean success, String status) {
            this.status = status;
            this.success = success;
        }
    }
}