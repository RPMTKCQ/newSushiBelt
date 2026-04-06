package com.sushi.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.sushi.game.asset.AssetService;
import com.sushi.game.asset.SoundAsset;
import com.sushi.game.audio.AudioService;
import com.sushi.game.system.PowerUpSystem;
import com.sushi.game.ui.model.PowerUpType;
import com.sushi.game.ui.view.BottomBarUI;
import com.sushi.game.ui.view.PowerUpCardUI;
import com.sushi.game.ui.view.TopBarUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GameScreenUI {

    private final Stage stage;
    private final Skin skin;
    private final AssetService assetService;
    private final AudioService audioService;

    // --- SUB-COMPONENTS ---
    private TopBarUI topBarUI;
    private BottomBarUI bottomBarUI;

    private Table pauseOverlay;
    private Table powerUpOverlay;
    private Table eventLogTable;

    private boolean chefReady = false;
    private boolean chefCooking = false;

    // FIX: Variables for the Level-Up Queue
    private int queuedLevelUps = 0;
    private PowerUpSystem activePowerUpSystem;

    private int powerUpSelectedIndex = 0;
    private Group pauseSelectedItem;
    private final List<Group> pauseMenuItems = new ArrayList<>();
    private boolean inputLocked = false;

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private float holdTimer = 0f;
    private static final float REPEAT_RATE = 0.15f;

    private enum PauseState {MAIN, CONFIRM_QUIT}

    private PauseState currentPauseState = PauseState.MAIN;
    private com.badlogic.gdx.graphics.Texture darkOverlayTexture;

    public GameScreenUI(Stage stage, Skin skin, AssetService assetService, AudioService audioService) {
        this.stage = stage;
        this.skin = skin;
        this.assetService = assetService;
        this.audioService = audioService;
        build();
        setupKeyboardControls();
    }

    private void build() {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(new com.badlogic.gdx.graphics.Color(0f, 0f, 0f, 0.75f));
        pixmap.fill();
        darkOverlayTexture = new com.badlogic.gdx.graphics.Texture(pixmap);
        pixmap.dispose();

        topBarUI = new TopBarUI(skin, assetService);
        bottomBarUI = new BottomBarUI(skin, assetService);

        Table root = new Table();
        root.setName("GameScreen");
        root.setFillParent(true);
        root.setTouchable(Touchable.childrenOnly);

        root.add(topBarUI).growX().align(Align.top).minHeight(280f).row();

        powerUpOverlay = new Table();
        powerUpOverlay.setVisible(false);
        powerUpOverlay.setTouchable(Touchable.disabled);
        Table midRow = new Table();
        midRow.add().growX();
        midRow.add(powerUpOverlay).grow();
        midRow.add().growX();
        root.add(midRow).grow().row();

        root.add(bottomBarUI).padBottom(4f).growX().align(Align.bottom);
        stage.addActor(root);

        Table logRoot = new Table();
        logRoot.setFillParent(true);
        logRoot.setTouchable(Touchable.disabled);

        eventLogTable = new Table();
        eventLogTable.align(Align.bottomRight);
        logRoot.add(eventLogTable).expand().align(Align.bottomRight).padBottom(150f).padRight(50f);

        stage.addActor(logRoot);
    }

    private void setupKeyboardControls() {
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (pauseOverlay != null && pauseOverlay.isVisible()) return false;

                if (powerUpOverlay != null && powerUpOverlay.isVisible()) {
                    // FIX: Changed from LEFT/RIGHT arrows to A/D!
                    if (keycode == Input.Keys.A) {
                        powerUpSelectedIndex = (powerUpSelectedIndex - 1 + 3) % 3;
                        updatePowerUpColors();
                        if(audioService != null) audioService.playSound(SoundAsset.MENU_HOVER);
                        return true;
                    }
                    if (keycode == Input.Keys.D) {
                        powerUpSelectedIndex = (powerUpSelectedIndex + 1) % 3;
                        updatePowerUpColors();
                        if(audioService != null) audioService.playSound(SoundAsset.MENU_HOVER);
                        return true;
                    }
                    if (keycode == Input.Keys.W) {
                        scrollSelectedPowerUp(-30f);
                        return true;
                    }
                    if (keycode == Input.Keys.S) {
                        scrollSelectedPowerUp(30f);
                        return true;
                    }
                    if (keycode == Input.Keys.SPACE || keycode == Input.Keys.ENTER) {
                        Actor card = powerUpOverlay.getChildren().get(powerUpSelectedIndex);
                        if (card instanceof Table) {
                            InputEvent clickEvent = new InputEvent();
                            clickEvent.setType(InputEvent.Type.touchDown);
                            card.fire(clickEvent);
                            clickEvent.setType(InputEvent.Type.touchUp);
                            card.fire(clickEvent);
                        }
                        return true;
                    }
                    return false;
                }

                if (topBarUI.handleKeyDown(keycode)) return true;

                return false;
            }
        });
    }

    private void scrollSelectedPowerUp(float amount) {
        if (powerUpOverlay != null && powerUpOverlay.getChildren().size > powerUpSelectedIndex) {
            Actor actor = powerUpOverlay.getChildren().get(powerUpSelectedIndex);
            if (actor instanceof PowerUpCardUI) {
                ((PowerUpCardUI) actor).scroll(amount);
            }
        }
    }

    public void addOrder(String dishId, String customerId, float maxTime) {
        topBarUI.addOrder(dishId, customerId, maxTime);
    }

    public void removeOrder(String customerId) {
        topBarUI.removeOrder(customerId);
    }

    public void updateReceipts(float delta) {
        topBarUI.updateReceipts(delta);
    }

    public String getFirstDishName() {
        return topBarUI.getFirstDishName();
    }

    public String getFirstCustomerId() {
        return topBarUI.getFirstCustomerId();
    }

    public void setMoney(String text) {
        topBarUI.setMoney(text);
    }

    public void setScore(String text) {
        topBarUI.setScore(text);
    }

    public boolean isQueueSortedByUrgency() {
        return topBarUI.isQueueSortedByUrgency();
    }

    public void setLastSubmitSorted(boolean sorted) {
        topBarUI.setLastSubmitSorted(sorted);
    }

    public boolean wasLastSubmitSorted() {
        return topBarUI.wasLastSubmitSorted();
    }

    public void startCookingFor(String customerId) {
        topBarUI.startCookingFor(customerId);
    }

    public void updateCookingProgress(float ratio) {
        topBarUI.updateCookingProgress(ratio);
    }

    public void resetCookingBar() {
        topBarUI.resetCookingBar();
    }

    public void setReputation(int current, int max) {
        bottomBarUI.setReputation(current, max);
    }

    public void setXp(int xp, int xpToNext, int level) {
        bottomBarUI.setXp(xp, xpToNext, level);
    }

    public void updateTimer(String timeText) {
        bottomBarUI.updateTimer(timeText);
    }

    public void updateInventory(List<String> dishes) {
        bottomBarUI.updateInventory(dishes);
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
        if (!ready) topBarUI.resetCookingBar();
    }

    public void playCookingDoneSound() {
        if (audioService != null) audioService.playSound(SoundAsset.COOKING_DONE);
    }

    public void addLogEvent(String message, com.badlogic.gdx.graphics.Color color) {
        Label logLabel = new Label(message, skin, "receipt");
        logLabel.setColor(color);
        eventLogTable.add(logLabel).align(Align.right).padBottom(5f).row();

        logLabel.addAction(Actions.sequence(
            Actions.alpha(0f), Actions.fadeIn(0.2f), Actions.delay(4f),
            Actions.fadeOut(1f), Actions.removeActor()
        ));
    }

    private void updatePowerUpColors() {
        for (int i = 0; i < powerUpOverlay.getChildren().size; i++) {
            Actor actor = powerUpOverlay.getChildren().get(i);
            if (actor instanceof Table card) {
                card.clearActions();
                if (i == powerUpSelectedIndex) card.addAction(Actions.color(skin.getColor("sand"), 0.1f));
                else card.addAction(Actions.color(skin.getColor("white"), 0.1f));
            }
        }
    }

    // FIX: This method now adds the level-up to a queue instead of just overriding the screen!
    public void showPowerUpOverlay(PowerUpSystem powerUpSystem) {
        this.activePowerUpSystem = powerUpSystem;
        queuedLevelUps++;

        if (!powerUpOverlay.isVisible()) {
            displayNextPowerUp();
        }
    }

    // FIX: This method renders the screen. When you pick a card, it checks if it should run again!
    private void displayNextPowerUp() {
        if (queuedLevelUps <= 0) return;
        queuedLevelUps--;

        audioService.playSound(SoundAsset.LEVEL_UP);
        powerUpOverlay.clearChildren();

        List<PowerUpType> allTypes = new ArrayList<>(Arrays.asList(PowerUpType.values()));
        Collections.shuffle(allTypes);

        for (int i = 0; i < 3; i++) {
            PowerUpType type = allTypes.get(i);

            Runnable onSelected = () -> {
                addLogEvent("Power-Up Activated: " + type.displayName(), Color.CYAN);

                // NEW: Tell LevelSystem to apply any immediate corruptions!
                if (activePowerUpSystem != null && activePowerUpSystem.getEngine() != null) {
                    activePowerUpSystem.getEngine().getSystem(com.sushi.game.system.LevelSystem.class).applyImmediateCorruption(type);
                }

                if (queuedLevelUps > 0) {
                    displayNextPowerUp();
                } else {
                    hideOverlay();
                }
            };

            final int index = i;
            Runnable onHover = () -> {
                powerUpSelectedIndex = index;
                updatePowerUpColors();
                if (audioService != null) audioService.playSound(SoundAsset.MENU_HOVER);
            };

            PowerUpCardUI card = new PowerUpCardUI(skin, type, activePowerUpSystem, audioService, onSelected, onHover);
            powerUpOverlay.add(card).padLeft(20f).padRight(20f).padBottom(120f).minWidth(250f).minHeight(400f);
        }

        powerUpSelectedIndex = 0;
        updatePowerUpColors();

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
        if (powerUpOverlay != null) {
            powerUpOverlay.setTouchable(isPaused ? Touchable.disabled : (powerUpOverlay.isVisible() ? Touchable.enabled : Touchable.disabled));
        }

        if (pauseOverlay == null) {
            pauseOverlay = new Table();
            pauseOverlay.setFillParent(true);
            pauseOverlay.setBackground(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(darkOverlayTexture));

            pauseOverlay.addListener(new InputListener() {
                @Override
                public boolean keyDown(InputEvent event, int keycode) {
                    if (!isPaused) return false;
                    if (inputLocked) return true;

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
                            action.run();
                            return true;
                        }
                    }

                    if (pauseMenuItems.isEmpty()) return false;
                    int currentIndex = pauseMenuItems.indexOf(pauseSelectedItem);
                    int newIndex = currentIndex;

                    if (currentPauseState == PauseState.CONFIRM_QUIT) {
                        if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT)
                            newIndex = (currentIndex - 1 + pauseMenuItems.size()) % pauseMenuItems.size();
                        else if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT)
                            newIndex = (currentIndex + 1) % pauseMenuItems.size();
                    } else {
                        if (keycode == Input.Keys.W || keycode == Input.Keys.UP)
                            newIndex = (currentIndex - 1 + pauseMenuItems.size()) % pauseMenuItems.size();
                        else if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN)
                            newIndex = (currentIndex + 1) % pauseMenuItems.size();
                        else if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
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
                    if (!isPaused || inputLocked) return true; // FIX: Prevent clicking through Pause Menu
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
                    return true; // FIX: Prevent clicking through Pause Menu
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

    private TextButton createPauseButton(String text, Runnable onClick) {
        TextButton.TextButtonStyle customStyle = new TextButton.TextButtonStyle(skin.get("menu", TextButton.TextButtonStyle.class));
        customStyle.over = null;
        customStyle.checkedOver = null;
        customStyle.down = null;

        TextButton button = new TextButton(text, customStyle);
        button.setTransform(true);
        button.setOrigin(Align.center);
        button.setColor(Color.LIGHT_GRAY);

        Runnable unifiedAction = () -> {
            audioService.playSound(SoundAsset.MENU_SELECT);
            executeSafeAction(onClick);
        };
        button.setUserObject(unifiedAction);

        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                unifiedAction.run();
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                selectPauseItem(button);
            }
        });

        pauseMenuItems.add(button);
        return button;
    }

    private void buildMainPauseScreen(Runnable onResume, Runnable onQuit) {
        currentPauseState = PauseState.MAIN;
        pauseOverlay.clearChildren();
        pauseMenuItems.clear();
        this.pauseSelectedItem = null;

        Label title = new Label("GAME PAUSED", skin, "title");
        title.setAlignment(Align.center);
        pauseOverlay.add(title).padBottom(40f).row();

        TextButton resumeBtn = createPauseButton("Resume", onResume);
        pauseOverlay.add(resumeBtn).minSize(300f, 80f).padBottom(20f).row();

        Table musicTable = new Table();
        musicTable.setTransform(true);
        musicTable.setOrigin(Align.center);
        musicTable.setColor(Color.LIGHT_GRAY);

        Label musicLabel = new Label("Music Volume", skin, "receipt");
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
        soundTable.setTransform(true);
        soundTable.setOrigin(Align.center);
        soundTable.setColor(Color.LIGHT_GRAY);

        Label soundLabel = new Label("Sound Volume", skin, "receipt");
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

        TextButton quitBtn = createPauseButton("Quit", () -> buildConfirmQuitScreen(onResume, onQuit));
        pauseOverlay.add(quitBtn).minSize(300f, 80f);

        pauseOverlay.pack();
        if (!pauseMenuItems.isEmpty()) {
            stage.act(0f);
            selectPauseItem(pauseMenuItems.get(0));
        }
    }

    private void buildConfirmQuitScreen(Runnable onResume, Runnable onQuit) {
        currentPauseState = PauseState.CONFIRM_QUIT;
        pauseOverlay.clearChildren();
        pauseMenuItems.clear();
        this.pauseSelectedItem = null;

        Label title = new Label("Quit to Menu?", skin, "title");
        title.setAlignment(Align.center);
        pauseOverlay.add(title).padBottom(40f).row();

        Label subTitle = new Label("Progress will be lost", skin, "receipt");
        subTitle.setColor(skin.getColor("white"));
        subTitle.setAlignment(Align.center);
        pauseOverlay.add(subTitle).padBottom(40f).row();

        Table buttonsTable = new Table();

        TextButton yesBtn = createPauseButton("Yes", onQuit);
        buttonsTable.add(yesBtn).minSize(200f, 60f).padRight(30f);

        TextButton noBtn = createPauseButton("No", () -> buildMainPauseScreen(onResume, onQuit));
        buttonsTable.add(noBtn).minSize(200f, 60f).padLeft(30f);

        pauseOverlay.add(buttonsTable);
        pauseOverlay.pack();

        if (!pauseMenuItems.isEmpty()) {
            stage.act(0f);
            selectPauseItem(pauseMenuItems.get(1));
        }
    }

    private void adjustPauseSlider(float amount) {
        if (pauseSelectedItem != null && pauseSelectedItem.getUserObject() instanceof Slider slider)
            slider.setValue(slider.getValue() + amount);
    }

    private void selectPauseItem(Group group) {
        if (this.pauseSelectedItem == group) return;
        if (this.pauseSelectedItem != null) {
            this.pauseSelectedItem.clearActions();
            this.pauseSelectedItem.setScale(1f);
            this.pauseSelectedItem.setColor(Color.LIGHT_GRAY);
        }
        if (this.pauseSelectedItem != null) audioService.playSound(SoundAsset.MENU_HOVER);
        this.pauseSelectedItem = group;
        if (this.pauseSelectedItem != null) {
            this.pauseSelectedItem.setOrigin(this.pauseSelectedItem.getWidth() / 2f, this.pauseSelectedItem.getHeight() / 2f);
            this.pauseSelectedItem.clearActions();
            this.pauseSelectedItem.setScale(1.15f);
            this.pauseSelectedItem.setColor(Color.WHITE);
        }
    }

    public boolean isPowerUpOverlayVisible() {
        return powerUpOverlay != null && powerUpOverlay.isVisible();
    }

}
