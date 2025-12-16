package com.example.se2_restaurant_management_application.data.models;

public class CategoryHeader implements DisplayableItem {
    private String categoryName;

    public CategoryHeader(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }
}
