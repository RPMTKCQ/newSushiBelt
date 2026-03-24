package com.sushi.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class Conveyor implements Component {
    public static final ComponentMapper<Conveyor> MAPPER = ComponentMapper.getFor(Conveyor.class);

    public enum Direction { LEFT, RIGHT, UP, DOWN }

    public Direction direction = Direction.RIGHT;
    public float speed = 1f;  // world units per second
}
