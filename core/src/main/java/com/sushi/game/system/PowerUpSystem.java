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
    private float doubleTipsMultiplier = 1f;

    public boolean hasPristineKitchen = false;
    public boolean hasGreenTea = false;
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
            case MOVEMENT_SPEED -> applyMovementSpeed(type.multiplier());
            case COOKING_SPEED -> applyCookingSpeed(type.multiplier());
            case SUGAR_RUSH -> applyMovementSpeed(type.multiplier());
            case BIONIC_LEGS -> applyMovementSpeed(type.multiplier()); // FIX: New Speed Boost
            case SOUS_CHEF -> applyCookingSpeed(type.multiplier()); // FIX: Doubles the cook speed!
            case DOUBLE_TIPS -> {
                doubleTipsServesLeft += 3;
                doubleTipsMultiplier = type.multiplier();
            }
            case PRISTINE_KITCHEN -> hasPristineKitchen = true;
            case GREEN_TEA -> hasGreenTea = true;
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

    public float getDoubleTipsMultiplier() { return doubleTipsMultiplier; }
    public float getRushHourMultiplier() { return 1f; }

    public void consumeServePowerUps() {
        if (doubleTipsServesLeft > 0) {
            doubleTipsServesLeft--;
            if (doubleTipsServesLeft == 0) {
                doubleTipsMultiplier = 1f;
                Gdx.app.log("POWERUP", "Double Tips ended.");
            }
        }
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        // Continuous logic for active powerups if needed
    }
}
