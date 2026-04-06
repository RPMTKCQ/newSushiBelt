package com.sushi.game.system;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
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
    private int money         = 0;

    private int lastScoreBracket = 0;

    // Timer & Game Modes
    private float timeLeft = 180f;
    private static final int FINANCIAL_QUOTA = 200;

    // Reputation Tracking
    private int reputationHp = 100;
    private static final int MAX_HP = 100;
    private static final int HP_PENALTY = 5;
    private static final int HP_HEAL = 1;

    // Power Up Cooldowns
    private float pristineCooldown = 0f;

    private int totalProcessed = 0;

    public LevelSystem(GameScreenUI gameScreenUI, PowerUpSystem powerUpSystem, boolean isEndlessMode, Runnable onWin, Runnable onGameOver) {
        this.gameScreenUI  = gameScreenUI;
        this.powerUpSystem = powerUpSystem;
        this.isEndlessMode = isEndlessMode;
        this.onWin = onWin;
        this.onGameOver = onGameOver;

        updateStatsUI();

        if (isEndlessMode) {
            gameScreenUI.updateTimer("ENDLESS");
        }
    }

    public int getScore() { return score; }
    public int getLevel() { return level; }
    public int getMoney() { return money; }

    @Override
    public void update(float deltaTime) {

        // Cooldown tick for Pristine Kitchen
        if (pristineCooldown > 0) {
            pristineCooldown -= deltaTime;
            if (pristineCooldown <= 0) {
                gameScreenUI.addLogEvent("Pristine Kitchen is active again!", com.badlogic.gdx.graphics.Color.GREEN);
            }
        }

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
        } else {
            timeLeft += deltaTime;
            int minutes = (int) (timeLeft / 60);
            int seconds = (int) (timeLeft % 60);
            gameScreenUI.updateTimer(String.format("%d:%02d", minutes, seconds));
        }
    }

    // FIX: Added 'patienceRatio' to the signature so we can calculate Green Tea and Greedy Algorithm!
    // YOU MUST PASS THIS FROM YOUR CUSTOMER SYSTEM NOW! (e.g. 1.0 = full patience, 0.1 = almost angry)
    public void onServeCompleted(boolean sortedCorrectly, boolean lateDelivery, float patienceRatio) {
        totalProcessed++;

        if (!lateDelivery) {
            reputationHp = Math.min(MAX_HP, reputationHp + HP_HEAL);
        }

        // Green Tea Logic
        if (powerUpSystem.hasGreenTea && patienceRatio > 0.20f) {
            if (MathUtils.randomBoolean(0.5f)) { // 50% chance
                reputationHp = Math.min(MAX_HP, reputationHp + 5);
                gameScreenUI.addLogEvent("Green Tea healed 5 HP!", com.badlogic.gdx.graphics.Color.GREEN);
            }
        }

        float moneyMultiplier = powerUpSystem.getDoubleTipsMultiplier();

        // Pristine Kitchen Logic
        if (powerUpSystem.hasPristineKitchen && reputationHp == 100 && pristineCooldown <= 0) {
            moneyMultiplier *= 2f;
        }

        // Greedy Algorithm Logic (Up to 3x tips based on how low patience is)
        if (powerUpSystem.hasGreedyAlgorithm) {
            float greedMultiplier = 1f + (2f * (1f - patienceRatio));
            moneyMultiplier *= greedMultiplier;
        }

        powerUpSystem.consumeServePowerUps();

        int baseScore = lateDelivery ? SCORE_LATE : SCORE_ON_TIME + (sortedCorrectly ? SORT_SCORE_BONUS : 0);
        score += baseScore; // Score doesn't get multiplied

        // If Green Tea is active, base money is 0!
        int baseMoney = powerUpSystem.hasGreenTea ? 0 : (lateDelivery ? MONEY_LATE : MONEY_ON_TIME + (sortedCorrectly ? SORT_MONEY_BONUS : 0));
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

        updateStatsUI();
    }

    public void onCustomerLeftAngry() {
        totalProcessed++;
        reputationHp -= HP_PENALTY;

        gameScreenUI.addLogEvent("A customer left in anger! (-" + HP_PENALTY + " HP)", com.badlogic.gdx.graphics.Color.RED);

        // Pristine Kitchen Cooldown Trigger
        if (powerUpSystem.hasPristineKitchen) {
            pristineCooldown = 30f;
            gameScreenUI.addLogEvent("Pristine Kitchen disabled for 30s!", com.badlogic.gdx.graphics.Color.ORANGE);
        }

        updateStatsUI();

        if (reputationHp <= 0) {
            gameScreenUI.addLogEvent("REPUTATION DEPLETED! RESTAURANT CLOSED!", com.badlogic.gdx.graphics.Color.RED);
            onGameOver.run();
        }
    }

    private void levelUp() {
        level++;
        xp -= xpToNextLevel;
        xpToNextLevel = BASE_XP_TO_NEXT + ((level - 1) * XP_INCREMENT);

        gameScreenUI.addLogEvent("Level Up! Choose a perk.", com.badlogic.gdx.graphics.Color.CYAN);
        gameScreenUI.showPowerUpOverlay(powerUpSystem);
        updateStatsUI();
    }

    public void addDebugXp(int amount) {
        xp += amount;
        while (xp >= xpToNextLevel && level < MAX_LEVEL) {
            levelUp();
        }
        updateStatsUI();
    }

    private void updateStatsUI() {
        gameScreenUI.setReputation(reputationHp, MAX_HP);
        gameScreenUI.setScore(String.valueOf(score));
        gameScreenUI.setXp(xp, xpToNextLevel, level);

        if (isEndlessMode) {
            gameScreenUI.setMoney(money + "$");
        } else {
            gameScreenUI.setMoney(money + "$ / " + FINANCIAL_QUOTA + "$");
        }
    }
}
