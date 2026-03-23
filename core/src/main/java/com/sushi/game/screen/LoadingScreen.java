package com.sushi.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.sushi.game.SushiGame;
import com.sushi.game.asset.AssetService;
import com.sushi.game.asset.AtlasAsset;
import com.sushi.game.asset.SkinAsset;
import com.sushi.game.asset.SoundAsset;

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
        for (SoundAsset sound : SoundAsset.values()) {
            assetService.queue(sound);
        }
        assetService.queue(SkinAsset.DEFAULT);
        assetService.queue(SkinAsset.GAME);
    }

    @Override
    public void render(float delta) {
        if(this.assetService.update()){
            Gdx.app.debug("LoadingScreen", "Finished asset loading");
            createScreens();
            this.game.removeScreen(this);
            this.dispose();
            this.game.setScreen(MenuScreen.class);
        }
    }

    private void createScreens() {
        this.game.addScreen(new MenuScreen(this.game));
        this.game.addScreen(new GameScreen(this.game));

    }
}
