package com.sushi.game.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.World;
import com.sushi.game.component.Physic;
import com.sushi.game.component.Transform;

public class PhysicSystem extends IteratingSystem implements EntityListener {

    private final World world;
    private final float interval;
    private float accumulator;

    public PhysicSystem(World world, float interval) {
        super(Family.all(Physic.class, Transform.class).get());
        this.world = world;
        this.interval = interval;
        this.accumulator = 0f;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        engine.addEntityListener(getFamily(), this);
    }

    @Override
    public void removedFromEngine(Engine engine) {
        super.removedFromEngine(engine);
        engine.removeEntityListener(this);
    }

    @Override
    public void entityAdded(Entity entity) {
    }

    @Override
    public void entityRemoved(Entity entity) {
    }

    @Override
    public void update(float deltaTime) {
        float frameTime = Math.min(deltaTime, 0.25f);
        this.accumulator += frameTime;

        // CHANGED: Added maxSteps circuit breaker to prevent thread freezing on lag spikes
        int maxSteps = 5;
        while (this.accumulator >= this.interval && maxSteps > 0) {
            for (int i = 0; i < getEntities().size(); ++i) {
                Physic physic = Physic.MAPPER.get(getEntities().get(i));
                if (physic.getBody() != null) {
                    physic.getPrevPosition().set(physic.getBody().getPosition());
                }
            }

            this.world.step(interval, 6, 2);
            this.accumulator -= this.interval;
            maxSteps--;
        }

        float alpha = this.accumulator / this.interval;

        for (int i = 0; i < getEntities().size(); ++i) {
            this.interpolateEntity(getEntities().get(i), alpha);
        }
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
    }

    private void interpolateEntity(Entity entity, float alpha) {
        Transform transform = Transform.MAPPER.get(entity);
        Physic physic = Physic.MAPPER.get(entity);

        if (physic.getBody() == null) return;

        float interpolatedX = MathUtils.lerp(physic.getPrevPosition().x, physic.getBody().getPosition().x, alpha);
        float interpolatedY = MathUtils.lerp(physic.getPrevPosition().y, physic.getBody().getPosition().y, alpha);

        transform.getPosition().set(interpolatedX, interpolatedY);
    }
}
