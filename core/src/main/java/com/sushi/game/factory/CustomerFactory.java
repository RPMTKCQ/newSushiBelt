package com.sushi.game.factory;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.World;
import com.sushi.game.asset.AssetService;
import com.sushi.game.asset.AtlasAsset;
import com.sushi.game.component.Customer;
import com.sushi.game.component.Transform;

public class CustomerFactory {
    private final Engine engine;
    private final World world;
    private final AssetService assetService;

    private static final String[] SPRITE_NAMES = {
        "customer_a", "customer_b", "customer_c"
    };

    public CustomerFactory(Engine engine, World world, AssetService assetService) {
        this.engine = engine;
        this.world = world;
        this.assetService = assetService;
    }

    public Entity createCustomer (float x, float y) {
        TextureAtlas atlas = assetService.get(AtlasAsset.OBJECTS);
        int spriteIdx = MathUtils.random(0,2);

        Entity entity = engine.createEntity();

        //customer data
        Customer customer = engine.createComponent(Customer.class);
        customer.spriteIndex = spriteIdx;
        entity.add(customer);

        //position
        Transform transform = engine.createComponent(Transform.class);
        transform.getPosition().set(x, y); // might change, add a third variable cuz conflict
    }
}
