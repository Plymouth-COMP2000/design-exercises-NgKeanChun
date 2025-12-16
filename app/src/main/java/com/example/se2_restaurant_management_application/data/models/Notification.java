package com.example.se2_restaurant_management_application.data.models;

import com.google.gson.annotations.SerializedName;

// Implements DisplayableItem to be used in adapters with headers
public class Notification implements DisplayableItem {

    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("title")
    private String title;

    @SerializedName("body")
    private String body;

    @SerializedName("status")
    private String status;

    @SerializedName("is_read")
    private boolean isRead;

    // Constructors
    public Notification(int id, int userId, String title, String body, String status, boolean isRead) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.body = body;
        this.status = status;
        this.isRead = isRead;
    }

    public Notification(int userId, String title, String body, String status) {
        this.userId = userId;
        this.title = title;
        this.body = body;
        this.status = status;
        this.isRead = false;
    }

    // --- Helpers ---
    public void markAsRead() {
        this.isRead = true;
    }

    // --- Getters ---
    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getStatus() {
        return status;
    }

    public boolean isRead() {
        return isRead;
    }

    // --- Setters ---
    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
