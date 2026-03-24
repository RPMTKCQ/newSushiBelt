package com.sushi.game.factory;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.sushi.game.asset.AssetService;
import com.sushi.game.asset.AtlasAsset;
import com.sushi.game.component.*;

public class ChefFactory extends EntityFactory {

    public ChefFactory(Engine engine, AssetService assetService) {
        super(engine, assetService);
    }

    public Entity createChef(float x, float y, int stationId) {
        Entity entity = createBase(x, y, 1, "Chef/idle_right");

        Chef chef = new Chef();
        chef.stationId = stationId;
        entity.add(chef);

        entity.add(new Animation2D(
            AtlasAsset.OBJECTS,
            "Chef",
            Animation2D.AnimationType.IDLE,
            Animation.PlayMode.LOOP,
            1f
        ));

        entity.add(new Facing(Facing.FacingDirection.RIGHT));

        engine.addEntity(entity);
        return entity;
    }
}
