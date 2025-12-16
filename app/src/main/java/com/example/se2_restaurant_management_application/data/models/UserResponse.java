package com.example.se2_restaurant_management_application.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserResponse {

    @SerializedName("users")
    private List<User> users;

    public List<User> getUsers() {
        return users;
    }
}