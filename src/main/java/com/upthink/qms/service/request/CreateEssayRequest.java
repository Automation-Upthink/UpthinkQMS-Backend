package com.upthink.qms.service.request;

import gson.GsonDTO;

public class CreateEssayRequest extends GsonDTO {

    private final String taskId;
    private final String name;
    private final String clientName;
    private final String uuid;
    private final String dueDate;
    private final int credId;

    public String getUuid() { return uuid;}

    public CreateEssayRequest(String taskId, String name, String clientName, String uuid, String dueDate, int credId) {
        this.taskId = taskId;
        this.name = name;
        this.clientName = clientName;
        this.uuid = uuid;
        this.dueDate = dueDate;
        this.credId = credId;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getName() {
        return name;
    }

    public String getClientName() {return clientName;}

    public int getCredId() {
        return credId;
    }

    public String getDueDate() {
        return dueDate;
    }


}
