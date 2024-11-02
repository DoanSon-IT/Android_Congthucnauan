package com.example.recipe_sharing_app.model;

public class Comment {
    private String userId;      // ID người dùng
    private String recipeId;    // ID công thức
    private String userName;    // Tên người dùng
    private String userAvatar;  // Đường dẫn đến ảnh đại diện của người dùng
    private String content;      // Nội dung bình luận
    private String time;
    // Thời gian bình luận

    // Constructor mặc định (cần thiết cho Firebase)
    public Comment() {
    }

    // Constructor với tham số
    public Comment(String userId, String recipeId, String userName, String userAvatar, String content, String time) {
        this.userId = userId;
        this.recipeId = recipeId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.content = content;
        this.time = time;
    }

    // Getter và Setter
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
