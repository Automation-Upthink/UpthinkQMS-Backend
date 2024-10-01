package com.upthink.qms.domain;


import gson.GsonDTO;
import jakarta.persistence.*;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name="essay")
public class Essay extends GsonDTO {

    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "essay_id")
    private String essayId;
    @Column(name = "name")
    private String name;
    @Column(name ="filelink")
    private String fileLink;
    @Column(name ="status")
    private String status;
    @Column(name ="due_date")
    private String dueDate;
    @Column(name ="essay_client_id")
    private  int clientId;
    @Column(name ="essay_cred_id")
    private  int credId;
    @Column(name ="grade_time")
    private Long gradeTime;
    @Column(name = "created_at")
    private  Timestamp downloadTime;
    @Column(name = "fetched_for")
    private String fetched_for;
    @Transient
    private String clientName;
    @Transient
    private String credName;

    protected Essay(){}

    public Essay(
            String id,
            String essayId,
            String name,
            String fileLink,
            String status,
            String clientName,
            String credName,
            int clientId,
            int credId,
            Timestamp downloadTime,
            String dueDate) {
        this.id = id;
        this.essayId = essayId;
        this.name = name;
        this.fileLink = fileLink;
        this.status = status;
        this.clientName = clientName;
        this.credName = credName;
        this.clientId = clientId;
        this.credId = credId;
        this.downloadTime = downloadTime;
        this.gradeTime = (long) 0.0;
        this.dueDate = dueDate;
    }

    public Essay(
            String id,
            String essayId,
            String name,
            String fileLink,
            String status,
            int clientId,
            int credId,
            long gradeTime,
            Timestamp downloadTime,
            String dueDate) {
        this.id = id;
        this.essayId = essayId;
        this.name = name;
        this.fileLink = fileLink;
        this.status = status;
        this.clientName = "";
        this.credName = "";
        this.clientId = clientId;
        this.credId = credId;
        this.gradeTime = gradeTime;
        this.downloadTime = downloadTime;
        this.dueDate = dueDate;
    }

    public String getId() {
        return id;
    }

    public String getEssayId() {
        return essayId;
    }

    public String getName() {
        return name;
    }

    public String getFileLink() {
        return fileLink;
    }

    public String getStatus() {
        return status;
    }


    public String getDueDate() {
        return dueDate;
    }

    public int getClientId() {
        return clientId;
    }

    public int getCredId() {
        return credId;
    }

    public long getGradeTime() {
        return gradeTime;
    }

    public Timestamp getDownloadTime() {
        return downloadTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setGradeTime(long gradeTime) {
        this.gradeTime = gradeTime;
    }

    public String getClientName() {
        return clientName;
    }

    public String getCredName() {
        return credName;
    }
}
