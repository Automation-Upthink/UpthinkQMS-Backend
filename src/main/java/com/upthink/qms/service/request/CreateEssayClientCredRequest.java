package com.upthink.qms.service.request;


public class CreateEssayClientCredRequest extends AuthenticatedRequest{

    private  String name, clientName, password;
    private  int downloadLimit;

//    public CreateEssayClientCredRequest(){}

    public CreateEssayClientCredRequest(
            TokenValidation id,
            String name,
            String clientName,
            String password,
            int downloadLimit) {
        super(id);
        this.name = name;
        this.clientName = clientName;
        this.password = password;
        this.downloadLimit = downloadLimit;
    }

    public String getName() {
        return name;
    }

    public String getClientName() {
        return clientName;
    }

    public String getPassword() {
        return password;
    }

    public int getDownloadLimit() {
        return downloadLimit;
    }
}
