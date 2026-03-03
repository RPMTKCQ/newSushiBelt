package com.sushi.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.sushi.game.SushiGame;
import com.sushi.game.component.CameraFollow;
import com.sushi.game.component.Transform;

public class CameraSystem extends IteratingSystem {
    public static final float CAM_OFFSET_Y = 0.8f;
    public static final float CAM_OFFSET_X = 0.3f;

    private final Camera camera;
    private final Vector2 targetPosition;
    private final float smoothingFactor;
    private float mapWidth;
    private float mapHeight;

    public CameraSystem(Camera camera) {
        super(Family.all(CameraFollow.class, Transform.class).get());
        this.camera = camera;
        this.targetPosition = new Vector2();
        this.smoothingFactor = 3f;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Transform transform = Transform.MAPPER.get(entity);
        calcTargetPosition(transform.getPosition());

        float progress = smoothingFactor * deltaTime;
        float smoothedX = MathUtils.lerp(camera.position.x, this.targetPosition.x, progress);
        float smoothedY = MathUtils.lerp(camera.position.y, this.targetPosition.y, progress);
        camera.position.set(smoothedX, smoothedY, camera.position.z);
    }

    private void calcTargetPosition(Vector2 entityPosition) {
        float targetX = entityPosition.x + CAM_OFFSET_X;
        float camHalfW = camera.viewportWidth * 5f;

        if (mapWidth > camHalfW) {
            float min = Math.min(camHalfW, mapWidth - camHalfW);
            float max = Math.max(camHalfW, mapWidth - camHalfW);
            targetX = MathUtils.clamp(targetX, min, max);
        }

        float targetY = entityPosition.y + CAM_OFFSET_Y;
        float camHalfH = camera.viewportWidth * 5f;

        if (mapHeight > camHalfH) {
            float min = Math.min(camHalfW, mapHeight - camHalfH);
            float max = Math.max(camHalfW, mapHeight - camHalfH);
            targetY = MathUtils.clamp(targetY, min, max);
        }
        this.targetPosition.set(targetX, targetY);
    }

    public void setMap(TiledMap tiledMap) {
        Integer width = tiledMap.getProperties().get("width", 0, Integer.class);
        Integer height = tiledMap.getProperties().get("height", 0, Integer.class);
        Integer tileWidth = tiledMap.getProperties().get("tilewidth", 0, Integer.class);
        Integer tileHeight = tiledMap.getProperties().get("tileheight", 0, Integer.class);

        this.mapWidth = width * tileWidth * SushiGame.UNIT_SCALE;
        this.mapHeight = height * tileHeight * SushiGame.UNIT_SCALE;

        Entity camEntity = getEntities().first();
        if (camEntity == null) {
            return;
        }

        processEntity(camEntity, 0f);
    }
}
