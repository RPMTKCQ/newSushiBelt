package com.sushi.game.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
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
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import java.util.ArrayList;
import java.util.List;

public class GameScreenUI {

    private final Stage stage;
    private final Skin skin;
    private final AssetService assetService;

    // ── labels updated at runtime ─────────────────────────────────────────────
    private Label moneyLabel;
    private Label scoreLabel;
    private Label levelLabel;
    private Label strikesLabel;
    private ProgressBar xpBar;

    // ── power-up overlay ──────────────────────────────────────────────────────
    private Table powerUpOverlay;

    // ── inventory slots ───────────────────────────────────────────────────────
    private final Table[] inventorySlots = new Table[3];

    // ── receipt cards ─────────────────────────────────────────────────────────
    private Table receiptRow;
    private final List<ReceiptCardData> receiptCards = new ArrayList<>();

    private static class ReceiptCardData {
        Table card;
        Image dishImage;
        Label dishLabel;
        ProgressBar timerBar;
        Label secondsLabel;
        String customerId;
        float timer;
        float maxTime;

        ReceiptCardData(Table card, Image img, Label label,
                        ProgressBar bar, Label secondsLabel, String customerId, float maxTime) {
            this.card = card;
            this.dishImage = img;
            this.dishLabel = label;
            this.timerBar = bar;
            this.secondsLabel = secondsLabel;
            this.customerId = customerId;
            this.maxTime = maxTime;
        }
    }

    public GameScreenUI(Stage stage, Skin skin, AssetService assetService) {
        this.stage = stage;
        this.skin = skin;
        this.assetService = assetService;
        build();
    }

    // ── receipt API ───────────────────────────────────────────────────────────

    public void addOrder(String dishId, String customerId, float maxTime) {
        Table card = new Table();
        card.setBackground(skin.getDrawable("rct-border"));
        card.align(Align.top);

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
        Label secondsLabel = new Label("30s", skin, "receipt");
        secondsLabel.setColor(skin.getColor("black"));
        secondsLabel.setAlignment(Align.center);
        card.add(secondsLabel).spaceTop(4f);

        ReceiptCardData data = new ReceiptCardData(card, img, label, bar, secondsLabel, customerId, maxTime);
        receiptCards.add(data);
        attachDragListener(card, data);
        relayoutReceiptRow();
    }

    public void removeOrder(String customerId) {
        ReceiptCardData toRemove = null;
        for (ReceiptCardData d : receiptCards) {
            if (d.customerId.equals(customerId)) {
                toRemove = d;
                break;
            }
        }
        if (toRemove == null) return;
        receiptCards.remove(toRemove);
        toRemove.card.remove();
        relayoutReceiptRow();
    }

    public void removeFirstOrder() {
        if (receiptCards.isEmpty()) return;
        ReceiptCardData first = receiptCards.remove(0);
        first.card.remove();
        relayoutReceiptRow();
    }

    public String getFirstDishName() {
        if (receiptCards.isEmpty()) return null;
        return receiptCards.get(0).dishLabel.getText().toString()
            .toLowerCase().replace(" ", "_");
    }

    public void updateReceipts(float delta) {
        for (ReceiptCardData d : receiptCards) {
            d.timer += delta;
            float ratio = 1f - (d.timer / d.maxTime);
            d.timerBar.setValue(Math.max(ratio * 100f, 0f));

            // update seconds label
            int secondsLeft = Math.max(0, (int)(d.maxTime - d.timer));
            d.secondsLabel.setText(secondsLeft + "s");

            // color urgency — white → yellow → red
            if (ratio > 0.5f) {
                d.secondsLabel.setColor(skin.getColor("black"));
            } else if (ratio > 0.25f) {
                d.secondsLabel.setColor(com.badlogic.gdx.graphics.Color.ORANGE);
            } else {
                d.secondsLabel.setColor(com.badlogic.gdx.graphics.Color.RED);
            }
        }
    }

    // ── inventory API ─────────────────────────────────────────────────────────

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

    // ── score / level API ─────────────────────────────────────────────────────

    public void setMoney(int money) {
        moneyLabel.setText("Money: " + money + "$");
    }

    public void setScore(int score) {
        scoreLabel.setText("Score: " + score);
    }

    public void setStrikes(int s, int max) {
        strikesLabel.setText("Strikes: " + s + "/" + max);
    }

    public void setXp(int xp, int xpToNext, int level) {
        levelLabel.setText("Level: " + level + "/10");
        xpBar.setValue((float) xp / xpToNext * 100f);
    }

