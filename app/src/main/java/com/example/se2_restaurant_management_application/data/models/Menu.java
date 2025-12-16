package com.example.se2_restaurant_management_application.data.models;

public class Menu implements DisplayableItem {
    private int id;
    private String name;
    private String description;
    private double price;
    private int imageDrawableId;
    private String imageUri;
    private String category;

    // No-argument constructor
    public Menu() {
    }

    // Constructor for creating new menu items
    public Menu(String name, String description, double price, int imageDrawableId, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageDrawableId = imageDrawableId;
        this.imageUri = null;
        this.category = category;
    }

    // --- Getters and Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getImageDrawableId() {
        return imageDrawableId;
    }

    public void setImageDrawableId(int imageDrawableId) {
        this.imageDrawableId = imageDrawableId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
