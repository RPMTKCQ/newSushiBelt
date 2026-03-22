package com.sushi.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sushi.game.ui.model.RecipeQueue;

import java.util.ArrayList;
import java.util.List;

public class RecipeQueueUI {
    private static final float CARD_WIDTH = 35f;
    private static final float CARD_HEIGHT = 45f;
    private static final float CARD_SPACING = 5f;
    private static final float START_X = 8f;
    private static final float START_Y = 180f - CARD_HEIGHT - 8f; // top left

    private final List<RecipeCard> cards = new ArrayList<>();
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final Viewport uiViewport;
    private final RecipeQueue recipeQueue;
    private RecipeCard draggingCard = null;
    private final Vector2 tempVec = new Vector2();
    private final Skin skin;

    public RecipeQueueUI(Viewport uiViewport, RecipeQueue recipeQueue, Skin skin) {
        this.uiViewport = uiViewport;
        this.recipeQueue = recipeQueue;
        this.skin = skin;
        this.shapeRenderer = new ShapeRenderer();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ui/Pixellari.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 8;
        parameter.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest;
        parameter.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest;
        this.font = generator.generateFont(parameter);
        generator.dispose();
    }

    public void addOrder(String dishName, String customerId) {
        // new orders appear rightmost
        float x = START_X + cards.size() * (CARD_WIDTH + CARD_SPACING);
        cards.add(new RecipeCard(dishName, customerId, x, START_Y));
    }

    public void removeOrder(String customerId) {
        cards.removeIf(card -> card.customerId.equals(customerId));
        relayout();
    }

    public String getFirstDishName() {
        return cards.isEmpty() ? null : cards.get(0).dishName;
    }

    public void update(float delta) {
        // update timers
        for (RecipeCard card : cards) {
            if (!card.isDragging) {
                card.timer += delta;
            }
        }

        handleInput();
    }

    private void handleInput() {
        uiViewport.apply();

        // convert screen coords to UI coords
        tempVec.set(Gdx.input.getX(), Gdx.input.getY());
        uiViewport.unproject(tempVec);
        float mx = tempVec.x;
        float my = tempVec.y;

        if (Gdx.input.justTouched()) {
            // find which card was clicked, search in reverse (top card first)
            for (int i = cards.size() - 1; i >= 0; i--) {
                RecipeCard card = cards.get(i);
                if (card.contains(mx, my)) {
                    draggingCard = card;
                    card.isDragging = true;
                    card.dragOffsetX = mx - card.x;
                    break;
                }
            }
        }

        if (draggingCard != null) {
            if (Gdx.input.isTouched()) {
                // follow mouse horizontally only
                draggingCard.x = mx - draggingCard.dragOffsetX;
            } else {
                // released — insertion sort snap
                draggingCard.isDragging = false;
                insertionSort();
                relayout();
                draggingCard = null;
            }
        }
    }

    private void insertionSort() {
        // find where dragging card should be inserted based on its x position
        int dragIndex = cards.indexOf(draggingCard);
        int targetIndex = dragIndex;

        for (int i = 0; i < cards.size(); i++) {
            if (i == dragIndex) continue;
            RecipeCard other = cards.get(i);
            float otherCenter = other.x + other.width / 2f;
            float dragCenter = draggingCard.x + draggingCard.width / 2f;

            if (dragCenter < otherCenter && i < targetIndex) {
                targetIndex = i;
            } else if (dragCenter > otherCenter && i > targetIndex) {
                targetIndex = i;
            }
        }

        if (targetIndex != dragIndex) {
            cards.remove(dragIndex);
            cards.add(targetIndex, draggingCard);
        }
    }

    private void relayout() {
        for (int i = 0; i < cards.size(); i++) {
            RecipeCard card = cards.get(i);
            if (!card.isDragging) {
                card.x = START_X + i * (CARD_WIDTH + CARD_SPACING);
                card.y = START_Y;
            }
        }
    }

    public void draw(Batch batch) {
        uiViewport.apply();
        batch.setProjectionMatrix(uiViewport.getCamera().combined);

        shapeRenderer.setProjectionMatrix(uiViewport.getCamera().combined);

        for (RecipeCard card : cards) {
            // draw card background with ShapeRenderer
            batch.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            // urgency color — green to red based on timer
            float urgency = Math.min(card.timer / card.maxTime, 1f);
            shapeRenderer.setColor(urgency, 1f - urgency, 0f, 1f);
            shapeRenderer.rect(card.x, card.y, card.width, card.height);
            shapeRenderer.end();

            // draw card border
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(card.x, card.y, card.width, card.height);
            shapeRenderer.end();

            batch.begin();

            // draw dish name text
            font.setColor(Color.WHITE);
            font.draw(batch, card.dishName, card.x + 2f, card.y + CARD_HEIGHT - 2f);

            // draw timer text
            int secondsLeft = (int) (card.maxTime - card.timer);
            font.draw(batch, secondsLeft + "s", card.x + 2f, card.y + 8f);
        }
    }

    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
    }
}
