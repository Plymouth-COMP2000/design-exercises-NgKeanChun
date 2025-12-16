package com.example.se2_restaurant_management_application.data.models;

import com.google.gson.annotations.SerializedName;

public class SingleUserResponse {

    @SerializedName("user")
    private User user;

    public User getUser() {
        return user;
    }
}
