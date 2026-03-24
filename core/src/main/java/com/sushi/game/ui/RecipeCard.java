package com.sushi.game.ui;

import com.badlogic.gdx.math.MathUtils;

public class RecipeCard {
    public String dishName;
    public String customerId;
    public float timer = 0f;
    public float maxTime;       // now set dynamically, adjustable

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
        this.maxTime = MathUtils.random(15f, 30f);
    }

    public boolean contains(float px, float py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }

    public float getPatienceRatio() {
        return 1f - (timer / maxTime);   // 1 = full patience, 0 = expired
    }

    public boolean isExpired() {
        return timer >= maxTime;
    }
}

