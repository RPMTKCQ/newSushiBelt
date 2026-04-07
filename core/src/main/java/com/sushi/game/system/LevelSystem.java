package com.sushi.game.system;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.math.MathUtils;
import com.sushi.game.asset.MapAsset;
import com.sushi.game.ui.GameScreenUI;
import com.sushi.game.ui.model.PowerUpType;

public class LevelSystem extends EntitySystem {

    private static final int MAX_LEVEL        = 10;
    private static final int BASE_XP_TO_NEXT  = 30;
    private static final int XP_INCREMENT     = 20;
    private static final int XP_PER_SERVE     = 15;

    private static final int SCORE_ON_TIME    = 10;
    private static final int SCORE_LATE       = 0;
    private static final int MONEY_ON_TIME    = 15;
    private static final int MONEY_LATE       = 8;
    private static final int SORT_SCORE_BONUS = 5;
    private static final int SORT_MONEY_BONUS = 2;

    private final GameScreenUI gameScreenUI;
    private final PowerUpSystem powerUpSystem;
    private final boolean isEndlessMode;
    private final Runnable onWin;
    private final Runnable onGameOver;
    private final MapAsset currentStage;

    private int level         = 1;
    private int xp            = 0;
    private int xpToNextLevel = BASE_XP_TO_NEXT;
    private int score         = 0;
    private int money         = 0;

    private int lastScoreBracket = 0;

    // FIX: Starts at 0 for the Stopwatch!
    private float timeElapsed = 0f;
    private final int financialQuota;
    private final int baseHpPenalty;

    private int reputationHp = 100;
    private static final int MAX_HP = 100;
    private static final int HP_HEAL = 1;

    private float pristineCooldown = 0f;
    private int totalProcessed = 0;

    public LevelSystem(GameScreenUI gameScreenUI, PowerUpSystem powerUpSystem, boolean isEndlessMode, Runnable onWin, Runnable onGameOver, MapAsset currentStage) {
        this.gameScreenUI  = gameScreenUI;
        this.powerUpSystem = powerUpSystem;
        this.isEndlessMode = isEndlessMode;
        this.onWin = onWin;
        this.onGameOver = onGameOver;
        this.currentStage = currentStage;

        // FIX: Dynamic Quotas based on Level!
        this.financialQuota = switch (currentStage) {
            case STAGE_1 -> 150;
            case STAGE_2 -> 300;
            case STAGE_3 -> 500;
            default -> 150;
        };

        // FIX: Dynamic HP Damage based on Level!
        this.baseHpPenalty = switch (currentStage) {
            case STAGE_1 -> 10;
            case STAGE_2 -> 15;
            case STAGE_3 -> 25;
            default -> 10;
        };

        updateStatsUI();
        gameScreenUI.updateTimer("00:00");
    }

    public int getScore() { return score; }
    public int getLevel() { return level; }
    public int getMoney() { return money; }

    @Override
    public void update(float deltaTime) {
        if (pristineCooldown > 0) {
            pristineCooldown -= deltaTime;
            if (pristineCooldown <= 0) {
                gameScreenUI.addLogEvent("Pristine Kitchen is active again!", com.badlogic.gdx.graphics.Color.GREEN);
            }
        }

        // FIX: Stopwatch increments for BOTH modes!
        timeElapsed += deltaTime;
        int minutes = (int) (timeElapsed / 60);
        int seconds = (int) (timeElapsed % 60);
        gameScreenUI.updateTimer(String.format("%02d:%02d", minutes, seconds)); // Accommodates 20:00+ safely!

        if (!isEndlessMode) {
            // TIME ATTACK: Win immediately when quota is reached!
            if (money >= financialQuota) {
                onWin.run();
            }
        }
    }

    public void applyImmediateCorruption(PowerUpType type) {
        if (type == PowerUpType.SUGAR_RUSH) {
            reputationHp -= 5;
            gameScreenUI.addLogEvent("Sugar Rush penalty: -5 HP", com.badlogic.gdx.graphics.Color.RED);
        }
        if (type == PowerUpType.BIONIC_LEGS) {
            reputationHp -= 10;
            gameScreenUI.addLogEvent("Bionic Legs penalty: -10 HP", com.badlogic.gdx.graphics.Color.RED);
        }
        updateStatsUI();
        if (reputationHp <= 0) onGameOver.run();
    }

    public void addMoneyFromPickup(int amount) {
        float moneyMultiplier = powerUpSystem.getDoubleTipsMultiplier();
        if (powerUpSystem.hasPristineKitchen && reputationHp == 100 && pristineCooldown <= 0) {
            moneyMultiplier *= 2f;
        }
        if (powerUpSystem.hasGreedyAlgorithm) {
            moneyMultiplier *= 1.5f;
        }

        int gained = (int) (amount * moneyMultiplier);
        money += gained;
        gameScreenUI.addLogEvent("Collected $" + gained + "!", com.badlogic.gdx.graphics.Color.GREEN);

        updateStatsUI();
    }

    public void onServeCompleted(boolean sortedCorrectly, boolean lateDelivery) {
        totalProcessed++;

        if (!lateDelivery) {
            reputationHp = Math.min(MAX_HP, reputationHp + HP_HEAL);

            if (powerUpSystem.hasGreenTea && MathUtils.randomBoolean(0.25f)) {
                reputationHp = Math.min(MAX_HP, reputationHp + 2);
                gameScreenUI.addLogEvent("Green Tea healed 2 HP!", com.badlogic.gdx.graphics.Color.GREEN);
            }
        }

        float moneyMultiplier = powerUpSystem.getDoubleTipsMultiplier();
        if (powerUpSystem.hasPristineKitchen && reputationHp == 100 && pristineCooldown <= 0) {
            moneyMultiplier *= 2f;
        }
        if (powerUpSystem.hasGreedyAlgorithm) {
            moneyMultiplier *= 1.5f;
        }

        powerUpSystem.consumeServePowerUps();

        int baseScore = lateDelivery ? SCORE_LATE : SCORE_ON_TIME + (sortedCorrectly ? SORT_SCORE_BONUS : 0);
        score += baseScore;

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
        int penalty = baseHpPenalty;
        if (powerUpSystem.hasGreedyAlgorithm) {
            penalty *= 2;
        }

        reputationHp -= penalty;
        gameScreenUI.addLogEvent("A customer left in anger! (-" + penalty + " HP)", com.badlogic.gdx.graphics.Color.RED);

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
            gameScreenUI.setMoney(money + "$ / " + financialQuota + "$");
        }
    }
}
