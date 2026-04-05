package com.sushi.game.screen;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.sushi.game.SushiGame;
import com.sushi.game.asset.MapAsset;
import com.sushi.game.asset.SkinAsset;

public class GameOverScreen extends ScreenAdapter {

    private final SushiGame game;
    private final Stage     stage;
    private final int       finalScore;
    private final MapAsset  currentStage;
    private final boolean   isEndlessMode;

    public GameOverScreen(SushiGame game, int finalScore, MapAsset currentStage, boolean isEndlessMode) {
        this.game          = game;
        this.finalScore    = finalScore;
        this.currentStage  = currentStage;
        this.isEndlessMode = isEndlessMode;
        this.stage         = new Stage(new FitViewport(1920f, 1080f), game.getBatch());
        build();
    }

    private void build() {
        Skin skin = game.getAssetService().get(SkinAsset.DEFAULT);

        Table root = new Table();
        root.setFillParent(true);
        root.center();

        Label title = new Label("GAME OVER", skin, "powerup");
        title.setAlignment(Align.center);

        Label score = new Label("Final Score: " + finalScore, skin, "receipt");
        score.setAlignment(Align.center);

        TextButton retry = new TextButton("Try Again", skin);
        retry.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                // Restarts the exact stage and mode the player died on
                game.setScreen(new GameScreen(game, currentStage, isEndlessMode));
                dispose();
            }
        });

        TextButton menu = new TextButton("Main Menu", skin);
        menu.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
                dispose();
            }
        });

        root.add(title).padBottom(40f).row();
        root.add(score).padBottom(60f).row();
        root.add(retry).padBottom(20f).minSize(300f, 80f).row();
        root.add(menu).minSize(300f, 80f);

        stage.addActor(root);
    }

    @Override public void show()  { game.setInputProcessors(stage); }
    @Override public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }
    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void dispose() { stage.dispose(); }
}
