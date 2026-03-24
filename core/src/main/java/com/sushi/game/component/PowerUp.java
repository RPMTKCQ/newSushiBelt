package com.sushi.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.sushi.game.ui.model.PowerUpType;

public class PowerUp implements Component {
    public static final ComponentMapper<PowerUp> MAPPER = ComponentMapper.getFor(PowerUp.class);

    public PowerUpType type;
    public boolean active = false;

    // for RUSH_HOUR — counts remaining bonus serves
    public int remainingUses = 0;

    // for MOVEMENT_SPEED / COOKING_SPEED — tracks the applied multiplier
    // so PowerUpSystem knows what to undo when it expires
    public float appliedMultiplier = 1f;

    public void reset() {
        type = null;
        active = false;
        remainingUses = 0;
        appliedMultiplier = 1f;
    }
}
