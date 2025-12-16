package com.example.se2_restaurant_management_application.data.models;

public class Reservation {

    private int id;
    private String status;
    private String dateTime;
    private int numberOfGuests;
    private int tableNumber;
    private int userId; // <-- ADD THIS FIELD

    // UPDATE THIS CONSTRUCTOR
    public Reservation(int id, String status, String dateTime, int numberOfGuests, int tableNumber, int userId) {
        this.id = id;
        this.status = status;
        this.dateTime = dateTime;
        this.numberOfGuests = numberOfGuests;
        this.tableNumber = tableNumber;
        this.userId = userId; // <-- INITIALIZE IT
    }

    // UPDATE THIS CONSTRUCTOR
    public Reservation(String status, String dateTime, int numberOfGuests, int tableNumber, int userId) {
        this.status = status;
        this.dateTime = dateTime;
        this.numberOfGuests = numberOfGuests;
        this.tableNumber = tableNumber;
        this.userId = userId; // <-- INITIALIZE IT
    }

    // --- Getters and Setters ---

    // ADD GETTER AND SETTER FOR USER ID
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }
}
