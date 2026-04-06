package com.sushi.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sushi.game.SushiGame;
import com.sushi.game.asset.MapAsset;
import com.sushi.game.asset.MusicAsset;
import com.sushi.game.asset.SkinAsset;
import com.sushi.game.asset.SoundAsset;

import java.util.ArrayList;
import java.util.List;

public class GameOverScreen extends ScreenAdapter {
    private final SushiGame game;
    private final Stage stage;
    private final Skin skin;
    private final Viewport uiViewport;

    private final int score;
    private final MapAsset currentStage;
    private final boolean isEndless;

    private Group selectedItem;
    private final List<Group> menuItems = new ArrayList<>();
    private boolean isInitialSelectionDone = false;

    public GameOverScreen(SushiGame game, int score, MapAsset currentStage, boolean isEndless) {
        this.game = game;
        this.score = score;
        this.currentStage = currentStage;
        this.isEndless = isEndless;

        this.uiViewport = new FitViewport(800f, 450f);
        this.stage = new Stage(uiViewport, game.getBatch());
        this.skin = game.getAssetService().get(SkinAsset.DEFAULT);

        buildUI();
    }

    private void buildUI() {
        Table table = new Table();
        table.setFillParent(true);

        Label label = new Label("Game Over!", skin, "title");
        table.add(label).padBottom(20f).row();

        Label scoreLabel = new Label("Score: " + score, skin, "default");
        table.add(scoreLabel).padBottom(40f).row();

        TextButton retryBtn = createButton("Retry", () -> game.setScreen(new GameScreen(game, currentStage, isEndless)));
        table.add(retryBtn).padBottom(15f).minWidth(200f).row();

        TextButton menuBtn = createButton("Main Menu", () -> game.setScreen(new MenuScreen(game)));
        table.add(menuBtn).minWidth(200f).row();

        stage.addActor(table);

        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                return handleKeyDown(keycode);
            }
        });
    }

    private TextButton createButton(String text, Runnable onClick) {
        TextButton.TextButtonStyle customStyle = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
        customStyle.over = null;
        customStyle.checkedOver = null;
        customStyle.down = null;

        TextButton button = new TextButton(text, customStyle);

        button.setTransform(true);
        button.setOrigin(Align.center);
        button.setColor(Color.LIGHT_GRAY);

        Runnable unifiedAction = () -> {
            game.getAudioService().playSound(SoundAsset.MENU_SELECT);
            onClick.run();
        };
        button.setUserObject(unifiedAction);

        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                unifiedAction.run();
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                selectMenuItem(button);
            }
        });

        menuItems.add(button);
        return button;
    }

    private boolean handleKeyDown(int keycode) {
        if (menuItems.isEmpty()) return false;

        int currentIndex = menuItems.indexOf(selectedItem);
        int newIndex = currentIndex;

        if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
            newIndex = (currentIndex - 1 + menuItems.size()) % menuItems.size();
        } else if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
            newIndex = (currentIndex + 1) % menuItems.size();
        }

        if (keycode == Input.Keys.SPACE || keycode == Input.Keys.ENTER) {
            if (selectedItem != null && selectedItem.getUserObject() instanceof Runnable action) {
                action.run();
            }
            return true;
        }

        if (newIndex != currentIndex) {
            selectMenuItem(menuItems.get(newIndex));
            return true;
        }
        return false;
    }

    private void selectMenuItem(Group group) {
        if (this.selectedItem == group) return;

        if (this.selectedItem != null) {
            this.selectedItem.clearActions();
            this.selectedItem.setScale(1f);
            this.selectedItem.setColor(Color.LIGHT_GRAY);
        }

        if (this.selectedItem != null) {
            game.getAudioService().playSound(SoundAsset.MENU_HOVER);
        }

        this.selectedItem = group;

        if (this.selectedItem != null) {
            this.selectedItem.setOrigin(Align.center);
            this.selectedItem.clearActions();
            this.selectedItem.setScale(1.15f);
            this.selectedItem.setColor(Color.WHITE);
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        game.getAudioService().playMusic(MusicAsset.GAME_OVER, false);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);

        if (!isInitialSelectionDone && !menuItems.isEmpty()) {
            selectMenuItem(menuItems.get(0));
            isInitialSelectionDone = true;
        }

        stage.draw();
    }

    @Override
    public void resize(int width, int height) { uiViewport.update(width, height, true); }

    @Override
    public void hide() { stage.clear(); }

    @Override
    public void dispose() { stage.dispose(); }
}
