package com.sushi.game.factory;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;
import com.sushi.game.SushiGame;
import com.sushi.game.asset.AssetService;
import com.sushi.game.asset.AtlasAsset;
import com.sushi.game.component.*;
import com.sushi.game.component.Transform;

public class CustomerFactory extends EntityFactory {
    private static final String[] SPRITE_NAMES = {
        "NPC1/idle_right",
        "NPC2/idle_right",
        "NPC3/idle_right"
    };

    private final World world;

    public CustomerFactory(Engine engine, World world, AssetService assetService) {
        super(engine, assetService);
        this.world = world;
    }

    public Entity createCustomer(float x, float y) {
        int spriteIdx = MathUtils.random(0, 2);

        String[] ATLAS_KEYS = {"NPC1", "NPC2", "NPC3"};
        String atlasKey = ATLAS_KEYS[spriteIdx];

        Entity entity = createBase(x, y, 1, SPRITE_NAMES[spriteIdx]);

        Customer customer = new Customer();
        customer.spriteIndex = spriteIdx;
        entity.add(customer);
        entity.add(new Interactable()); // might change

        // adds idle animation
        entity.add(new Animation2D(
            AtlasAsset.OBJECTS,  // same atlas as everything else
            atlasKey,            // "NPC1", "NPC2", or "NPC3"
            Animation2D.AnimationType.IDLE,
            Animation.PlayMode.LOOP,
            1f                   // speed multiplier
        ));

        //facing directoin
        entity.add(new Facing(Facing.FacingDirection.RIGHT));

        //temp logs
        TextureAtlas atlas = assetService.get(AtlasAsset.OBJECTS);
        TextureAtlas.AtlasRegion testRegion = atlas.findRegion(SPRITE_NAMES[0]);
        Gdx.app.log("CUSTOMER", "region found: " + (testRegion != null ? SPRITE_NAMES[0] : "NULL - wrong name!"));
        // click detection
        Transform transform = Transform.MAPPER.get(entity);
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(transform.getPosition());
        Body body = world.createBody(bodyDef);
        body.setUserData(entity);

        CircleShape shape = new CircleShape();
        shape.setRadius(0.4f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        body.createFixture(fixtureDef);
        shape.dispose();

        entity.add(new Physic(body, transform.getPosition().cpy()));

        engine.addEntity(entity);
        return entity;
    }
}
