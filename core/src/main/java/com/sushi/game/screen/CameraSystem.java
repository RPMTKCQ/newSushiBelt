package com.sushi.game.screen;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.sushi.game.component.CameraFollow;
import com.sushi.game.component.Transform;

public class CameraSystem extends IteratingSystem {

    private final Camera camera;

    public CameraSystem(Camera camera) {
        super(Family.all(CameraFollow.class, Transform.class).get());
        this.camera = camera;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Transform transform = Transform.MAPPER.get(entity);

        camera.position.set(transform.getPosition().x, transform.getPosition().y, camera.position.z);
    }
}
