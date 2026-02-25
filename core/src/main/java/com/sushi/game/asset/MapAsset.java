package com.sushi.game.asset;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.maps.tiled.BaseTiledMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public enum MapAsset implements Asset<TiledMap> {
    MAIN("mao.tmx");

    private final AssetDescriptor<TiledMap> descriptor;

      MapAsset(String mapName) {
          TmxMapLoader.Parameters parameters = new TmxMapLoader.Parameters();
          parameters.projectFilePath = "tiledMAP/samplesushi.tiled-project";
          this.descriptor = new AssetDescriptor<>("tiledMAP/"+mapName, TiledMap.class);
    }

    @Override
    public AssetDescriptor<TiledMap> getDescriptor() {
        return this.descriptor;
    }
}
