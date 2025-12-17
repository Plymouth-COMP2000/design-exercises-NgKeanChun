package com.example.se2_restaurant_management_application.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;


public class ApiResponse {

    @SerializedName("message")
    private String message;


    @SerializedName("notifications")
    private List<Notification> notifications;

    // --- Getters and Setters ---

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }
}
