package com.upthink.qms.domain;

import gson.*;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "person")
public class Person extends GsonDTO{

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "cognitoid", nullable = false, unique = true)
    private String cognitoId;

    @Column(name = "groups", columnDefinition = "varchar[]")  // Using TEXT or a similar type to store JSON
    private List<String> groups;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Transient
    private String phoneNumber;

    @Transient
    private String userName;

    // Default constructor required by JPA
    protected Person() {
    }

    // Constructor
    public Person (String id, String cognitoId, String name, String email, List<String> groups, boolean active) {
        this.id = id;
        this.cognitoId = cognitoId;
        this.name = name;
        this.email = email;
        this.groups = groups;
        this.active = active;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getCognitoId() {
        return cognitoId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() { return email; }

    public List<String> getGroups() {
        return groups;
    }

    public boolean isActive() {
        return active;
    }

    public void setGroups(List<String> groups){
        this.groups = groups;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
