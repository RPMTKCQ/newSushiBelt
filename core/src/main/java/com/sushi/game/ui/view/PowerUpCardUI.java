package com.sushi.game.ui.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.sushi.game.asset.SoundAsset;
import com.sushi.game.audio.AudioService;
import com.sushi.game.system.PowerUpSystem;
import com.sushi.game.ui.model.PowerUpType;

public class PowerUpCardUI extends Table {

    private final ScrollPane scrollPane; // FIX: Make scrollPane a class variable so we can access it!

    public PowerUpCardUI(Skin skin, PowerUpType type, PowerUpSystem powerUpSystem, AudioService audioService, Runnable onSelected, Runnable onHover) {
        super(skin);

        setBackground(skin.getDrawable("rct-border"));
        align(Align.top);
        setUserObject(type);
        setTouchable(Touchable.enabled);

        Table iconBorder = new Table();
        iconBorder.setBackground(skin.getDrawable("powerUpIconBG"));
        Image icon = new Image(skin, type.iconName());
        icon.setScaling(Scaling.fit);
        icon.setTouchable(Touchable.disabled);
        iconBorder.add(icon).minSize(50f);
        iconBorder.setTouchable(Touchable.disabled);
        add(iconBorder).padTop(30f).minSize(80f).row();

        Table texts = new Table();
        texts.setTouchable(Touchable.disabled);

        Label categoryLabel = new Label(type.category(), skin, "powerup");
        categoryLabel.setColor(skin.getColor("black"));
        texts.add(categoryLabel).padBottom(10f).row();

        Label title = new Label(type.displayName(), skin, "receipt");
        title.setColor(skin.getColor("black"));
        texts.add(title).padBottom(15f).row();

        Table descTable = new Table();
        descTable.setTouchable(Touchable.disabled);

        Label desc = new Label(type.description(), skin, "small");
        desc.setWrap(true);
        desc.setColor(skin.getColor("black"));
        descTable.add(desc).growX().row();

        Label corruption = new Label(type.corruption(), skin, "small");
        corruption.setWrap(true);
        Color redColor = skin.has("Text_RED", Color.class) ? skin.getColor("Text_RED") : Color.RED;
        corruption.setColor(redColor);
        if (type.corruption().isEmpty()) {
            corruption.setText("");
        } else {
            descTable.add(corruption).growX().padTop(5f);
        }

        scrollPane = new ScrollPane(descTable, new ScrollPane.ScrollPaneStyle());
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setTouchable(Touchable.disabled);

        texts.add(scrollPane).grow().padTop(5f);
        add(texts).padTop(10f).padLeft(20f).padRight(20f).grow().padBottom(20f).minWidth(170f).minHeight(190f);

        addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                onHover.run();
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                addAction(Actions.color(skin.getColor("white"), 0.1f));
            }
        });

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (audioService != null) audioService.playSound(SoundAsset.MENU_SELECT);
                powerUpSystem.applyPowerUp(type);
                onSelected.run();
            }
        });
    }

    // FIX: A helper method so GameScreenUI can push the scroll wheel up and down!
    public void scroll(float amount) {
        if (scrollPane != null) {
            scrollPane.setScrollY(scrollPane.getScrollY() + amount);
        }
    }
}
