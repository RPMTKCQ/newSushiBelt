package com.sushi.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.sushi.game.component.Move;
import com.sushi.game.component.Transform;

public class MoveSystem extends IteratingSystem {
    private final Vector2 normalizedDirection = new Vector2();


    public MoveSystem() {
        super(Family.all(Move.class, Transform.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Move move = Move.MAPPER.get(entity);
        if(move.isRooted() || move.getDirection().isZero()){
    return;
        }

        normalizedDirection.set(move.getDirection()).nor();
        Transform transform = Transform.MAPPER.get(entity);
        Vector2 position = transform.getPosition();
        position.set(
            position.x + move.getMaxSpeed() * normalizedDirection.x * deltaTime,
            position.y + move.getMaxSpeed() * normalizedDirection.y * deltaTime
        );
    }
}
