package com.sushi.game.asset;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;

public enum SoundAsset implements Asset<Sound> {
    WALKING("walking.wav");

    private final AssetDescriptor<Sound> descriptor;

    SoundAsset(String musicFile){
        this.descriptor = new AssetDescriptor<>("audio/sound/"+musicFile, Sound.class);
    }

    @Override
    public AssetDescriptor<Sound> getDescriptor() {
        return descriptor;
    }

}
