package com.upthink.qms.domain;

import gson.GsonDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;


public class QmAnalytics extends GsonDTO {
    private final String personId;
    private final String personName;
    private final String personEmail;
    private final int checkInNum;
    private final int checkOutNum;
    private final int reuploadNum;
    private final int avgGradeTime;

    public QmAnalytics(
            String personId, int checkInNum, int checkOutNum, int reuploadNum, int avgGradeTime) {
        this.personId = personId;
        this.checkInNum = checkInNum;
        this.checkOutNum = checkOutNum;
        this.reuploadNum = reuploadNum;
        this.avgGradeTime = avgGradeTime;
        this.personName = "";
        this.personEmail = "";
    }

    public QmAnalytics(
            String personId,
            String personName,
            String personEmail,
            int checkInNum,
            int checkOutNum,
            int reuploadNum,
            int avgGradeTime) {
        this.personId = personId;
        this.personName = personName;
        this.personEmail = personEmail;
        this.checkInNum = checkInNum;
        this.checkOutNum = checkOutNum;
        this.reuploadNum = reuploadNum;
        this.avgGradeTime = avgGradeTime;
    }

    public String getPersonId() {
        return personId;
    }

    public int getCheckInNum() {
        return checkInNum;
    }

    public int getCheckOutNum() {
        return checkOutNum;
    }

    public int getReuploadNum() {
        return reuploadNum;
    }

    public int getAvgGradeTime() {
        return avgGradeTime;
    }

    public String getPersonName() {
        return personName;
    }

    public String getPersonEmail() {
        return personEmail;
    }
}
