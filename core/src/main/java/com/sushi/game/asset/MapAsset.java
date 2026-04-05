package com.sushi.game.asset;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.maps.tiled.BaseTiledMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public enum MapAsset implements Asset<TiledMap> {
    EASY("restaurant-1.tmx");

    private final AssetDescriptor<TiledMap> descriptor;

    MapAsset(String mapName) {
        TmxMapLoader.Parameters parameters = new TmxMapLoader.Parameters();
        parameters.projectFilePath = "tiledMAP/sushiBelt.tiled-project";
        this.descriptor = new AssetDescriptor<>("tiledMAP/" + mapName, TiledMap.class);
    }

    @Override
    public AssetDescriptor<TiledMap> getDescriptor() {
        return this.descriptor;
    }
}
