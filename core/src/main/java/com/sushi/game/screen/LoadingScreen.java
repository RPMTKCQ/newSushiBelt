package com.sushi.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.sushi.game.SushiGame;
import com.sushi.game.asset.AssetService;
import com.sushi.game.asset.AtlasAsset;

public class LoadingScreen extends ScreenAdapter {

    private final SushiGame game;
    private final AssetService assetService;

    public LoadingScreen(SushiGame game, AssetService assetService) {
        this.game = game;
        this.assetService = assetService;
    }

    @Override
    public void show() {
        for (AtlasAsset atlas : AtlasAsset.values()) {
            assetService.queue(atlas);
        }
    }

    @Override
    public void render(float delta) {
        if(this.assetService.update()){
            Gdx.app.debug("LoadingScreen", "Finished asset loading");
            createScreens();
            this.game.removeScreen(this);
            this.dispose();
            this.game.setScreen(GameScreen.class);
        }
    }

    private void createScreens() {
        this.game.addScreen(new GameScreen(this.game));

    }
}
