package com.sushi.game.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.sushi.game.component.Chef;
import com.sushi.game.component.Move;
import com.sushi.game.component.PowerUp;
import com.sushi.game.ui.model.PowerUpType;

public class PowerUpSystem extends IteratingSystem {

    // how many bonus-point serves remain globally for RUSH_HOUR
    private int rushHourServesLeft = 0;
    // the active RUSH_HOUR multiplier exposed so ScoreSystem can read it
    private float rushHourScoreMultiplier = 1f;

    private final Engine engine;

    public PowerUpSystem(Engine engine) {
        super(Family.all(PowerUp.class).get());
        this.engine = engine;
    }

    // ── called by PowerUpCardUI when player clicks a card ────────────────────
    public void applyPowerUp(PowerUpType type) {
        // prevent stacking the same type — remove old one first
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
            case MOVEMENT_SPEED:
                powerUp.appliedMultiplier = type.multiplier();
                applyMovementSpeed(powerUp.appliedMultiplier);
                break;

            case COOKING_SPEED:
                powerUp.appliedMultiplier = type.multiplier();
                applyCookingSpeed(powerUp.appliedMultiplier);
                break;

            case RUSH_HOUR:
                powerUp.remainingUses = 3;
                rushHourServesLeft = 3;
                rushHourScoreMultiplier = type.multiplier();
                break;
        }

        entity.add(powerUp);
        engine.addEntity(entity);
    }

    // ── called by your score logic after each successful serve ────────────────
    // returns the score multiplier to apply (1f = no bonus)
    public float onServeCompleted() {
        if (rushHourServesLeft <= 0) return 1f;

        rushHourServesLeft--;

        // find and update the RUSH_HOUR PowerUp component
        for (Entity entity : getEntities()) {
            PowerUp p = PowerUp.MAPPER.get(entity);
            if (p.type == PowerUpType.RUSH_HOUR && p.active) {
                p.remainingUses = rushHourServesLeft;
                if (rushHourServesLeft == 0) {
                    p.active = false;
                    rushHourScoreMultiplier = 1f;
                    engine.removeEntity(entity);
                }
                break;
            }
        }

        return rushHourServesLeft >= 0 ? PowerUpType.RUSH_HOUR.multiplier() : 1f;
    }

    // ── IteratingSystem tick — nothing to poll each frame currently,
    //    but hook is here for future timed powerups (e.g. duration-based) ─────
    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PowerUp p = PowerUp.MAPPER.get(entity);
        if (!p.active) {
            engine.removeEntity(entity);
        }
    }

    // ── internal helpers ─────────────────────────────────────────────────────

    private void applyMovementSpeed(float multiplier) {
        for (Entity e : engine.getEntitiesFor(Family.all(Move.class).get())) {
            Move move = Move.MAPPER.get(e);
            move.setMaxSpeed(move.getMaxSpeed() * multiplier);
        }
    }

    private void applyCookingSpeed(float multiplier) {
        // iterate all Chef components and scale their cook duration
        for (Entity e : engine.getEntitiesFor(Family.all(Chef.class).get())) {
            Chef chef = Chef.MAPPER.get(e);
            chef.cookDuration /= multiplier; // divide = faster cooking
        }
    }

    private void revertPowerUp(PowerUp old) {
        switch (old.type) {
            case MOVEMENT_SPEED:
                for (Entity e : engine.getEntitiesFor(Family.all(Move.class).get())) {
                    Move move = Move.MAPPER.get(e);
                    move.setMaxSpeed(move.getMaxSpeed() / old.appliedMultiplier);
                }
                break;
            case COOKING_SPEED:
                for (Entity e : engine.getEntitiesFor(Family.all(Chef.class).get())) {
                    Chef.MAPPER.get(e).cookDuration *= old.appliedMultiplier;
                }
                break;
            case RUSH_HOUR:
                rushHourServesLeft = 0;
                rushHourScoreMultiplier = 1f;
                break;
        }
    }

    public float getRushHourMultiplier() {
        return rushHourScoreMultiplier;
    }

    public int getRushHourServesLeft() {
        return rushHourServesLeft;
    }
}
