package com.sushi.game.asset;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Music;

public enum MusicAsset implements Asset<Music> {
    MENU("audio_2.wav"), // yeah i think its audio_2.wav
    L1_MUSIC("audio_1.mp3"),
    L2_MUSIC("audio_5.mp3"),
    L3_MUSIC("audio_4.mp3");


    private final AssetDescriptor<Music> descriptor;

    MusicAsset(String musicFile) {
        this.descriptor = new AssetDescriptor<>("audio/music/"+musicFile, Music.class);
    }

    @Override
    public AssetDescriptor<Music> getDescriptor() {
        return descriptor;
    }
// to connect
}
