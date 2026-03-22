package com.sushi.game.factory;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.sushi.game.asset.AssetService;
import com.sushi.game.component.Chef;

public class ChefFactory extends EntityFactory {
    private static final String CHEF_SPRITE = "chef_idle"; // replace with real atlas name

    public ChefFactory(Engine engine, AssetService assetService) {
        super(engine, assetService); // no World needed — chef has no physics body
    }

    public Entity createChef(float x, float y, int stationId) {
        // base handles Transform + Graphic
        Entity entity = createBase(x, y, 1, CHEF_SPRITE);

        // chef-specific: station assignment + recipe logic
        Chef chef = new Chef();
        chef.stationId = stationId;
        entity.add(chef);

        engine.addEntity(entity);
        return entity;
    }
}
