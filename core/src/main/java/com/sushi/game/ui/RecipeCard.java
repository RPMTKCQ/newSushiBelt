package com.sushi.game.ui;

public class RecipeCard {
    public String dishName;
    public String customerId;
    public float timer = 0f;
    public float maxTime = 60f; // seconds before customer gets angry

    // position in UI space (320x180)
    public float x;
    public float y;
    public float width = 35f;
    public float height = 45f;

    // drag state
    public boolean isDragging = false;
    public float dragOffsetX = 0f;

    public RecipeCard(String dishName, String customerId, float x, float y) {
        this.dishName = dishName;
        this.customerId = customerId;
        this.x = x;
        this.y = y;
    }

    public boolean contains(float px, float py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }
}
