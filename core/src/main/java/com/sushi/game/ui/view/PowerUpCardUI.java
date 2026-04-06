package com.sushi.game.ui.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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

    public PowerUpCardUI(Skin skin, PowerUpType type, PowerUpSystem powerUpSystem, AudioService audioService, Runnable onSelected, Runnable onHover) {
        super(skin);

        setBackground(skin.getDrawable("rct-border"));
        align(Align.top);
        setUserObject(type);
        setTouchable(Touchable.enabled);

        Table iconBorder = new Table();
        iconBorder.setBackground(skin.getDrawable("powerUpIconBG"));
        Image icon = new Image(skin, "servicePowerUpIcon");
        icon.setScaling(Scaling.fit);
        icon.setTouchable(Touchable.disabled);
        iconBorder.add(icon).minSize(50f);
        iconBorder.setTouchable(Touchable.disabled);
        add(iconBorder).padTop(30f).minSize(80f).row();

        Table texts = new Table();
        texts.setTouchable(Touchable.disabled);

        Label categoryLabel = new Label("[Power-Up]", skin, "powerup");
        categoryLabel.setColor(skin.getColor("black"));
        texts.add(categoryLabel).padBottom(10f).row();

        Label title = new Label(type.displayName(), skin, "receipt");
        title.setColor(skin.getColor("black"));
        texts.add(title).padBottom(20f).row();

        Table descTable = new Table();
        descTable.setTouchable(Touchable.disabled);

        Label desc = new Label(type.description(), skin, "small");
        desc.setWrap(true);
        desc.setColor(skin.getColor("black"));
        descTable.add(desc).growX().row();

        Label corruption = new Label("Corruption debuff description here", skin, "small");
        corruption.setWrap(true);
        Color redColor = skin.has("Text_RED", Color.class) ? skin.getColor("Text_RED") : Color.RED;
        corruption.setColor(redColor);
        descTable.add(corruption).growX();

        texts.add(descTable).growX();
        add(texts).padTop(20f).minWidth(170f).minHeight(190f);

        // Add Input Listeners
        addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                onHover.run(); // Tell the main UI we are hovering
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
                onSelected.run(); // Tell the main UI to close the menu
            }
        });
    }
}
