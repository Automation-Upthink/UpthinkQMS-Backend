package com.upthink.qms.service.response;

import gson.GsonDTO;

import java.sql.Timestamp;
import java.util.List;

public class EssayClientCredResponse extends GsonDTO {

    public static class EssayClientCredDTO extends GsonDTO {

        private final int id;
        private final String name;
        private final String clientName;
        private final String password;
        private final String createdBy;
        private final int downloadLimit;

        private final int downloadRemaining;
        private final Timestamp createdAt;

        public EssayClientCredDTO(int id,
                                  String name,
                                  String clientName,
                                  String password,
                                  String createdBy,
                                  int downloadLimit,
                                  int downloadRemaining,
                                  Timestamp createdAt) {
            this.id = id;
            this.name = name;
            this.clientName = clientName;
            this.password = password;
            this.createdBy = createdBy;
            this.downloadLimit = downloadLimit;
            this.downloadRemaining = downloadRemaining;
            this.createdAt = createdAt;
        }
    }

    private final List<EssayClientCredDTO> essayClientCredList;

    public EssayClientCredResponse(List<EssayClientCredDTO> essayClientCredList) {
        this.essayClientCredList = essayClientCredList;
    }
}
