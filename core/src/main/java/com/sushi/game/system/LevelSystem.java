package com.sushi.game.system;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.sushi.game.ui.GameScreenUI;

public class LevelSystem extends EntitySystem {

    // --- XP & BALANCING ADJUSTMENTS HERE ---
    private static final int MAX_LEVEL        = 10;
    private static final int BASE_XP_TO_NEXT  = 100;  // XP needed for Level 2
    private static final int XP_INCREMENT     = 50;   // How much harder each level gets (Lvl 3 = 150xp, Lvl 4 = 200xp)
    private static final int XP_PER_SERVE     = 10;   // XP given per delivery

    private static final int SCORE_ON_TIME    = 10;
    private static final int SCORE_LATE       = 0;
    private static final int MONEY_ON_TIME    = 5;
    private static final int MONEY_LATE       = 3;
    private static final int SORT_SCORE_BONUS = 5;
    private static final int SORT_MONEY_BONUS = 2;

    private final GameScreenUI gameScreenUI;
    private final PowerUpSystem powerUpSystem;

    private int level         = 1;
    private int xp            = 0;
    private int xpToNextLevel = BASE_XP_TO_NEXT;
    private int score         = 0;
    private int money         = 100;

    public LevelSystem(GameScreenUI gameScreenUI, PowerUpSystem powerUpSystem) {
        this.gameScreenUI  = gameScreenUI;
        this.powerUpSystem = powerUpSystem;
        gameScreenUI.setScore(score);
        gameScreenUI.setMoney(money);
        gameScreenUI.setXp(xp, xpToNextLevel, level);
    }

    public int getScore() { return score; }
    public int getLevel() { return level; } // ADDED for the Spawner

    public void onServeCompleted(boolean sortedCorrectly, boolean lateDelivery) {
        float multiplier = powerUpSystem.getRushHourMultiplier();
        powerUpSystem.consumeRushHourServeIfActive();
        if (powerUpSystem.getRushHourMultiplier() == 1f) gameScreenUI.hideRushHourIfDepleted();

        int baseScore = lateDelivery ? SCORE_LATE : SCORE_ON_TIME + (sortedCorrectly ? SORT_SCORE_BONUS : 0);
        int gained = (int) (baseScore * multiplier);
        score += gained;

        money += lateDelivery ? MONEY_LATE : MONEY_ON_TIME + (sortedCorrectly ? SORT_MONEY_BONUS : 0);

        xp += XP_PER_SERVE;
        if (xp >= xpToNextLevel && level < MAX_LEVEL) levelUp();

        gameScreenUI.setScore(score);
        gameScreenUI.setMoney(money);
        gameScreenUI.setXp(xp, xpToNextLevel, level);

        Gdx.app.log("LEVEL", "serve done | late=" + lateDelivery + " sorted=" + sortedCorrectly + " score+=" + gained + " xp=" + xp + "/" + xpToNextLevel);
    }

    private void levelUp() {
        level++;
        xp = 0;
        xpToNextLevel = BASE_XP_TO_NEXT + (level - 1) * XP_INCREMENT;
        gameScreenUI.setXp(0, xpToNextLevel, level);
        gameScreenUI.showPowerUpOverlay(powerUpSystem);
        Gdx.app.log("LEVEL", "Leveled up to " + level + "!");
    }
}
