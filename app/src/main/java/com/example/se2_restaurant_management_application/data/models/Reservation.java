package com.example.se2_restaurant_management_application.data.models;

public class Reservation {

    private int id;
    private String status;
    private String dateTime;
    private int numberOfGuests;
    private int tableNumber;
    private String userId;


    public Reservation(int id, String status, String dateTime, int numberOfGuests, int tableNumber, String userId) {
        this.id = id;
        this.status = status;
        this.dateTime = dateTime;
        this.numberOfGuests = numberOfGuests;
        this.tableNumber = tableNumber;
        this.userId = userId;
    }

    public Reservation(String status, String dateTime, int numberOfGuests, int tableNumber, String userId) {
        this.status = status;
        this.dateTime = dateTime;
        this.numberOfGuests = numberOfGuests;
        this.tableNumber = tableNumber;
        this.userId = userId;
    }

    // --- Getters and Setters ---
    public String getUserId() { // CHANGE THIS
        return userId;
    }

    public void setUserId(String userId) { // CHANGE THIS
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
