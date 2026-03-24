package com.sushi.game.asset;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Music;

public enum MusicAsset implements Asset<Music> {
    BACKGROUND("background.ogg");

    private final AssetDescriptor<Music> descriptor;

    MusicAsset(String musicFile) {
        this.descriptor = new AssetDescriptor<>("audio/"+musicFile, Music.class);
    }

    @Override
    public AssetDescriptor<Music> getDescriptor() {
        return descriptor;
    }
// to connect
}
