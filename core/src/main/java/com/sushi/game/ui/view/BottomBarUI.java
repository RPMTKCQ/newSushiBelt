package com.sushi.game.ui.view;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.sushi.game.asset.AssetService;
import com.sushi.game.asset.AtlasAsset;

import java.util.List;

public class BottomBarUI extends Table {

    private final Skin skin;
    private final AssetService assetService;

    private Label timerLabel;
    private Label reputationTextLabel;
    private Label levelLabel; // NEW: The Level Indicator Label
    private ProgressBar reputationBar;
    private ProgressBar xpBar;
    private final Table[] inventorySlots = new Table[3];

    public BottomBarUI(Skin skin, AssetService assetService) {
        super(skin);
        this.skin = skin;
        this.assetService = assetService;

        align(Align.bottom);
        add(buildPlayerStatePanel()).padBottom(40f).minWidth(700f);
        add(buildInventoryRow()).padBottom(40f).growX().align(Align.bottom);
        add().minWidth(700f);
    }

    private Table buildPlayerStatePanel() {
        Table playerState = new Table();
        playerState.align(Align.left);

        Image playerIcon = new Image(skin, "player-icon");
        playerIcon.setScaling(Scaling.fit);
        playerState.add(playerIcon).padLeft(50f).align(Align.top).minSize(32f).maxSize(100f);

        Table barsTable = new Table();
        barsTable.align(Align.left);

        Table repBarTable = new Table();
        reputationBar = new ProgressBar(0f, 100f, 1f, false, skin, "reputationBar");
        repBarTable.add(reputationBar).grow();
        barsTable.add(repBarTable).align(Align.topLeft).minWidth(450f).row();

        Table xpRowTable = new Table();
        reputationTextLabel = new Label("REP: 100/100", skin, "powerup");
        xpRowTable.add(reputationTextLabel).padLeft(30f).padRight(30f);
        xpBar = new ProgressBar(0f, 100f, 1f, false, skin, "xpBar");
        xpRowTable.add(xpBar).growX().minWidth(0f);
        barsTable.add(xpRowTable).fillX().align(Align.left).minWidth(200f).row();

        Table timerRow = new Table();

        // FIX: Added the Level Label into the empty space on the left!
        levelLabel = new Label("LV. 1", skin, "powerup");
        timerRow.add(levelLabel).align(Align.left).padLeft(30f).growX();

        Table timerContainer = new Table();
        timerContainer.align(Align.right);
        Image timerImg = new Image(skin, "timer");
        timerImg.setScaling(Scaling.fit);
        timerContainer.add(timerImg).minSize(32f);
        timerLabel = new Label("00:00", skin, "powerup");
        timerContainer.add(timerLabel);
        timerRow.add(timerContainer).align(Align.right);
        barsTable.add(timerRow).growX();

        playerState.add(barsTable).align(Align.top);
        return playerState;
    }

    private Table buildInventoryRow() {
        Table row = new Table();
        row.align(Align.bottom);
        row.add().growX().align(Align.bottom);

        Table slots = new Table();
        for (int i = 0; i < 3; i++) {
            Table slot = new Table();
            slot.setBackground(skin.getDrawable("fd-border"));
            slot.pad(5f);
            inventorySlots[i] = slot;
            slots.add(inventorySlots[i]).spaceRight(7f).minSize(100f).maxSize(100f);
        }
        row.add(slots).growX().align(Align.bottom);
        row.add().growX();
        return row;
    }

    public void setReputation(int current, int max) {
        reputationTextLabel.setText("REP: " + current + "/" + max);
        reputationBar.setValue(((float) current / max) * 100f);
    }

    public void setXp(int xp, int xpToNext, int level) {
        xpBar.setValue((float) xp / xpToNext * 100f);
        // Automatically updates the text when you level up
        if (levelLabel != null) levelLabel.setText("LV. " + level);
    }

    public void updateTimer(String timeText) {
        if (timerLabel != null) timerLabel.setText(timeText);
    }

    public void updateInventory(List<String> dishes) {
        for (int i = 0; i < 3; i++) {
            Table slot = inventorySlots[i];
            slot.clearChildren();
            if (i < dishes.size()) {
                String dishId = dishes.get(i);
                Image img = new Image(assetService.get(AtlasAsset.OBJECTS).findRegion("Food/" + dishId.replace("_", "-")));
                img.setScaling(Scaling.fit);
                slot.add(img).minSize(50f);
            }
        }
    }
}
