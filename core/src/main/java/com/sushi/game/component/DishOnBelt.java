package com.sushi.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.physics.box2d.Body;

public class DishOnBelt implements Component {
    public static final ComponentMapper<DishOnBelt> MAPPER = ComponentMapper.getFor(DishOnBelt.class);

    public String dishId;
    public boolean pickedUp    = false;
    public int waypointIndex   = 0;
    // body stored here instead of Physic component — keeps dish out of PhysicSystem interpolation
    public Body body           = null;
}
