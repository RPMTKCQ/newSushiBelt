package com.sushi.game.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.sushi.game.system.PowerUpSystem;
import com.sushi.game.ui.model.PowerUpType;

public class GameScreenUI {

    private final Stage stage;
    private final Skin  skin;

    // ── labels updated at runtime ─────────────────────────────────────────────
    private Label moneyLabel;
    private Label scoreLabel;
    private Label levelLabel;
    private ProgressBar xpBar;

    // ── power-up overlay (shown on level up) ──────────────────────────────────
    private Table powerUpOverlay;

    public GameScreenUI(Stage stage, Skin skin) {
        this.stage = stage;
        this.skin  = skin;
        build();
    }

    // ── runtime setters called by LevelSystem ─────────────────────────────────

    public void setMoney(int money) {
        moneyLabel.setText("Money: " + money + "$");
    }

    public void setScore(int score) {
        scoreLabel.setText("Score: " + score);
    }

    public void setXp(int xp, int xpToNext, int level) {
        levelLabel.setText("Level: " + level + "/10");
        xpBar.setValue((float) xp / xpToNext * 100f);
    }

    // ── show / hide overlay ───────────────────────────────────────────────────

    public void showPowerUpOverlay(PowerUpSystem powerUpSystem) {
        powerUpOverlay.setVisible(true);
        powerUpOverlay.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
        powerUpOverlay.toFront();

        // re-wire listeners with fresh powerUpSystem reference each level-up
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

    // ── card hover + click ────────────────────────────────────────────────────

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

    // ── build ─────────────────────────────────────────────────────────────────

    private void build() {
        Table root = new Table();
        root.setName("GameScreen");
        root.setFillParent(true);

        // ── row 1: receipts + score/money/level ───────────────────────────────
        Table topBar = new Table();
        topBar.setName("TopBar");
        topBar.setColor(skin.getColor("sand"));
        topBar.padLeft(50f).padRight(50f).align(Align.left);

        topBar.add(buildReceiptRow()).growX().align(Align.left);
        topBar.add(buildScorePanel()).grow().align(Align.topRight);

        root.add(topBar).growX().align(Align.left).minHeight(280f);
        root.row();

        // ── row 2: power-up overlay (hidden by default) ───────────────────────
        powerUpOverlay = buildPowerUpOverlay();
        powerUpOverlay.setVisible(false);
        powerUpOverlay.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);

        Table midRow = new Table();
        midRow.add().grow();
        midRow.add(powerUpOverlay).growX();
        midRow.add().grow();
        root.add(midRow).growX();
        root.row();

        // ── row 3: inventory ──────────────────────────────────────────────────
        root.add(buildInventoryRow()).padBottom(40f).growX().align(Align.bottom).minHeight(350f);

        stage.addActor(root);
    }

    // ── receipt row ───────────────────────────────────────────────────────────

    private Table buildReceiptRow() {
        Table row = new Table();
        row.align(Align.left);
        row.add(buildReceiptCard("tuna-roll",      "Tuna Roll")).padRight(10f).growY().align(Align.top).minSize(160f, 200f).maxSize(160f, 200f);
        row.add(buildReceiptCard("tuna-roll",      "Tuna Roll")).padRight(10f).growY().align(Align.top).minSize(160f, 200f).maxSize(160f, 200f);
        row.add(buildReceiptCard("salmon-nigiri",  "Salmon Roll")).padRight(10f).growY().align(Align.top).minSize(160f, 200f).maxSize(160f, 200f);
        return row;
    }

    private Table buildReceiptCard(String imageName, String dishName) {
        Table card = new Table();
        card.setBackground(skin.getDrawable("rct-border"));
        card.align(Align.top);

        Image img = new Image(skin, imageName);
        img.setScaling(Scaling.fit);
        card.add(img).padTop(10f).minSize(80f);
        card.row();

        Label title = new Label(dishName, skin, "receipt");
        title.setColor(skin.getColor("black"));
        card.add(title).spaceTop(10f);
        card.row();

        ProgressBar bar = new ProgressBar(0f, 100f, 1f, false, skin);
        card.add(bar).padLeft(-50f).padRight(-50f).spaceTop(10f).maxWidth(100f);

        return card;
    }

    // ── score / money / level panel ───────────────────────────────────────────

    private Table buildScorePanel() {
        Table panel = new Table();
        panel.align(Align.topRight);

        // money + score row
        Table topRow = new Table();
        moneyLabel = new Label("Money: 100$", skin, "powerup");
        moneyLabel.setName("Money");
        scoreLabel = new Label("Score: 0", skin, "powerup");
        scoreLabel.setName("Score");
        topRow.add(moneyLabel).padRight(30f).spaceRight(20f);
        topRow.add(scoreLabel).padRight(30f);
        panel.add(topRow).padBottom(20f).align(Align.right);
        panel.row();

        // level + xp bar row
        Table levelRow = new Table();
        levelLabel = new Label("Level: 1/10", skin, "powerup");
        levelLabel.setName("LevelText");
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
        overlay.add(buildPowerUpCard(PowerUpType.COOKING_SPEED)) .spaceRight(100f).fill().align(Align.top).minSize(200f, 400f).maxSize(200f, 400f);
        overlay.add(buildPowerUpCard(PowerUpType.RUSH_HOUR))     .spaceRight(100f).fill().align(Align.top).minSize(200f, 400f).maxSize(200f, 400f);

        return overlay;
    }

    private Table buildPowerUpCard(PowerUpType type) {
        Table card = new Table();
        card.setName("PowerUp");
        card.setBackground(skin.getDrawable("rct-border"));
        card.align(Align.top);
        card.setUserObject(type);   // tag the type onto the actor for rewiring

        // icon placeholder
        Table iconBorder = new Table();
        iconBorder.setBackground(skin.getDrawable("fd-border"));
        iconBorder.add().minSize(30f).maxSize(30f);
        card.add(iconBorder).padTop(30f).spaceBottom(30f).minSize(80f);
        card.row();

        // title
        Label title = new Label(type.displayName(), skin, "powerup");
        title.setAlignment(Align.center);
        title.setWrap(true);
        title.setColor(skin.getColor("black"));
        card.add(title).spaceTop(10f).spaceBottom(20f).minSize(170f, 80f);
        card.row();

        // description
        Label desc = new Label(type.description(), skin, "receipt");
        desc.setAlignment(Align.center);
        desc.setWrap(true);
        desc.setColor(skin.getColor("black"));
        card.add(desc).spaceTop(20f).fillX().minSize(150f, 40f);

        return card;
    }

    // ── inventory row ─────────────────────────────────────────────────────────

    private Table buildInventoryRow() {
        Table row = new Table();
        row.align(Align.bottom);
        row.add().growX().align(Align.bottom);

        Table slots = new Table();
        slots.add(buildInventorySlot("maguro-nigiri")).spaceRight(7f).minSize(100f).maxSize(100f);
        slots.add(buildInventorySlot(null))           .spaceRight(7f).minSize(100f).maxSize(100f);
        slots.add(buildInventorySlot(null))           .spaceRight(7f).minSize(100f).maxSize(100f);
        row.add(slots).growX().align(Align.bottom);

        row.add().growX();
        return row;
    }

    private Table buildInventorySlot(String imageName) {
        Table slot = new Table();
        slot.setBackground(skin.getDrawable("fd-border"));
        slot.pad(5f);
        if (imageName != null) {
            Image img = new Image(skin, imageName);
            img.setScaling(Scaling.fit);
            slot.add(img).minSize(50f);
        }
        return slot;
    }
}
