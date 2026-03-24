package com.sushi.game.factory;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.Animation;
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

        Customer customer = new Customer();
        customer.spriteIndex = spriteIdx;

        // NEW BALANCE: Overcooked-style tight patience timer (Randomly between 40-50 seconds)
        // This is perfectly tuned for ~20s perfect-serve sequences.
        customer.maxPatience = MathUtils.random(40f, 50f);

        entity.add(customer);
        entity.add(new Interactable());

        entity.add(new Animation2D(
            AtlasAsset.OBJECTS,
            ATLAS_KEYS[spriteIdx],
            Animation2D.AnimationType.IDLE,
            Animation.PlayMode.LOOP,
            1f
        ));

        entity.add(new Facing(Facing.FacingDirection.RIGHT));

        Vector2 spawnPos = new Vector2(x, y).scl(SushiGame.UNIT_SCALE);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        bodyDef.position.set(spawnPos);
        bodyDef.fixedRotation = true;

        Body body = world.createBody(bodyDef);
        body.setUserData(entity);

        CircleShape sensorShape = new CircleShape();
        sensorShape.setRadius(0.8f);
        sensorShape.setPosition(new Vector2(0.5f, 0.5f));

        FixtureDef sensorFixture = new FixtureDef();
        sensorFixture.shape = sensorShape;
        sensorFixture.isSensor = true;
        body.createFixture(sensorFixture);
        sensorShape.dispose();

        CircleShape solidShape = new CircleShape();
        solidShape.setRadius(0.3f);
        solidShape.setPosition(new Vector2(0.5f, 0.3f));

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
