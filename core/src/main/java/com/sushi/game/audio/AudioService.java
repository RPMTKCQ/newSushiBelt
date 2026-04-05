package com.sushi.game.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.sushi.game.asset.AssetService;
import com.sushi.game.asset.MusicAsset;
import com.sushi.game.asset.SoundAsset;

public class AudioService {

    private final AssetService assetService;
    private Music currentMusic;
    private MusicAsset currentMusicAsset;
    private float musicVolume;
    private float soundVolume;
    private final Preferences prefs;

    public AudioService(AssetService assetService) {
        this.assetService = assetService;
        this.currentMusic = null;
        this.currentMusicAsset = null;

        // Load saved volumes. If they don't exist, default to 0.5f and 0.33f
        this.prefs = Gdx.app.getPreferences("SushiBeltSettings");
        this.musicVolume = prefs.getFloat("musicVolume", 0.5f);
        this.soundVolume = prefs.getFloat("soundVolume", 0.33f);
    }

    public void setMusicVolume(float musicVolume) {
        this.musicVolume = MathUtils.clamp(musicVolume, 0f, 1f);
        if (this.currentMusic != null) {
            this.currentMusic.setVolume(this.musicVolume);
        }
        // Save the setting
        prefs.putFloat("musicVolume", this.musicVolume);
        prefs.flush();
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setSoundVolume(float soundVolume) {
        this.soundVolume = MathUtils.clamp(soundVolume, 0f, 1f);

        // Save the setting
        prefs.putFloat("soundVolume", this.soundVolume);
        prefs.flush();
    }

    public float getSoundVolume() {
        return soundVolume;
    }

    public void playMusic(MusicAsset musicAsset) {
        if (this.currentMusicAsset == musicAsset) return;

        if (this.currentMusic != null) {
            this.currentMusic.stop();
            this.assetService.unload(this.currentMusicAsset);
        }

        this.currentMusic = this.assetService.load(musicAsset);
        this.currentMusic.setVolume(musicVolume); // Ensure loaded music respects current volume
        this.currentMusic.setLooping(true);
        this.currentMusic.play();
        this.currentMusicAsset = musicAsset;
    }

    public void playSound(SoundAsset soundAsset) {
        this.assetService.get(soundAsset).play(soundVolume);
    }

    public void setMap(TiledMap tiledMap) {
        String musicAssetStr = tiledMap.getProperties().get("music", "", String.class);
        if (musicAssetStr.isBlank()) return;

        MusicAsset musicAsset = MusicAsset.valueOf(musicAssetStr);
        playMusic(musicAsset);
    }
}
