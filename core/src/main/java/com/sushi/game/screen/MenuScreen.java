package com.sushi.game.screen;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
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

    public MenuScreen(SushiGame game) {
        this.game = game;
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
        this.game.setInputProcessors(stage);

        this.stage.addActor(new MenuView(stage, skin, new MenuViewModel(game)));
        this.game.getAudioService().playMusic(MusicAsset.BACKGROUND);
    }

    @Override
    public void hide() {
        this.stage.clear();
    }

    @Override
    public void render(float delta) {
        uiViewport.apply();
        stage.getBatch().setColor(Color.WHITE);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
