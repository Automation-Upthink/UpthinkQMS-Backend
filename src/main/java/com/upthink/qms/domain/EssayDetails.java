package com.upthink.qms.domain;

import gson.GsonDTO;
import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "essay_details")
public class EssayDetails extends GsonDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @Column(name="person_id")
    private String personId;

    @Column(name="essay_id")
    private String essayId;

    @Column(name = "user_action")
    private String userAction;

    @Column(name="created_at")
    private Timestamp createdAt;

    @Column(name="updated_at")
    private Timestamp updatedAt;

    @Column(name="deleted_at")
    private Timestamp deletedAt;

    @Transient
    private Timestamp downloadedAt;

    @Transient
    private String clientName;

    @Transient
    private String credsName;


    public String getClientName() {
        return clientName;
    }

    public String getCredsName() {
        return credsName;
    }


    public Timestamp getDownloadedAt() {
        return downloadedAt;
    }

    public EssayDetails(){}

    public EssayDetails(
            int id,
            String personId,
            String essayId,
            String userAction,
            Timestamp createdAt) {
        this.id = id;
        this.personId = personId;
        this.essayId = essayId;
        this.userAction = userAction;
        this.createdAt = createdAt;
        this.clientName = "";
        this.credsName = "";
        this.downloadedAt = null;
    }

    public EssayDetails(
            int id,
            String personId,
            String essayId,
            String userAction,
            Timestamp createdAt,
            String clientName,
            String credsName,
            Timestamp downloadedAt) {
        this.id = id;
        this.personId = personId;
        this.essayId = essayId;
        this.userAction = userAction;
        this.createdAt = createdAt;
        this.clientName = clientName;
        this.credsName = credsName;
        this.downloadedAt = downloadedAt;
    }

    public int getId() {
        return id;
    }

    public String getPersonId() {
        return personId;
    }

    public String getEssayId() {
        return essayId;
    }

    public String getUserAction() {
        return userAction;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }
}
