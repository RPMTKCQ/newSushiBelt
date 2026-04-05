package com.sushi.game.ui.model;

import com.badlogic.gdx.Gdx;
import com.sushi.game.SushiGame;
import com.sushi.game.asset.MapAsset;
import com.sushi.game.audio.AudioService;
import com.sushi.game.screen.GameScreen;

public class MenuViewModel extends ViewModel {

    private final AudioService audioService;

    public MenuViewModel(SushiGame game) {
        super(game);
        this.audioService = game.getAudioService();
    }

    public float getMusicVolume() {
        return audioService.getMusicVolume();
    }

    public float getSoundVolume() {
        return audioService.getSoundVolume();
    }

    public void setMusicVolume(float volume) {
        this.audioService.setMusicVolume(volume);
    }

    public void setSoundVolume(float volume) {
        this.audioService.setSoundVolume(volume);
    }


    public void playSound(com.sushi.game.asset.SoundAsset sound) {
        audioService.playSound(sound);
    }


    public void startGame(boolean isEndlessMode, MapAsset stage) {
        game.setScreen(new GameScreen(game, stage, isEndlessMode));
    }

    public void quitGame() {
        Gdx.app.exit();
    }
}
