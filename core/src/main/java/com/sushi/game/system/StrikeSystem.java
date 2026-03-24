package com.sushi.game.system;

import com.badlogic.ashley.core.EntitySystem;
import com.sushi.game.ui.GameScreenUI;

public class StrikeSystem extends EntitySystem {

    private static final float MAX_SATISFACTION    = 100f;
    private static final float GAME_OVER_THRESHOLD = 50f;   // below 50% = game over
    private static final float MISS_PENALTY        = 4f;    // missed order costs 4%
    private static final float SUCCESS_REWARD      = 2f;    // successful delivery gains 2%

    private final GameScreenUI gameScreenUI;
    private final Runnable     onGameOver;

    private float satisfaction = 100f;

    public StrikeSystem(GameScreenUI gameScreenUI, Runnable onGameOver) {
        this.gameScreenUI = gameScreenUI;
        this.onGameOver   = onGameOver;
        gameScreenUI.setSatisfaction(satisfaction);
    }

    // called when a customer leaves without being served (timer ran out)
    public void onCustomerLeft() {
        satisfaction = Math.max(0f, satisfaction - MISS_PENALTY);
        gameScreenUI.setSatisfaction(satisfaction);
        if (satisfaction <= GAME_OVER_THRESHOLD) onGameOver.run();
    }

    // called when a successful delivery is made
    public void onServeCompleted() {
        satisfaction = Math.min(MAX_SATISFACTION, satisfaction + SUCCESS_REWARD);
        gameScreenUI.setSatisfaction(satisfaction);
    }

    public float getSatisfaction() { return satisfaction; }

    @Override public void update(float delta) { }
}
