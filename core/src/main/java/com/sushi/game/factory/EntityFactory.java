package com.sushi.game.factory;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.sushi.game.SushiGame;
import com.sushi.game.asset.AssetService;
import com.sushi.game.asset.AtlasAsset;
import com.sushi.game.component.Graphic;
import com.sushi.game.component.Transform;

public class EntityFactory {
    protected final Engine engine;
    protected final AssetService assetService;

    public EntityFactory(Engine engine, AssetService assetService) {
        this.engine = engine;
        this.assetService = assetService;
    }

    protected Entity createBase(float x, float y, int z, String regionName) {
        Entity entity = engine.createEntity();

        TextureAtlas atlas = assetService.get(AtlasAsset.OBJECTS);
        TextureAtlas.AtlasRegion region = atlas.findRegion(regionName);

        Vector2 position = new Vector2(x, y).scl(SushiGame.UNIT_SCALE);
        Vector2 size = new Vector2(region.getRegionWidth(), region.getRegionHeight()).scl(SushiGame.UNIT_SCALE);

        entity.add(new Transform(position, z, size, new Vector2(1f, 1f), 0f));
        entity.add(new Graphic(Color.WHITE.cpy(), region));

        return entity;
    }

}
