package com.sushi.game.screen;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.sushi.game.SushiGame;
import com.sushi.game.asset.SkinAsset;

public class WinScreen extends ScreenAdapter {

    private final SushiGame game;
    private final Stage stage;
    private final int finalScore;
    private final int finalMoney;

    public WinScreen(SushiGame game, int finalScore, int finalMoney) {
        this.game = game;
        this.finalScore = finalScore;
        this.finalMoney = finalMoney;
        this.stage = new Stage(new FitViewport(1920f, 1080f), game.getBatch());
        build();
    }

    private void build() {
        Skin skin = game.getAssetService().get(SkinAsset.DEFAULT);

        Table root = new Table();
        root.setFillParent(true);
        root.center();

        Label title = new Label("SUCCESS!", skin, "powerup");
        title.setAlignment(Align.center);

        Label subtitle = new Label("Financial Quota Exceeded!", skin, "powerup");
        subtitle.setAlignment(Align.center);

        Label moneyLabel = new Label("Cash Earned: $" + finalMoney, skin, "receipt");
        moneyLabel.setAlignment(Align.center);

        Label scoreLabel = new Label("Reputation Score: " + finalScore, skin, "receipt");
        scoreLabel.setAlignment(Align.center);

        TextButton menu = new TextButton("Main Menu", skin);
        menu.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
                dispose();
            }
        });

        root.add(title).padBottom(20f).row();
        root.add(subtitle).padBottom(40f).row();
        root.add(moneyLabel).padBottom(10f).row();
        root.add(scoreLabel).padBottom(60f).row();
        root.add(menu).minSize(300f, 80f);

        stage.addActor(root);
    }

    @Override public void show() { game.setInputProcessors(stage); }
    @Override public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }
    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void dispose() { stage.dispose(); }
}
