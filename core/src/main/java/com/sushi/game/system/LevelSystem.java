package com.sushi.game.system;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.sushi.game.ui.GameScreenUI;

public class LevelSystem extends EntitySystem {

    private static final int MAX_LEVEL        = 10;
    private static final int BASE_XP_TO_NEXT  = 30;
    private static final int XP_INCREMENT     = 20;
    private static final int XP_PER_SERVE     = 10;

    private static final int SCORE_ON_TIME    = 10;
    private static final int SCORE_LATE       = 0;
    private static final int MONEY_ON_TIME    = 5;
    private static final int MONEY_LATE       = 3;
    private static final int SORT_SCORE_BONUS = 5;
    private static final int SORT_MONEY_BONUS = 2;

    private final GameScreenUI gameScreenUI;
    private final PowerUpSystem powerUpSystem;
    private final boolean isEndlessMode;
    private final Runnable onWin;
    private final Runnable onGameOver;

    private int level         = 1;
    private int xp            = 0;
    private int xpToNextLevel = BASE_XP_TO_NEXT;
    private int score         = 0;
    private int money         = 100;

    private int lastScoreBracket = 0;

    // Timer & Game Modes
    private float timeLeft = 180f; // 3 minutes
    private static final int FINANCIAL_QUOTA = 200;

    // Reputation Tracking
    private int successfulServes = 0;
    private int totalProcessed = 0;

    public LevelSystem(GameScreenUI gameScreenUI, PowerUpSystem powerUpSystem, boolean isEndlessMode, Runnable onWin, Runnable onGameOver) {
        this.gameScreenUI  = gameScreenUI;
        this.powerUpSystem = powerUpSystem;
        this.isEndlessMode = isEndlessMode;
        this.onWin = onWin;
        this.onGameOver = onGameOver;

        gameScreenUI.setScore(score);
        gameScreenUI.setMoney(money);
        gameScreenUI.setXp(xp, xpToNextLevel, level);

        if (isEndlessMode) {
            gameScreenUI.updateTimer("ENDLESS");
        }
    }

    public int getScore() { return score; }
    public int getLevel() { return level; }
    public int getMoney() { return money; }

    @Override
    public void update(float deltaTime) {
        if (!isEndlessMode) {
            timeLeft -= deltaTime;
            if (timeLeft < 0) timeLeft = 0;

            int minutes = (int) (timeLeft / 60);
            int seconds = (int) (timeLeft % 60);
            gameScreenUI.updateTimer(String.format("%d:%02d", minutes, seconds));

            if (timeLeft <= 0) {
                if (money >= FINANCIAL_QUOTA) {
                    onWin.run();
                } else {
                    gameScreenUI.addLogEvent("FAILED TO MEET QUOTA!", com.badlogic.gdx.graphics.Color.RED);
                    onGameOver.run();
                }
            }
        }
    }

    public void onServeCompleted(boolean sortedCorrectly, boolean lateDelivery) {
        totalProcessed++;
        if (!lateDelivery) {
            successfulServes++;
        }

        float scoreMultiplier = powerUpSystem.getRushHourMultiplier();
        float moneyMultiplier = powerUpSystem.getDoubleTipsMultiplier();

        powerUpSystem.consumeServePowerUps();

        if (powerUpSystem.getRushHourMultiplier() == 1f) {
            gameScreenUI.hideRushHourIfDepleted();
        }

        int baseScore = lateDelivery ? SCORE_LATE : SCORE_ON_TIME + (sortedCorrectly ? SORT_SCORE_BONUS : 0);
        int gainedScore = (int) (baseScore * scoreMultiplier);
        score += gainedScore;

        int baseMoney = lateDelivery ? MONEY_LATE : MONEY_ON_TIME + (sortedCorrectly ? SORT_MONEY_BONUS : 0);
        int gainedMoney = (int) (baseMoney * moneyMultiplier);
        money += gainedMoney;

        int newBracket = score / 100;
        if (newBracket > lastScoreBracket) {
            lastScoreBracket = newBracket;
            gameScreenUI.addLogEvent("Reputation increased! More customers arriving...", com.badlogic.gdx.graphics.Color.GOLD);
        }

        if (lateDelivery) {
            gameScreenUI.addLogEvent("A customer was served late!", com.badlogic.gdx.graphics.Color.RED);
        }

        xp += XP_PER_SERVE;

        if (xp >= xpToNextLevel && level < MAX_LEVEL) {
            levelUp();
        }

        checkReputation();
        gameScreenUI.setScore(score);
        gameScreenUI.setMoney(money);
        gameScreenUI.setXp(xp, xpToNextLevel, level);
    }

    public void onCustomerLeftAngry() {
        totalProcessed++;
        gameScreenUI.addLogEvent("A customer left in anger!", com.badlogic.gdx.graphics.Color.RED);
        checkReputation();
    }

    private void checkReputation() {
        // Grace period of 5 customers before failing you
        if (totalProcessed >= 5) {
            float reputation = (float) successfulServes / totalProcessed;
            gameScreenUI.setSatisfaction(reputation);

            if (reputation < 0.5f) {
                gameScreenUI.addLogEvent("REPUTATION TOO LOW! RESTAURANT CLOSED!", com.badlogic.gdx.graphics.Color.RED);
                onGameOver.run();
            }
        }
    }

    private void levelUp() {
        level++;
        xp -= xpToNextLevel;
        xpToNextLevel = BASE_XP_TO_NEXT + ((level - 1) * XP_INCREMENT);
        gameScreenUI.setXp(xp, xpToNextLevel, level);

        gameScreenUI.addLogEvent("Level Up! Choose a perk.", com.badlogic.gdx.graphics.Color.CYAN);
        gameScreenUI.showPowerUpOverlay(powerUpSystem);
    }
}
