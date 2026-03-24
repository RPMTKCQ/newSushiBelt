package com.sushi.game.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.sushi.game.asset.AssetService;
import com.sushi.game.asset.AtlasAsset;
import com.sushi.game.system.PowerUpSystem;
import com.sushi.game.ui.model.PowerUpType;

import java.util.ArrayList;
import java.util.List;

public class GameScreenUI {

    private final Stage stage;
    private final Skin skin;
    private final AssetService assetService;

    private Label moneyLabel;
    private Label scoreLabel;
    private Label levelLabel;
    private Label satisfactionLabel;
    private ProgressBar xpBar;

    private Table powerUpOverlay;
    private final Table[] inventorySlots = new Table[3];
    private Table receiptRow;
    private final List<ReceiptCardData> receiptCards = new ArrayList<>();

    private boolean chefReady        = false;
    private boolean chefCooking      = false;
    private boolean lastSubmitSorted = false;
    private String cookingCustomerId = null;

    private static class ReceiptCardData {
        Table card;
        Image dishImage;
        Label dishLabel;
        ProgressBar timerBar;
        ProgressBar cookingBar;
        Label secondsLabel;
        String customerId;
        float timer;
        float maxTime;
        boolean cooked = false;

        ReceiptCardData(Table card, Image img, Label label,
                        ProgressBar bar, ProgressBar cookingBar,
                        Label secondsLabel, String customerId, float maxTime) {
            this.card         = card;
            this.dishImage    = img;
            this.dishLabel    = label;
            this.timerBar     = bar;
            this.cookingBar   = cookingBar;
            this.secondsLabel = secondsLabel;
            this.customerId   = customerId;
            this.maxTime      = maxTime;
        }
    }

    public GameScreenUI(Stage stage, Skin skin, AssetService assetService) {
        this.stage        = stage;
        this.skin         = skin;
        this.assetService = assetService;
        build();
    }

    public boolean isQueueSortedByUrgency() {
        List<ReceiptCardData> active = new ArrayList<>();
        for (ReceiptCardData d : receiptCards) {
            if (!d.cooked && (cookingCustomerId == null || !d.customerId.equals(cookingCustomerId))) active.add(d);
        }
        for (int i = 0; i < active.size() - 1; i++) {
            float timeRemainingA = active.get(i).maxTime   - active.get(i).timer;
            float timeRemainingB = active.get(i + 1).maxTime - active.get(i + 1).timer;
            if (timeRemainingA > timeRemainingB) return false;
        }
        return true;
    }

    public void setLastSubmitSorted(boolean sorted) { this.lastSubmitSorted = sorted; }
    public boolean wasLastSubmitSorted()            { return lastSubmitSorted; }

    public boolean isChefReady()                { return chefReady; }
    public void setChefCooking(boolean cooking) { this.chefCooking = cooking; }
    public boolean isChefCooking()              { return chefCooking; }

    public void setChefReady(boolean ready) {
        this.chefReady = ready;
        if (!ready) cookingCustomerId = null;
    }

    public void startCookingFor(String customerId) {
        this.cookingCustomerId = customerId;
        ReceiptCardData d = getCookingCard();
        if (d != null) {
            d.card.setTouchable(Touchable.disabled);
            d.card.clearActions();
            d.card.addAction(Actions.color(new com.badlogic.gdx.graphics.Color(1f, 0.65f, 0.1f, 1f), 0.2f));
        }
    }

    private ReceiptCardData getCookingCard() {
        if (cookingCustomerId == null) return null;
        for (ReceiptCardData d : receiptCards) {
            if (d.customerId.equals(cookingCustomerId)) return d;
        }
        return null;
    }

    public void updateCookingProgress(float ratio) {
        ReceiptCardData d = getCookingCard();
        if (d == null) return;
        d.cookingBar.setValue(ratio * 100f);
        d.cookingBar.setVisible(true);
    }

    public void resetCookingBar() {
        ReceiptCardData d = getCookingCard();
        if (d == null) return;
        d.cookingBar.setValue(0f);
        d.cookingBar.setVisible(false);
        d.cooked = true;

        d.card.setTouchable(Touchable.disabled);
        d.card.clearActions();
        d.card.addAction(Actions.color(new com.badlogic.gdx.graphics.Color(1f, 0.95f, 0.2f, 1f), 0.2f));

        cookingCustomerId = null;
    }

