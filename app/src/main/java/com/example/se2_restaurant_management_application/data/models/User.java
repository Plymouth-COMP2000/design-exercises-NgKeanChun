package com.example.se2_restaurant_management_application.data.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class User implements Serializable {



    private int id;
    @SerializedName("image_uri")
    private String imageUri;
    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    @SerializedName("firstname")
    private String firstName;

    @SerializedName("lastname")
    private String lastName;

    @SerializedName("email")
    private String email;

    @SerializedName("contact")
    private String contact;

    @SerializedName("usertype")
    private String userType;


    // --- Constructor, Getters and Setters ---

    public User(String username, String password, String firstName, String lastName, String email, String contact, String userType) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.contact = contact;
        this.userType = userType;
    }


    // --- Getters and Setters for all fields ---
    public int getId() { return id; }

    public String getImageUri() { return imageUri;}
    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    // A helper method to get the full name for display purposes in the app
    public String getFullName() {
        return firstName + " " + lastName;
    }
}