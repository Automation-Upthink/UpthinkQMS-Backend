package com.upthink.qms.service.request;

import java.util.List;

public class ListEssayRequest extends AuthenticatedRequest{

    private List<String> essayStatusList;

    public int getClientId() {
        return clientId;
    }

    private int clientId;


//    public ListEssayRequest(){}


    public ListEssayRequest(TokenValidation id, List<String> essayStatusList) {
        super(id);
        this.essayStatusList = essayStatusList;
        this.clientId = 0;
    }

    public ListEssayRequest(TokenValidation id, List<String> essayStatusList, int clientId) {
        super(id);
        this.essayStatusList = essayStatusList;
        this.clientId = clientId;
    }


    public List<String> getEssayStatus() {
        return essayStatusList;
    }
}