    public void onOrderDelivered(String customerId) {
        for (ReceiptCardData d : receiptCards) {
            if (d.customerId.equals(customerId)) {
                d.card.clearActions();
                d.card.addAction(Actions.color(skin.getColor("white"), 0.15f));
                return;
            }
        }
    }

    public void addOrder(String dishId, String customerId, float maxTime) {
        Table card = new Table();
        card.setBackground(skin.getDrawable("rct-border"));
        card.align(Align.top);
        card.setTouchable(Touchable.enabled);

        Image img = new Image(assetService.get(AtlasAsset.OBJECTS)
            .findRegion("Food/" + dishId.replace("_", "-")));
        img.setScaling(Scaling.fit);
        card.add(img).padTop(10f).minSize(80f);
        card.row();

        Label label = new Label(formatDishName(dishId), skin, "receipt");
        label.setColor(skin.getColor("black"));
        card.add(label).spaceTop(10f);
        card.row();

        ProgressBar bar = new ProgressBar(0f, 100f, 1f, false, skin);
        bar.setValue(100f);
        card.add(bar).padLeft(-50f).padRight(-50f).spaceTop(10f).maxWidth(100f);
        card.row();

        ProgressBar cookingBar = new ProgressBar(0f, 100f, 1f, false, skin);
        cookingBar.setValue(0f);
        cookingBar.setVisible(false);
        card.add(cookingBar).padLeft(-50f).padRight(-50f).spaceTop(4f).maxWidth(100f);
        card.row();

        Label secondsLabel = new Label("30s", skin, "receipt");
        secondsLabel.setColor(skin.getColor("black"));
        secondsLabel.setAlignment(Align.center);
        card.add(secondsLabel).spaceTop(4f);

        ReceiptCardData data = new ReceiptCardData(card, img, label, bar,
            cookingBar, secondsLabel, customerId, maxTime);
        receiptCards.add(data);
        attachDragListener(card, data);
        relayoutReceiptRow();
    }

    public void removeOrder(String customerId) {
        ReceiptCardData toRemove = null;
        for (ReceiptCardData d : receiptCards) {
            if (d.customerId.equals(customerId)) { toRemove = d; break; }
        }
        if (toRemove == null) return;
        receiptCards.remove(toRemove);
        toRemove.card.remove();
        relayoutReceiptRow();
    }

    public String getFirstDishName() {
        for (ReceiptCardData d : receiptCards) {
            if (!d.cooked && (cookingCustomerId == null || !d.customerId.equals(cookingCustomerId))) {
                return d.dishLabel.getText().toString().toLowerCase().replace(" ", "_");
            }
        }
        return null;
    }

    public String getFirstCustomerId() {
        for (ReceiptCardData d : receiptCards) {
            if (!d.cooked && (cookingCustomerId == null || !d.customerId.equals(cookingCustomerId))) {
                return d.customerId;
            }
        }
        return null;
    }

    public void updateReceipts(float delta) {
        for (ReceiptCardData d : receiptCards) {
            d.timer += delta;
            float ratio = 1f - (d.timer / d.maxTime);
            d.timerBar.setValue(Math.max(ratio * 100f, 0f));

            int secondsLeft = Math.max(0, (int)(d.maxTime - d.timer));
            d.secondsLabel.setText(secondsLeft + "s");

            if (ratio > 0.5f)
                d.secondsLabel.setColor(skin.getColor("black"));
            else if (ratio > 0.25f)
                d.secondsLabel.setColor(com.badlogic.gdx.graphics.Color.ORANGE);
            else
                d.secondsLabel.setColor(com.badlogic.gdx.graphics.Color.RED);
        }
    }

    public void updateInventory(List<String> dishes) {
        for (int i = 0; i < 3; i++) {
            Table slot = inventorySlots[i];
            slot.clearChildren();
            if (i < dishes.size()) {
                String dishId = dishes.get(i);
                Image img = new Image(assetService.get(AtlasAsset.OBJECTS)
                    .findRegion("Food/" + dishId.replace("_", "-")));
                img.setScaling(Scaling.fit);
                slot.add(img).minSize(50f);
            }
        }
    }

    public void setMoney(int money)              { moneyLabel.setText("Money: " + money + "$"); }
    public void setScore(int score)              { scoreLabel.setText("Score: " + score); }
    public void setSatisfaction(float pct)       { satisfactionLabel.setText(String.format("Satisfaction: %.0f%%", pct)); }

