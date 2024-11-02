package com.example.recipe_sharing_app.model;

public class User {
    private String fullName;
    private String email;
    private String password;
    private String role;
    private String images;

    // Constructor mặc định (cần thiết cho Firebase)
    public User() {
    }

    // Constructor với tham số
    public User(String fullName, String email, String password, String role, String images) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.images = images;
    }

    // Getter và Setter
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }
}
