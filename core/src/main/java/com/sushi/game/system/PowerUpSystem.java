package com.sushi.game.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.sushi.game.component.Chef;
import com.sushi.game.component.Move;
import com.sushi.game.component.PowerUp;
import com.sushi.game.ui.model.PowerUpType;

public class PowerUpSystem extends IteratingSystem {

    private int rushHourServesLeft = 0;
    private float rushHourScoreMultiplier = 1f;

    private int doubleTipsServesLeft = 0;
    private float doubleTipsMultiplier = 1f;

    private final Engine engine;

    public PowerUpSystem(Engine engine) {
        super(Family.all(PowerUp.class).get());
        this.engine = engine;
    }

    public void applyPowerUp(PowerUpType type) {
        for (Entity existing : getEntities()) {
            PowerUp old = PowerUp.MAPPER.get(existing);
            if (old.type == type) {
                revertPowerUp(old);
                engine.removeEntity(existing);
                break;
            }
        }

        Entity entity = new Entity();
        PowerUp powerUp = new PowerUp();
        powerUp.type = type;
        powerUp.active = true;

        switch (type) {
            case MOVEMENT_SPEED -> {
                powerUp.appliedMultiplier = type.multiplier();
                applyMovementSpeed(powerUp.appliedMultiplier);
            }
            case COOKING_SPEED -> {
                powerUp.appliedMultiplier = type.multiplier();
                applyCookingSpeed(powerUp.appliedMultiplier);
            }
            case RUSH_HOUR -> {
                rushHourServesLeft = 3;
                rushHourScoreMultiplier = type.multiplier();
            }
            case DOUBLE_TIPS -> {
                doubleTipsServesLeft = 3;
                doubleTipsMultiplier = type.multiplier();
            }
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

    private void revertPowerUp(PowerUp old) {
        switch (old.type) {
            case MOVEMENT_SPEED -> {
                for (Entity e : engine.getEntitiesFor(Family.all(Move.class).get())) {
                    Move move = Move.MAPPER.get(e);
                    move.setMaxSpeed(move.getMaxSpeed() / old.appliedMultiplier);
                }
            }
            case COOKING_SPEED -> {
                for (Entity e : engine.getEntitiesFor(Family.all(Chef.class).get())) {
                    Chef chef = Chef.MAPPER.get(e);
                    chef.cookDuration *= old.appliedMultiplier;
                }
            }
            case RUSH_HOUR -> {
                rushHourServesLeft = 0;
                rushHourScoreMultiplier = 1f;
            }
            case DOUBLE_TIPS -> {
                doubleTipsServesLeft = 0;
                doubleTipsMultiplier = 1f;
            }
        }
    }

    public float getRushHourMultiplier() { return rushHourScoreMultiplier; }
    public float getDoubleTipsMultiplier() { return doubleTipsMultiplier; }

    public void consumeServePowerUps() {
        if (rushHourServesLeft > 0) {
            rushHourServesLeft--;
            if (rushHourServesLeft == 0) {
                rushHourScoreMultiplier = 1f;
                Gdx.app.log("POWERUP", "Rush Hour ended.");
            }
        }
        if (doubleTipsServesLeft > 0) {
            doubleTipsServesLeft--;
            if (doubleTipsServesLeft == 0) {
                doubleTipsMultiplier = 1f;
                Gdx.app.log("POWERUP", "Double Tips ended.");
            }
        }
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {}
}
