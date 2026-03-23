package com.sushi.game.system;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.sushi.game.ui.GameScreenUI;

public class LevelSystem extends EntitySystem {

    private static final int MAX_LEVEL = 10;

    private final GameScreenUI gameScreenUI;
    private final PowerUpSystem powerUpSystem;

    private int level = 1;
    private int xp = 0;
    private int xpToNextLevel = 2;   // level 1 needs 2 serves
    private int score = 0;
    private int money = 100;

    public LevelSystem(GameScreenUI gameScreenUI, PowerUpSystem powerUpSystem) {
        this.gameScreenUI = gameScreenUI;
        this.powerUpSystem = powerUpSystem;
    }

    public void onServeCompleted(boolean sortedCorrectly) {
        float multiplier = powerUpSystem.onServeCompleted();
        int baseScore = 100;
        int bonus = sortedCorrectly ? 50 : 0;  // bonus for correct sort
        int gained = (int) ((baseScore + bonus) * multiplier);
        score += gained;
        money += sortedCorrectly ? 25 : 15;  // more money if sorted correctly

        xp++;
        if (xp >= xpToNextLevel && level < MAX_LEVEL) levelUp();

        gameScreenUI.setScore(score);
        gameScreenUI.setMoney(money);
        gameScreenUI.setXp(xp, xpToNextLevel, level);

        Gdx.app.log("LEVEL", "serve completed, sorted=" + sortedCorrectly + " gained=" + gained);
    }

    // ── called by CustomerSystem after a successful serve ────────────────────
    public void onServeCompleted() {
        // score — apply rush hour multiplier if active
        float multiplier = powerUpSystem.onServeCompleted();
        int gained = (int) (100 * multiplier);
        score += gained;
        money += 20;

        // xp
        xp++;
        if (xp >= xpToNextLevel && level < MAX_LEVEL) {
            levelUp();
        }

        // sync UI labels + progress bar
        gameScreenUI.setScore(score);
        gameScreenUI.setMoney(money);
        gameScreenUI.setXp(xp, xpToNextLevel, level);
    }

    private void levelUp() {
        level++;
        xp = 0;
        xpToNextLevel = level * 2;   // increasing serves required as level increases

        gameScreenUI.setXp(0, xpToNextLevel, level);
        gameScreenUI.showPowerUpOverlay(powerUpSystem);
    }

    // ── update() not needed — event-driven, not frame-polled ─────────────────
    @Override
    public void update(float delta) {
    }

    public int getLevel() {
        return level;
    }

    public int getScore() {
        return score;
    }

    public int getMoney() {
        return money;
    }
}
