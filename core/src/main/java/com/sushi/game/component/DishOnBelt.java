package com.sushi.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class DishOnBelt implements Component {
    public static final ComponentMapper<DishOnBelt> MAPPER = ComponentMapper.getFor(DishOnBelt.class);

    public String dishId;        // e.g. "tuna_roll"
    public boolean pickedUp = false;
}
