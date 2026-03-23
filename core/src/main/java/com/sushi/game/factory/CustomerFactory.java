package com.sushi.game.factory;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.sushi.game.SushiGame;
import com.sushi.game.asset.AssetService;
import com.sushi.game.asset.AtlasAsset;
import com.sushi.game.component.*;

public class CustomerFactory extends EntityFactory {

    private static final String[] SPRITE_NAMES = {
        "NPC1/idle_right",
        "NPC2/idle_right",
        "NPC3/idle_right"
    };

    private static final String[] ATLAS_KEYS = {"NPC1", "NPC2", "NPC3"};

    private final World world;

    public CustomerFactory(Engine engine, World world, AssetService assetService) {
        super(engine, assetService);
        this.world = world;
    }

    public Entity createCustomer(float x, float y) {
        int spriteIdx = MathUtils.random(0, 2);


        Entity entity = createBase(x, y, 5, SPRITE_NAMES[spriteIdx]);

        // customer component
        Customer customer = new Customer();
        customer.spriteIndex = spriteIdx;
        entity.add(customer);
        entity.add(new Interactable());

        // animation
        entity.add(new Animation2D(
            AtlasAsset.OBJECTS,
            ATLAS_KEYS[spriteIdx],
            Animation2D.AnimationType.IDLE,
            Animation.PlayMode.LOOP,
            1f
        ));

        entity.add(new Facing(Facing.FacingDirection.RIGHT));

        // temp log
        TextureAtlas atlas = assetService.get(AtlasAsset.OBJECTS);
        TextureAtlas.AtlasRegion testRegion = atlas.findRegion(SPRITE_NAMES[0]);
        Gdx.app.log("CUSTOMER", "region found: " + (testRegion != null ? SPRITE_NAMES[0] : "NULL"));

        // physics body at spawn position
        Vector2 spawnPos = new Vector2(x, y).scl(SushiGame.UNIT_SCALE);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(spawnPos);
        bodyDef.fixedRotation = true;

        Body body = world.createBody(bodyDef);
        body.setUserData(entity);

        // sensor for interaction detection
        CircleShape sensorShape = new CircleShape();
        sensorShape.setRadius(0.4f);
        FixtureDef sensorFixture = new FixtureDef();
        sensorFixture.shape = sensorShape;
        sensorFixture.isSensor = true;
        body.createFixture(sensorFixture);
        sensorShape.dispose();

        // solid fixture for collision with chairs and walls
        CircleShape solidShape = new CircleShape();
        solidShape.setRadius(0.25f);
        FixtureDef solidFixture = new FixtureDef();
        solidFixture.shape = solidShape;
        solidFixture.isSensor = false;
        solidFixture.density = 1f;
        body.createFixture(solidFixture);
        solidShape.dispose();

        entity.add(new Physic(body, spawnPos.cpy()));

        engine.addEntity(entity);
        return entity;
    }
}
