package com.sushi.game.system;

import com.badlogic.ashley.core.EntitySystem;
import com.sushi.game.ui.GameScreenUI;

public class StrikeSystem extends EntitySystem {

    private static final int MAX_STRIKES        = 20;
    private static final int SERVES_TO_REDEEM   = 2;   // serves needed to remove 1 strike

    private final GameScreenUI  gameScreenUI;
    private final Runnable       onGameOver;

    private int strikes          = 0;
    private int servesSinceStrike = 0;

    public StrikeSystem(GameScreenUI gameScreenUI, Runnable onGameOver) {
        this.gameScreenUI = gameScreenUI;
        this.onGameOver   = onGameOver;
    }

    // ── called by CustomerSystem when customer leaves angry ───────────────────
    public void onCustomerLeft() {
        strikes++;
        servesSinceStrike = 0;
        gameScreenUI.setStrikes(strikes, MAX_STRIKES);

        if (strikes >= MAX_STRIKES) {
            onGameOver.run();
        }
    }

    // ── called by LevelSystem after each successful serve ─────────────────────
    public void onServeCompleted() {
        if (strikes <= 0) return;

        servesSinceStrike++;
        if (servesSinceStrike >= SERVES_TO_REDEEM) {
            strikes--;
            servesSinceStrike = 0;
            gameScreenUI.setStrikes(strikes, MAX_STRIKES);
        }
    }

    public int getStrikes() { return strikes; }

    @Override public void update(float delta) { }
}
