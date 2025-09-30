package com.sifat.bcsbnagla.model;

import com.google.gson.annotations.SerializedName;

public class Photo {
    private int id;

    @SerializedName("imageUrl")
    private String imageUrl;

    private String description;

    @SerializedName("createdAt")
    private String createdAt;

    public Photo(int id, String imageUrl, String description, String createdAt) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.description = description;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}