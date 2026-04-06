package com.sushi.game.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.sushi.game.component.Chef;
import com.sushi.game.component.Move;
import com.sushi.game.component.PowerUp;
import com.sushi.game.ui.model.PowerUpType;

public class PowerUpSystem extends IteratingSystem {

    private int doubleTipsServesLeft = 0;

    // Buff Flags for other systems to read
    public boolean hasSugarRush = false;
    public boolean hasHeavyLifter = false;
    public boolean hasGreenTea = false;
    public boolean hasPristineKitchen = false;
    public boolean hasGreedyAlgorithm = false;

    private final Engine engine;

    public PowerUpSystem(Engine engine) {
        super(Family.all(PowerUp.class).get());
        this.engine = engine;
    }

    public void applyPowerUp(PowerUpType type) {
        Entity entity = new Entity();
        PowerUp powerUp = new PowerUp();
        powerUp.type = type;
        powerUp.active = true;

        switch (type) {
            case BASIC_MOVE_SPEED -> applyMovementSpeed(1.15f);
            case BASIC_COOK_SPEED -> applyCookingSpeed(1.15f);
            case SUGAR_RUSH -> {
                hasSugarRush = true;
                applyMovementSpeed(1.40f);
            }
            case HEAVY_LIFTER -> {
                hasHeavyLifter = true;
                applyMovementSpeed(0.85f);
                // Note: You must update your Inventory system to allow 5 items if hasHeavyLifter is true!
            }
            case DOUBLE_TIPS -> doubleTipsServesLeft += 3;
            case GREEN_TEA -> hasGreenTea = true;
            case PRISTINE_KITCHEN -> hasPristineKitchen = true;
            case GREEDY_ALGORITHM -> hasGreedyAlgorithm = true;
        }

        entity.add(powerUp);
        engine.addEntity(entity);
        Gdx.app.log("POWERUP", "Applied: " + type.displayName());
    }

    private void applyMovementSpeed(float multiplier) {
        for (Entity e : engine.getEntitiesFor(Family.all(Move.class).get())) {
            Move move = Move.MAPPER.get(e);
            move.setMaxSpeed(move.getMaxSpeed() * multiplier);
        }
    }

    private void applyCookingSpeed(float multiplier) {
        for (Entity e : engine.getEntitiesFor(Family.all(Chef.class).get())) {
            Chef chef = Chef.MAPPER.get(e);
            chef.cookDuration /= multiplier;
        }
    }

    public float getDoubleTipsMultiplier() { return doubleTipsServesLeft > 0 ? 2.0f : 1.0f; }

    public void consumeServePowerUps() {
        if (doubleTipsServesLeft > 0) {
            doubleTipsServesLeft--;
            if (doubleTipsServesLeft == 0) {
                Gdx.app.log("POWERUP", "Double Tips ended.");
            }
        }
    }

    // Helper for customer patience drain
    public float getPatienceDrainMultiplier() {
        float drain = 1.0f;
        if (hasSugarRush) drain += 0.20f;
        if (hasGreedyAlgorithm) drain += 0.15f;
        return drain;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {}
}
