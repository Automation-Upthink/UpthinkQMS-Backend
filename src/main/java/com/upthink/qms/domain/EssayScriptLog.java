package com.upthink.qms.domain;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.type.descriptor.jdbc.TimestampWithTimeZoneJdbcType;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "essay_script_log")
public class EssayScriptLog {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "person_id")
    private String personId;

    @Column(name = "action_performed")
    private String actionPerformed;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updated_at;

    @Column(name = "deleted_at")
    private Timestamp deletedAt;

    @Column(name = "type")
    private String type;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "uuid")
    private String uuid;


    public EssayScriptLog(int id, String personId, String actionPerformed, Timestamp createdAt) {
        this.id = id;
        this.personId = personId;
        this.actionPerformed = actionPerformed;
        this.createdAt = createdAt;
    }

    public EssayScriptLog(String personId, String actionPerformed, String type, String clientName, String uuid) {
        this.personId = personId;
        this.actionPerformed = actionPerformed;
        this.type = type;
        this.uuid = uuid;
    }

    public int getId() {
        return id;
    }

    public String getActionPerformed() {
        return actionPerformed;
    }

    public String getType() {
        return type;
    }

    public String getClientName() {
        return clientName;
    }

    public String getUuid() {
        return uuid;
    }


    public String getPersonId() {
        return personId;
    }

    public String getUserAction() {
        return actionPerformed;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdated_at() {
        return updated_at;
    }

    public Timestamp getDeletedAt() {
        return deletedAt;
    }
}
