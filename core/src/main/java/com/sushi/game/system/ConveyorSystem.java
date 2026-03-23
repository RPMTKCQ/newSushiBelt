package com.sushi.game.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.sushi.game.component.*;

public class ConveyorSystem extends IteratingSystem {

    // belt bounds in world units — set these to match your Tiled belt object
    private static final float BELT_MIN_X = 5f;
    private static final float BELT_MAX_X = 20f;
    private static final float BELT_MIN_Y = 5f;
    private static final float BELT_MAX_Y = 20f;

    public ConveyorSystem() {
        super(Family.all(DishOnBelt.class, Transform.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        DishOnBelt dish = DishOnBelt.MAPPER.get(entity);
        if (dish.pickedUp) {
            getEngine().removeEntity(entity);
            return;
        }

        Transform transform = Transform.MAPPER.get(entity);
        Vector2 pos = transform.getPosition();

        // move right along belt
        pos.x += 2f * deltaTime;

        // loop back when reaching the end of the belt
        if (pos.x > BELT_MAX_X) {
            pos.x = BELT_MIN_X;
        }
    }
}
