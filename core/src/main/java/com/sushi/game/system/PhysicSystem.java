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
        // Targets entities that have both Physic (Body) and Transform (Visual) components
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
        // Logic for when an entity is added (if needed)
//        System.out.println("This shit fucking works");
    }

    @Override
    public void entityRemoved(Entity entity) {
        // This is the correct place to destroy the body: when the entity leaves the game
        Physic physic = Physic.MAPPER.get(entity);
        if (physic != null && physic.getBody() != null) {
            this.world.destroyBody(physic.getBody());
        }
    }

    @Override
    public void update(float deltaTime) {
        // Cap deltaTime to 0.25s to prevent "Spiral of Death" during lag spikes
        this.accumulator += Math.min(deltaTime, 0.25f);

        while (this.accumulator >= this.interval) {
            // 1. Capture current body positions as "Previous" before we move them
            for (int i = 0; i < getEntities().size(); ++i) {
                Entity entity = getEntities().get(i);
                Physic physic = Physic.MAPPER.get(entity);
                physic.getPrevPosition().set(physic.getBody().getPosition());
            }

            // 2. Advance the physics world by one fixed step
            this.world.step(interval, 6, 2);
            this.accumulator -= this.interval;
        }

        // 3. Calculate how far we are between the previous and current physics state
        float alpha = this.accumulator / this.interval;

        // 4. Update the Transform component (Visuals) for rendering
        for (int i = 0; i < getEntities().size(); ++i) {
            this.interpolateEntity(getEntities().get(i), alpha);
        }
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        // We handle logic inside update() for the fixed-step pattern,
        // so we don't need to do anything per-entity here.
    }

    /**
     * Smooths the visual position of the entity between the last two physics steps.
     */
    private void interpolateEntity(Entity entity, float alpha) {
        Transform transform = Transform.MAPPER.get(entity);
        Physic physic = Physic.MAPPER.get(entity);

        if (physic.getBody() == null) return;

        // Lerp from the previous position to the current native Body position
        float interpolatedX = MathUtils.lerp(physic.getPrevPosition().x, physic.getBody().getPosition().x, alpha);
        float interpolatedY = MathUtils.lerp(physic.getPrevPosition().y, physic.getBody().getPosition().y, alpha);

        transform.getPosition().set(interpolatedX, interpolatedY);

        // Optional: If your Transform has a rotation, you should interpolate that too!
        // transform.setRotation(MathUtils.lerpAngle(physic.getPrevRotation(), physic.getBody().getAngle(), alpha));
    }
}