    public void setXp(int xp, int xpToNext, int level) {
        levelLabel.setText("Level: " + level + "/10");
        xpBar.setValue((float) xp / xpToNext * 100f);
    }

    public void showPowerUpOverlay(PowerUpSystem powerUpSystem) {
        powerUpOverlay.setVisible(true);
        powerUpOverlay.setTouchable(Touchable.enabled);
        powerUpOverlay.toFront();

        for (Actor actor : powerUpOverlay.getChildren()) {
            if (actor instanceof Table card) {
                PowerUpType type = (PowerUpType) card.getUserObject();
                if (type == null) continue;
                card.clearListeners();
                addCardListeners(card, type, powerUpSystem);
            }
        }
    }

    private void hideOverlay() {
        powerUpOverlay.setVisible(false);
        powerUpOverlay.setTouchable(Touchable.disabled);
    }

    private void build() {
        Table root = new Table();
        root.setName("GameScreen");
        root.setFillParent(true);
        root.setTouchable(Touchable.childrenOnly);

        Table topBar = new Table();
        topBar.setName("TopBar");
        topBar.setColor(skin.getColor("sand"));
        topBar.setTouchable(Touchable.childrenOnly);
        topBar.padLeft(50f).padRight(50f).align(Align.left);
        topBar.add(buildReceiptRow()).growX().align(Align.left);
        topBar.add(buildScorePanel()).grow().align(Align.topRight);
        root.add(topBar).growX().align(Align.left).minHeight(280f);
        root.row();

        powerUpOverlay = buildPowerUpOverlay();
        powerUpOverlay.setVisible(false);
        powerUpOverlay.setTouchable(Touchable.disabled);
        Table midRow = new Table();
        midRow.add().grow();
        midRow.add(powerUpOverlay).growX();
        midRow.add().grow();
        root.add(midRow).growX();
        root.row();

        root.add(buildInventoryRow()).padBottom(40f).growX()
            .align(Align.bottom).minHeight(350f);

        stage.addActor(root);
    }

    private Table buildReceiptRow() {
        receiptRow = new Table();
        receiptRow.align(Align.left);
        receiptRow.setTouchable(Touchable.childrenOnly);
        return receiptRow;
    }

    private void relayoutReceiptRow() {
        receiptRow.clearChildren();
        for (ReceiptCardData d : receiptCards) {
            receiptRow.add(d.card).padRight(10f).growY().align(Align.top)
                .minSize(160f, 220f).maxSize(160f, 220f);
        }
    }

    private void attachDragListener(Table card, ReceiptCardData data) {
        card.addListener(new DragListener() {
            {
                setTapSquareSize(4f);
            }

            @Override
            public void dragStart(InputEvent e, float x, float y, int ptr) {
                if ((cookingCustomerId != null && data.customerId.equals(cookingCustomerId)) || data.cooked) {
                    cancel();
                    return;
                }
                card.toFront();
                card.clearActions();
                card.addAction(Actions.color(skin.getColor("sand"), 0.08f));
            }

            @Override
            public void drag(InputEvent e, float x, float y, int ptr) {
                if ((cookingCustomerId != null && data.customerId.equals(cookingCustomerId)) || data.cooked) return;

                card.moveBy(x - card.getWidth() / 2f, 0);

                int from = receiptCards.indexOf(data);
                int to = from;
                float cardCX = card.getX() + card.getWidth() / 2f;

                int lockedCount = 0;
                for (ReceiptCardData d : receiptCards) {
                    if (d.cooked || (cookingCustomerId != null && d.customerId.equals(cookingCustomerId))) {
                        lockedCount++;
                    }
                }

                for (int i = lockedCount; i < receiptCards.size(); i++) {
                    if (i == from) continue;

                    float otherCX = receiptCards.get(i).card.getX() + receiptCards.get(i).card.getWidth() / 2f;
                    if (cardCX < otherCX && i < to) to = i;
                    else if (cardCX > otherCX && i > to) to = i;
                }

                // Prevent dragging into the locked wall territory entirely
                to = Math.max(lockedCount, Math.min(to, receiptCards.size() - 1));

                if (to != from) {
                    receiptCards.remove(from);
                    receiptCards.add(to, data);
                    relayoutReceiptRow();
                }
            }

            @Override
            public void dragStop(InputEvent e, float x, float y, int ptr) {
                if ((cookingCustomerId != null && data.customerId.equals(cookingCustomerId)) || data.cooked) return;
                card.clearActions();
                card.addAction(Actions.color(skin.getColor("white"), 0.08f));
                relayoutReceiptRow();
            }
        });
    }

