package com.upthink.qms.domain;


import com.upthink.qms.utils.StringLongDateConverter;
import gson.GsonDTO;
import jakarta.persistence.*;
import java.sql.Timestamp;


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
//    @Column(name ="due_date")
    @Column(name = "due_date", columnDefinition = "VARCHAR")
    @Convert(converter = StringLongDateConverter.class)
    private long dueDate;
    @Column(name ="essay_client_id")
    private  int clientId;
    @Column(name ="essay_cred_id")
    private  int credId;
    @Column(name ="grade_time")
    private Integer gradeTime;
    @Column(name = "created_at")
    private Timestamp downloadTime;
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
            long dueDate) {
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
        this.gradeTime = 0;
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
            Integer gradeTime,
            Timestamp downloadTime,
            long dueDate) {
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

    public long getDueDate() {
        return dueDate;
    }

    public int getClientId() {
        return clientId;
    }

    public int getCredId() {
        return credId;
    }

    public Integer getGradeTime() {
        return gradeTime;
    }

    public Timestamp getDownloadTime() {return downloadTime;}

    public void setStatus(String status) {
        this.status = status;
    }

    public void setGradeTime(Integer gradeTime) {
        this.gradeTime = gradeTime;
    }

    public String getClientName() {
        return clientName;
    }

    public String getCredName() {
        return credName;
    }
}
