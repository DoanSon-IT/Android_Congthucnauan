package com.example.recipe_sharing_app.model;

import java.util.List;

public class Recipe {

    private String recipeId;
    private String name;
    private String description;
    private String authorName;
    private String servings;
    private String cookTime;
    private String imageUrl;
    private List<Ingredient> ingredients;
    private List<Step> steps;

    public Recipe() {
        // Required empty constructor
    }

    public Recipe(String recipeId, String name, String description, String authorName, String servings, String cookTime,
                  String imageUrl, List<Ingredient> ingredients, List<Step> steps) {
        this.recipeId = recipeId;
        this.name = name;
        this.description = description;
        this.authorName = authorName;
        this.servings = servings;
        this.cookTime = cookTime;
        this.imageUrl = imageUrl;
        this.ingredients = ingredients;
        this.steps = steps;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
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

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getServings() {
        return servings;
    }

    public void setServings(String servings) {
        this.servings = servings;
    }

    public String getCookTime() {
        return cookTime;
    }

    public void setCookTime(String cookTime) {
        this.cookTime = cookTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }
}