    // ── power-up overlay API ──────────────────────────────────────────────────

    public void showPowerUpOverlay(PowerUpSystem powerUpSystem) {
        powerUpOverlay.setVisible(true);
        powerUpOverlay.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
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
        powerUpOverlay.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
    }

    // ── build ─────────────────────────────────────────────────────────────────

    private void build() {
        Table root = new Table();
        root.setName("GameScreen");
        root.setFillParent(true);

        // row 1 — receipts + score panel
        Table topBar = new Table();
        topBar.setName("TopBar");
        topBar.setColor(skin.getColor("sand"));
        topBar.padLeft(50f).padRight(50f).align(Align.left);
        topBar.add(buildReceiptRow()).growX().align(Align.left);
        topBar.add(buildScorePanel()).grow().align(Align.topRight);
        root.add(topBar).growX().align(Align.left).minHeight(280f);
        root.row();

        // row 2 — power-up overlay (hidden by default)
        powerUpOverlay = buildPowerUpOverlay();
        powerUpOverlay.setVisible(false);
        powerUpOverlay.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        Table midRow = new Table();
        midRow.add().grow();
        midRow.add(powerUpOverlay).growX();
        midRow.add().grow();
        root.add(midRow).growX();
        root.row();

        // row 3 — inventory
        root.add(buildInventoryRow()).padBottom(40f).growX()
            .align(Align.bottom).minHeight(350f);

        stage.addActor(root);
    }

    // ── receipt row ───────────────────────────────────────────────────────────

    private Table buildReceiptRow() {
        receiptRow = new Table();
        receiptRow.align(Align.left);
        return receiptRow;
    }

    private void relayoutReceiptRow() {
        receiptRow.clearChildren();
        for (ReceiptCardData d : receiptCards) {
            receiptRow.add(d.card).padRight(10f).growY().align(Align.top)
                .minSize(160f, 200f).maxSize(160f, 200f);
        }
    }

    private void attachDragListener(Table card, ReceiptCardData data) {
        card.addListener(new DragListener() {
            @Override
            public void dragStart(InputEvent e, float x, float y, int ptr) {
                card.toFront();
                card.addAction(Actions.color(skin.getColor("sand"), 0.08f));
            }

            @Override
            public void drag(InputEvent e, float x, float y, int ptr) {
                card.moveBy(x - card.getWidth() / 2f, 0);

                int from = receiptCards.indexOf(data);
                int to = from;
                float cardCenterX = card.getX() + card.getWidth() / 2f;

                for (int i = 0; i < receiptCards.size(); i++) {
                    if (i == from) continue;
                    float otherCX = receiptCards.get(i).card.getX()
                        + receiptCards.get(i).card.getWidth() / 2f;
                    if (cardCenterX < otherCX && i < to) to = i;
                    else if (cardCenterX > otherCX && i > to) to = i;
                }

                if (to != from) {
                    receiptCards.remove(from);
                    receiptCards.add(to, data);
                    relayoutReceiptRow();
                }
            }

            @Override
            public void dragStop(InputEvent e, float x, float y, int ptr) {
                card.addAction(Actions.color(skin.getColor("white"), 0.08f));
                relayoutReceiptRow();
            }
        });
    }

    // ── score panel ───────────────────────────────────────────────────────────

    private Table buildScorePanel() {
        Table panel = new Table();
        panel.align(Align.topRight);

        Table topRow = new Table();
        strikesLabel = new Label("Strikes: 0/20", skin, "powerup");
        moneyLabel = new Label("Money: 100$", skin, "powerup");
        scoreLabel = new Label("Score: 0", skin, "powerup");
        topRow.add(strikesLabel).padRight(30f);
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

    // ── power-up overlay ──────────────────────────────────────────────────────

    private Table buildPowerUpOverlay() {
        Table overlay = new Table();
        overlay.add(buildPowerUpCard(PowerUpType.MOVEMENT_SPEED)).spaceRight(100f).fill().align(Align.top).minSize(200f, 400f).maxSize(200f, 400f);
        overlay.add(buildPowerUpCard(PowerUpType.COOKING_SPEED)).spaceRight(100f).fill().align(Align.top).minSize(200f, 400f).maxSize(200f, 400f);
        overlay.add(buildPowerUpCard(PowerUpType.RUSH_HOUR)).spaceRight(100f).fill().align(Align.top).minSize(200f, 400f).maxSize(200f, 400f);
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

    // ── inventory row ─────────────────────────────────────────────────────────

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

    // ── helpers ───────────────────────────────────────────────────────────────

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
