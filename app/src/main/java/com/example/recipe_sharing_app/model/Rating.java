package com.example.recipe_sharing_app.model;

public class Rating {
    private String userId;
    private float rating;
    public Rating() {
    }

    public Rating(String userId, float rating) {
        this.userId = userId;
        this.rating = rating;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
