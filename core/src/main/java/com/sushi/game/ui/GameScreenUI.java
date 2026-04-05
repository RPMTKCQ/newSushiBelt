package com.sushi.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
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
import com.sushi.game.asset.SoundAsset;
import com.sushi.game.audio.AudioService;
import com.sushi.game.system.PowerUpSystem;
import com.sushi.game.ui.model.PowerUpType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GameScreenUI {

    private final Stage stage;
    private final Skin skin;
    private final AssetService assetService;
    private final AudioService audioService; // GLOBAL ACCESS TO SOUNDS

    private Label timerLabel;
    private Label moneyLabel;
    private Label scoreLabel;
    private Label levelLabel;
    private Label satisfactionLabel;
    private ProgressBar xpBar;

    private Table pauseOverlay;
    private Table powerUpOverlay;
    private Table eventLogTable;
    private final Table[] inventorySlots = new Table[3];
    private Table receiptRow;
    private final List<ReceiptCardData> receiptCards = new ArrayList<>();

    private boolean chefReady = false;
    private boolean chefCooking = false;
    private boolean lastSubmitSorted = false;
    private String cookingCustomerId = null;

    private ReceiptCardData currentlyDraggingCard = null;

    // Pause Menu Variables
    private Group pauseSelectedItem;
    private final List<Group> pauseMenuItems = new ArrayList<>();
    private Image pauseSelectionImg;
    private boolean inputLocked = false;

    // Discrete Slider Variables
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private float holdTimer = 0f;
    private static final float REPEAT_RATE = 0.15f;

    private enum PauseState {MAIN, CONFIRM_QUIT}

    private PauseState currentPauseState = PauseState.MAIN;

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
            this.card = card;
            this.dishImage = img;
            this.dishLabel = label;
            this.timerBar = bar;
            this.cookingBar = cookingBar;
            this.secondsLabel = secondsLabel;
            this.customerId = customerId;
            this.maxTime = maxTime;
        }
    }

    public GameScreenUI(Stage stage, Skin skin, AssetService assetService, AudioService audioService) {
        this.stage = stage;
        this.skin = skin;
        this.assetService = assetService;
        this.audioService = audioService; // Set audio context immediately!
        build();
    }

    public void addLogEvent(String message, com.badlogic.gdx.graphics.Color color) {
        Label logLabel = new Label(message, skin, "receipt");
        logLabel.setColor(color);
        eventLogTable.add(logLabel).align(Align.right).padBottom(5f).row();

        logLabel.addAction(Actions.sequence(
            Actions.alpha(0f),
            Actions.fadeIn(0.2f),
            Actions.delay(4f),
            Actions.fadeOut(1f),
            Actions.removeActor()
        ));
    }

    public boolean isQueueSortedByUrgency() {
        List<ReceiptCardData> active = new ArrayList<>();
        for (ReceiptCardData d : receiptCards) {
            if (!d.cooked && !d.customerId.equals(cookingCustomerId)) active.add(d);
        }
        for (int i = 0; i < active.size() - 1; i++) {
            float timeRemainingA = active.get(i).maxTime - active.get(i).timer;
            float timeRemainingB = active.get(i + 1).maxTime - active.get(i + 1).timer;
            if (timeRemainingA > timeRemainingB) return false;
        }
        return true;
    }

    public void setLastSubmitSorted(boolean sorted) {
        this.lastSubmitSorted = sorted;
    }

    public boolean wasLastSubmitSorted() {
        return lastSubmitSorted;
    }

    public boolean isChefReady() {
        return chefReady;
    }

    public void setChefCooking(boolean cooking) {
        this.chefCooking = cooking;
    }

    public boolean isChefCooking() {
        return chefCooking;
    }

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
            if (d.customerId.equals(customerId)) {
                toRemove = d;
                break;
            }
        }
        if (toRemove == null) return;

        if (!toRemove.cooked) {
            for (ReceiptCardData d : receiptCards) {
                if (d != toRemove && d.cooked && d.dishLabel.getText().toString().equals(toRemove.dishLabel.getText().toString())) {
                    d.cooked = false;
                    d.card.setTouchable(Touchable.enabled);
                    d.card.clearActions();
                    d.card.addAction(Actions.color(skin.getColor("white"), 0.2f));
                    break;
                }
            }
        }

        receiptCards.remove(toRemove);
        toRemove.card.remove();

        if (currentlyDraggingCard == toRemove) {
            currentlyDraggingCard = null;
        }

        relayoutReceiptRow();
    }

    public String getFirstDishName() {
        for (ReceiptCardData d : receiptCards) {
            if (!d.cooked && !d.customerId.equals(cookingCustomerId)) {
                return d.dishLabel.getText().toString().toLowerCase().replace(" ", "_");
            }
        }
        return null;
    }

    public String getFirstCustomerId() {
        for (ReceiptCardData d : receiptCards) {
            if (!d.cooked && !d.customerId.equals(cookingCustomerId)) {
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

            int secondsLeft = Math.max(0, (int) (d.maxTime - d.timer));
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

    public void updateTimer(String timeText) {
        if (timerLabel != null) timerLabel.setText(timeText);
    }

    public void setMoney(int money) {
        moneyLabel.setText("Money: " + money + "$");
    }

    public void setScore(int score) {
        scoreLabel.setText("Score: " + score);
    }

    public void setSatisfaction(float ratio) {
        satisfactionLabel.setText(String.format("Satisfaction: %.0f%%", ratio * 100));
    }

    public void setXp(int xp, int xpToNext, int level) {
        levelLabel.setText("Level: " + level + "/10");
        xpBar.setValue((float) xp / xpToNext * 100f);
    }

    public void showPowerUpOverlay(PowerUpSystem powerUpSystem) {
        audioService.playSound(SoundAsset.LEVEL_UP); // FIX: LEVEL UP SOUND HOOK

        powerUpOverlay.clearChildren();

        List<PowerUpType> allTypes = new ArrayList<>(Arrays.asList(PowerUpType.values()));
        Collections.shuffle(allTypes);

        powerUpOverlay.add(buildPowerUpCard(allTypes.get(0))).spaceRight(100f).fill().align(Align.top).minSize(200f, 400f).maxSize(200f, 400f);
        powerUpOverlay.add(buildPowerUpCard(allTypes.get(1))).spaceRight(100f).fill().align(Align.top).minSize(200f, 400f).maxSize(200f, 400f);
        powerUpOverlay.add(buildPowerUpCard(allTypes.get(2))).spaceRight(100f).fill().align(Align.top).minSize(200f, 400f).maxSize(200f, 400f);

        for (Actor actor : powerUpOverlay.getChildren()) {
            if (actor instanceof Table card) {
                PowerUpType type = (PowerUpType) card.getUserObject();
                if (type == null) continue;
                card.clearListeners();
                addCardListeners(card, type, powerUpSystem);
            }
        }

        powerUpOverlay.setVisible(true);
        powerUpOverlay.setTouchable(Touchable.enabled);
        powerUpOverlay.toFront();
    }

    public void hideRushHourIfDepleted() {
        Gdx.app.log("UI", "Rush Hour ended.");
    }

    private void hideOverlay() {
        powerUpOverlay.setVisible(false);
        powerUpOverlay.setTouchable(Touchable.disabled);
    }

    public void updatePauseMenu(float delta) {
        if (leftPressed || rightPressed) {
            holdTimer += delta;
            while (holdTimer >= REPEAT_RATE) {
                holdTimer -= REPEAT_RATE;
                adjustPauseSlider(leftPressed ? -0.1f : 0.1f);
            }
        }
    }

    public void togglePauseOverlay(boolean isPaused, Runnable onResume, Runnable onQuit) {
        if (pauseOverlay == null) {
            pauseOverlay = new Table();
            pauseOverlay.setFillParent(true);

            pauseSelectionImg = new Image(skin, "selection-2");
            pauseSelectionImg.setTouchable(Touchable.disabled);

            pauseOverlay.addListener(new InputListener() {
                @Override
                public boolean keyDown(InputEvent event, int keycode) {
                    if (!isPaused) return false;

                    if (inputLocked) {
                        return true;
                    }

                    if (keycode == Input.Keys.SHIFT_LEFT || keycode == Input.Keys.SHIFT_RIGHT || keycode == Input.Keys.ESCAPE) {
                        if (currentPauseState == PauseState.CONFIRM_QUIT) {
                            audioService.playSound(SoundAsset.MENU_BACK);
                            buildMainPauseScreen(onResume, onQuit);
                        } else {
                            audioService.playSound(SoundAsset.MENU_BACK);
                            executeSafeAction(onResume);
                        }
                        return true;
                    }

                    if (keycode == Input.Keys.SPACE || keycode == Input.Keys.ENTER) {
                        if (pauseSelectedItem != null && pauseSelectedItem.getUserObject() instanceof Runnable action) {
                            executeSafeAction(action);
                            return true;
                        }
                    }

                    if (pauseMenuItems.isEmpty()) return false;
                    int currentIndex = pauseMenuItems.indexOf(pauseSelectedItem);
                    int newIndex = currentIndex;

                    if (currentPauseState == PauseState.CONFIRM_QUIT) {
                        if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
                            newIndex = (currentIndex - 1 + pauseMenuItems.size()) % pauseMenuItems.size();
                        } else if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
                            newIndex = (currentIndex + 1) % pauseMenuItems.size();
                        }
                    } else {
                        if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
                            newIndex = (currentIndex - 1 + pauseMenuItems.size()) % pauseMenuItems.size();
                        } else if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
                            newIndex = (currentIndex + 1) % pauseMenuItems.size();
                        } else if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
                            leftPressed = true;
                            holdTimer = 0f;
                            adjustPauseSlider(-0.1f);
                        } else if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
                            rightPressed = true;
                            holdTimer = 0f;
                            adjustPauseSlider(0.1f);
                        }
                    }

                    if (newIndex != currentIndex && newIndex >= 0 && newIndex < pauseMenuItems.size()) {
                        selectPauseItem(pauseMenuItems.get(newIndex));
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean keyUp(InputEvent event, int keycode) {
                    if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) leftPressed = false;
                    if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) rightPressed = false;
                    return false;
                }

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (!isPaused || inputLocked) return false;

                    if (button == Input.Buttons.RIGHT) {
                        if (currentPauseState == PauseState.CONFIRM_QUIT) {
                            audioService.playSound(SoundAsset.MENU_BACK);
                            buildMainPauseScreen(onResume, onQuit);
                        } else {
                            audioService.playSound(SoundAsset.MENU_BACK);
                            executeSafeAction(onResume);
                        }
                        return true;
                    }
                    return false;
                }
            });

            stage.addActor(pauseOverlay);
        }

        pauseOverlay.setVisible(isPaused);

        if (isPaused) {
            inputLocked = false;
            pauseOverlay.toFront();
            stage.setKeyboardFocus(pauseOverlay);
            buildMainPauseScreen(onResume, onQuit);
        } else {
            stage.setKeyboardFocus(null);
        }
    }

    private void executeSafeAction(Runnable action) {
        inputLocked = true;
        Gdx.app.postRunnable(() -> {
            try {
                action.run();
            } catch (Exception e) {
                Gdx.app.error("GameScreenUI", "Error executing pause action", e);
            } finally {
                inputLocked = false;
            }
        });
    }

    private void buildMainPauseScreen(Runnable onResume, Runnable onQuit) {
        currentPauseState = PauseState.MAIN;
        pauseOverlay.clearChildren();
        pauseMenuItems.clear();

        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(new com.badlogic.gdx.graphics.Color(0f, 0f, 0f, 0.75f));
        pixmap.fill();
        com.badlogic.gdx.graphics.Texture tex = new com.badlogic.gdx.graphics.Texture(pixmap);

        Image darkBg = new Image(tex);
        darkBg.setSize(20000f, 20000f);
        darkBg.setPosition(-10000f, -10000f);
        pauseOverlay.addActor(darkBg);
        darkBg.toBack();

        Label title = new Label("GAME PAUSED", skin, "title");
        title.setAlignment(Align.center);
        pauseOverlay.add(title).padBottom(40f).row();

        TextButton resumeBtn = new TextButton("Resume", skin);
        resumeBtn.setColor(skin.getColor("white"));
        resumeBtn.setUserObject((Runnable) onResume);
        resumeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audioService.playSound(SoundAsset.MENU_SELECT);
                executeSafeAction(onResume);
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                selectPauseItem(resumeBtn);
            }
        });
        pauseMenuItems.add(resumeBtn);
        pauseOverlay.add(resumeBtn).minSize(300f, 80f).padBottom(20f).row();

        Table musicTable = new Table();
        Label musicLabel = new Label("Music Volume", skin, "default");
        musicLabel.setColor(skin.getColor("white"));
        musicTable.add(musicLabel).padBottom(5f).row();

        Slider musicSlider = new Slider(0f, 1f, 0.1f, false, skin);
        musicSlider.setValue(audioService.getMusicVolume());
        musicSlider.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioService.setMusicVolume(musicSlider.getValue());
                audioService.playSound(SoundAsset.MENU_HOVER);
            }
        });
        musicTable.add(musicSlider).width(290f);
        musicTable.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                selectPauseItem(musicTable);
            }
        });
        musicTable.setUserObject(musicSlider);
        pauseMenuItems.add(musicTable);
        pauseOverlay.add(musicTable).padBottom(20f).row();

        Table soundTable = new Table();
        Label soundLabel = new Label("Sound Volume", skin, "default");
        soundLabel.setColor(skin.getColor("white"));
        soundTable.add(soundLabel).padBottom(5f).row();

        Slider soundSlider = new Slider(0f, 1f, 0.1f, false, skin);
        soundSlider.setValue(audioService.getSoundVolume());
        soundSlider.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioService.setSoundVolume(soundSlider.getValue());
                audioService.playSound(SoundAsset.MENU_HOVER);
            }
        });
        soundTable.add(soundSlider).width(290f);
        soundTable.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                selectPauseItem(soundTable);
            }
        });
        soundTable.setUserObject(soundSlider);
        pauseMenuItems.add(soundTable);
        pauseOverlay.add(soundTable).padBottom(40f).row();

        TextButton quitBtn = new TextButton("Quit", skin);
        quitBtn.setColor(skin.getColor("white"));
        Runnable confirmQuitAction = () -> buildConfirmQuitScreen(onResume, onQuit);
        quitBtn.setUserObject(confirmQuitAction);
        quitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audioService.playSound(SoundAsset.MENU_SELECT);
                executeSafeAction(confirmQuitAction);
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                selectPauseItem(quitBtn);
            }
        });
        pauseMenuItems.add(quitBtn);
        pauseOverlay.add(quitBtn).minSize(300f, 80f);

        pauseOverlay.pack();
        if (!pauseMenuItems.isEmpty()) selectPauseItem(pauseMenuItems.get(0));
    }

    private void buildConfirmQuitScreen(Runnable onResume, Runnable onQuit) {
        currentPauseState = PauseState.CONFIRM_QUIT;
        pauseOverlay.clearChildren();
        pauseMenuItems.clear();

        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(new com.badlogic.gdx.graphics.Color(0f, 0f, 0f, 0.75f));
        pixmap.fill();
        com.badlogic.gdx.graphics.Texture tex = new com.badlogic.gdx.graphics.Texture(pixmap);

        Image darkBg = new Image(tex);
        darkBg.setSize(20000f, 20000f);
        darkBg.setPosition(-10000f, -10000f);
        pauseOverlay.addActor(darkBg);
        darkBg.toBack();

        Label title = new Label("Quit to Menu?", skin, "title");
        title.setAlignment(Align.center);
        pauseOverlay.add(title).padBottom(40f).row();

        Label subTitle = new Label("Progress will be lost", skin, "default");
        subTitle.setColor(skin.getColor("white"));
        subTitle.setAlignment(Align.center);
        pauseOverlay.add(subTitle).padBottom(40f).row();

        Table buttonsTable = new Table();

        TextButton yesBtn = new TextButton("Yes", skin);
        yesBtn.setColor(skin.getColor("white"));
        yesBtn.setUserObject((Runnable) onQuit);
        yesBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audioService.playSound(SoundAsset.MENU_SELECT);
                executeSafeAction(onQuit);
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                selectPauseItem(yesBtn);
            }
        });
        pauseMenuItems.add(yesBtn);
        buttonsTable.add(yesBtn).minSize(150f, 60f).padRight(30f);

        TextButton noBtn = new TextButton("No", skin);
        noBtn.setColor(skin.getColor("white"));
        Runnable noAction = () -> buildMainPauseScreen(onResume, onQuit);
        noBtn.setUserObject(noAction);
        noBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audioService.playSound(SoundAsset.MENU_BACK);
                executeSafeAction(noAction);
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                selectPauseItem(noBtn);
            }
        });
        pauseMenuItems.add(noBtn);
        buttonsTable.add(noBtn).minSize(150f, 60f);

        pauseOverlay.add(buttonsTable);
        pauseOverlay.pack();

        if (!pauseMenuItems.isEmpty()) selectPauseItem(pauseMenuItems.get(1));
    }

    private void adjustPauseSlider(float amount) {
        if (pauseSelectedItem != null && pauseSelectedItem.getUserObject() instanceof Slider slider) {
            slider.setValue(slider.getValue() + amount);
        }
    }

    private void selectPauseItem(Group group) {
        if (this.pauseSelectedItem == group) return;

        if (this.pauseSelectedItem instanceof Button oldButton) {
            oldButton.setChecked(false);
        }

        if (pauseSelectionImg.getParent() != null) {
            pauseSelectionImg.getParent().removeActor(pauseSelectionImg);
        }

        if (this.pauseSelectedItem != null) {
            audioService.playSound(SoundAsset.MENU_HOVER);
        }

        this.pauseSelectedItem = group;

        if (this.pauseSelectedItem instanceof Button newButton) {
            newButton.setChecked(true);
        }

        group.addActor(pauseSelectionImg);
        pauseSelectionImg.toBack();

        float extraSize = 5f;
        float halfExtra = extraSize * 0.5f;
        float resizeTime = 0.365f;

        pauseSelectionImg.setSize(group.getWidth() + extraSize, group.getHeight() + extraSize);
        pauseSelectionImg.setPosition(-halfExtra, -halfExtra);

        pauseSelectionImg.clearActions();
        pauseSelectionImg.addAction(Actions.forever(Actions.sequence(
            Actions.parallel(
                Actions.sizeBy(extraSize, extraSize, resizeTime, Interpolation.linear),
                Actions.moveBy(-halfExtra, -halfExtra, resizeTime, Interpolation.linear)
            ),
            Actions.parallel(
                Actions.sizeBy(-extraSize, -extraSize, resizeTime, Interpolation.linear),
                Actions.moveBy(halfExtra, halfExtra, resizeTime, Interpolation.linear)
            )
        )));
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
        root.add(topBar).growX().align(Align.top).minHeight(280f);
        root.row();

        powerUpOverlay = new Table();
        powerUpOverlay.setVisible(false);
        powerUpOverlay.setTouchable(Touchable.disabled);
        Table midRow = new Table();
        midRow.add().grow();
        midRow.add(powerUpOverlay).growX();
        midRow.add().grow();
        root.add(midRow).grow();
        root.row();

        root.add(buildInventoryRow()).padBottom(40f).growX()
            .align(Align.bottom).minHeight(350f);

        stage.addActor(root);

        Table logRoot = new Table();
        logRoot.setFillParent(true);
        logRoot.setTouchable(Touchable.disabled);

        eventLogTable = new Table();
        eventLogTable.align(Align.bottomRight);
        logRoot.add(eventLogTable).expand().align(Align.bottomRight).padBottom(150f).padRight(50f);

        stage.addActor(logRoot);
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

        if (currentlyDraggingCard != null && receiptCards.contains(currentlyDraggingCard)) {
            currentlyDraggingCard.card.toFront();
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
                currentlyDraggingCard = data;
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

                currentlyDraggingCard = null;
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
        moneyLabel = new Label("Money: 100$", skin, "powerup");
        scoreLabel = new Label("Score: 0", skin, "powerup");
        topRow.add(satisfactionLabel).padRight(30f);
        topRow.add(moneyLabel).padRight(30f).spaceRight(20f);
        topRow.add(scoreLabel).padRight(30f);
        panel.add(topRow).padBottom(20f).align(Align.right);
        panel.row();

        Table levelRow = new Table();
        timerLabel = new Label("3:00", skin, "powerup");
        levelLabel = new Label("Level: 1/10", skin, "powerup");
        xpBar = new ProgressBar(0f, 100f, 1f, false, skin, "customerSatisfaction");

        levelRow.add(timerLabel).spaceRight(30f);
        levelRow.add(levelLabel).spaceRight(30f);
        levelRow.add(xpBar).expandX().minHeight(20f);
        panel.add(levelRow).expandX().align(Align.right);

        return panel;
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
                audioService.playSound(SoundAsset.MENU_HOVER); // FIX: SWITCH SOUND HOOK
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                card.addAction(Actions.color(skin.getColor("white"), 0.1f));
            }
        });
        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audioService.playSound(SoundAsset.MENU_SELECT); // FIX: CHOOSE SOUND HOOK
                powerUpSystem.applyPowerUp(type);
                addLogEvent("Power-Up Activated: " + type.displayName(), com.badlogic.gdx.graphics.Color.CYAN);
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
