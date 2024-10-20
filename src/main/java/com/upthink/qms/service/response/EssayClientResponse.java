package com.upthink.qms.service.response;

import gson.GsonDTO;

import java.util.List;

public class EssayClientResponse extends GsonDTO {

    public static class EssayClientDTO extends GsonDTO {

        private final int id;
        private final String clientName;
        private final int downloadCapacity;

        public EssayClientDTO(int id, String clientName, int downloadCapacity) {
            this.id = id;
            this.clientName = clientName;
            this.downloadCapacity = downloadCapacity;
        }

        public int getId() {
            return id;
        }

        public String getClientName() {
            return clientName;
        }

        public int getDownloadCapacity() {
            return downloadCapacity;
        }
    }

    private final List<EssayClientDTO> essayClientList;

    public EssayClientResponse(List<EssayClientDTO> essayClientList) {
        this.essayClientList = essayClientList;
    }
}
