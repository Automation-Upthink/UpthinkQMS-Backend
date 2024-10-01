package com.upthink.qms.domain;

import gson.GsonDTO;
import jakarta.persistence.*;

@Entity
@Table(name = "essay_client")
public class EssayClient extends GsonDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "download_capacity", nullable = false)
    private int downloadCapacity;

    // Default constructor for JPA
    protected EssayClient() {}

    public EssayClient(String name, int downloadCapacity) {
        this.name = name;
        this.downloadCapacity = downloadCapacity;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDownloadCapacity() {
        return downloadCapacity;
    }

    public void setDownloadCapacity(int downloadCapacity) {
        this.downloadCapacity = downloadCapacity;
    }
}
