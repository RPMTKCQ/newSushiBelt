package com.sushi.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sushi.game.SushiGame;
import com.sushi.game.asset.MusicAsset;
import com.sushi.game.asset.SkinAsset;
import com.sushi.game.ui.model.MenuViewModel;
import com.sushi.game.ui.view.MenuView;

public class MenuScreen extends ScreenAdapter {
    private final SushiGame game;
    private final Stage stage;
    private final Skin skin;
    private final Viewport uiViewport;

    // NEW: Flag to track if we should skip the Title Screen
    private final boolean jumpToModeSelect;

    // Default Constructor (Normal Boot)
    public MenuScreen(SushiGame game) {
        this(game, false);
    }

    // Overloaded Constructor (End Screen Boot)
    public MenuScreen(SushiGame game, boolean jumpToModeSelect) {
        this.game = game;
        this.jumpToModeSelect = jumpToModeSelect;
        this.uiViewport = new FitViewport(800f, 450f);
        this.stage = new Stage(uiViewport, game.getBatch());
        this.skin = game.getAssetService().get(SkinAsset.DEFAULT);
    }

    @Override
    public void resize(int width, int height) {
        uiViewport.update(width, height, true);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        MenuView menuView = new MenuView(stage, skin, new MenuViewModel(game));

        // FIX: Tell the view to instantly swap menus before it even renders!
        if (jumpToModeSelect) {
            menuView.jumpToModeSelect();
        }

        this.stage.addActor(menuView);
        this.game.getAudioService().playMusic(MusicAsset.MENU);
    }

    @Override
    public void hide() {
        this.stage.clear();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
