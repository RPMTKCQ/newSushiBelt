package com.sushi.game.ui.model;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.sushi.game.ui.RecipeCard;

import java.util.ArrayList;
import java.util.List;

public class RecipeQueue {
    private final List<RecipeCard> cards = new ArrayList<>();
    private static final float START_X = 50f;
    private static final float START_Y = 600f;
    private static final float CARD_WIDTH = 120f;
    private static final float CARD_SPACING = 7f;


    public void addOrder(String dishName, String customerId) {
        float x = START_X + cards.size() * (CARD_WIDTH + CARD_SPACING);
        cards.add(new RecipeCard(dishName, customerId, x, START_Y));
    }

    public void removeOrder(String customerId) {
        cards.removeIf(card -> card.customerId.equals(customerId));
    }

    public List<RecipeCard> getCards() {
        return cards;
    }

    public String getFirstDishName() {
        return cards.isEmpty() ? null : cards.get(0).dishName;
    }
}
