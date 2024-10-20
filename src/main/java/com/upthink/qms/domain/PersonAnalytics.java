package com.upthink.qms.domain;

import gson.GsonDTO;
import jakarta.persistence.*;

@Entity
@Table(name="person_analytics")
public class PersonAnalytics extends GsonDTO {

    @Id
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name="checked_in_num")
    private int checkInNum;

    @Column(name="checked_out_num")
    private int checkOutNum;

    @Column(name="reupload_num")
    private int reuploadNum;

    @Column(name="avg_grade_time")
    private Integer avgGradeTime;

    @Column(name="person_id")
    private String personId;

    @Transient
    private String personName;

    @Transient
    private String personEmail;

    // Default constructor required by JPA
    protected PersonAnalytics() {
    }

    public PersonAnalytics(
            int id,
            int checkInNum,
            int checkOutNum,
            int reuploadNum,
            Integer avgGradeTime,
            String personId) {
        this.id = id;
        this.checkInNum = checkInNum;
        this.checkOutNum = checkOutNum;
        this.reuploadNum = reuploadNum;
        this.avgGradeTime = avgGradeTime;
        this.personId = personId;
        this.personName = "";
        this.personEmail = "";
    }

    public PersonAnalytics(
            int id,
            int checkInNum,
            int checkOutNum,
            int reuploadNum,
            Integer avgGradeTime,
            String personId,
            String personName,
            String personEmail) {
        this.id = id;
        this.checkInNum = checkInNum;
        this.checkOutNum = checkOutNum;
        this.reuploadNum = reuploadNum;
        this.avgGradeTime = avgGradeTime;
        this.personId = personId;
        this.personName = personName;
        this.personEmail = personEmail;
    }

    public int getId() {
        return id;
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

    public Integer getAvgGradeTime() {
        return avgGradeTime;
    }

    public String getPersonId() {
        return personId;
    }

    public String getPersonName() {
        return personName;
    }

    public String getPersonEmail() {
        return personEmail;
    }

}