    private Table buildScorePanel() {
        Table panel = new Table();
        panel.align(Align.topRight);

        Table topRow = new Table();
        satisfactionLabel = new Label("Satisfaction: 100%", skin, "powerup");
        moneyLabel   = new Label("Money: 100$",   skin, "powerup");
        scoreLabel   = new Label("Score: 0",       skin, "powerup");
        topRow.add(satisfactionLabel).padRight(30f);
        topRow.add(moneyLabel).padRight(30f).spaceRight(20f);
        topRow.add(scoreLabel).padRight(30f);
        panel.add(topRow).padBottom(20f).align(Align.right);
        panel.row();

        Table levelRow = new Table();
        levelLabel = new Label("Level: 1/10", skin, "powerup");
        xpBar = new ProgressBar(0f, 100f, 1f, false, skin, "customerSatisfaction");
        levelRow.add(levelLabel).spaceRight(30f);
        levelRow.add(xpBar).expandX().minHeight(20f);
        panel.add(levelRow).expandX().align(Align.right);

        return panel;
    }

    private Table buildPowerUpOverlay() {
        Table overlay = new Table();
        overlay.add(buildPowerUpCard(PowerUpType.MOVEMENT_SPEED)).spaceRight(100f).fill().align(Align.top).minSize(200f, 400f).maxSize(200f, 400f);
        overlay.add(buildPowerUpCard(PowerUpType.COOKING_SPEED)) .spaceRight(100f).fill().align(Align.top).minSize(200f, 400f).maxSize(200f, 400f);
        overlay.add(buildPowerUpCard(PowerUpType.RUSH_HOUR))     .spaceRight(100f).fill().align(Align.top).minSize(200f, 400f).maxSize(200f, 400f);
        return overlay;
    }

    private Table buildPowerUpCard(PowerUpType type) {
        Table card = new Table();
        card.setBackground(skin.getDrawable("rct-border"));
        card.align(Align.top);
        card.setUserObject(type);

        Table iconBorder = new Table();
        iconBorder.setBackground(skin.getDrawable("fd-border"));
        iconBorder.add().minSize(30f).maxSize(30f);
        card.add(iconBorder).padTop(30f).spaceBottom(30f).minSize(80f);
        card.row();

        Label title = new Label(type.displayName(), skin, "powerup");
        title.setAlignment(Align.center);
        title.setWrap(true);
        title.setColor(skin.getColor("black"));
        card.add(title).spaceTop(10f).spaceBottom(20f).minSize(170f, 80f);
        card.row();

        Label desc = new Label(type.description(), skin, "receipt");
        desc.setAlignment(Align.center);
        desc.setWrap(true);
        desc.setColor(skin.getColor("black"));
        card.add(desc).spaceTop(20f).fillX().minSize(150f, 40f);

        return card;
    }

    private void addCardListeners(Table card, PowerUpType type, PowerUpSystem powerUpSystem) {
        card.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                card.addAction(Actions.color(skin.getColor("sand"), 0.1f));
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                card.addAction(Actions.color(skin.getColor("white"), 0.1f));
            }
        });
        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                powerUpSystem.applyPowerUp(type);
                hideOverlay();
            }
        });
    }

    private Table buildInventoryRow() {
        Table row = new Table();
        row.align(Align.bottom);
        row.add().growX().align(Align.bottom);

        Table slots = new Table();
        for (int i = 0; i < 3; i++) {
            inventorySlots[i] = buildInventorySlot();
            slots.add(inventorySlots[i]).spaceRight(7f).minSize(100f).maxSize(100f);
        }
        row.add(slots).growX().align(Align.bottom);
        row.add().growX();
        return row;
    }

    private Table buildInventorySlot() {
        Table slot = new Table();
        slot.setBackground(skin.getDrawable("fd-border"));
        slot.pad(5f);
        return slot;
    }

    private String formatDishName(String dishId) {
        String[] parts = dishId.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            sb.append(Character.toUpperCase(p.charAt(0)))
                .append(p.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }
}
