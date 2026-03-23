package com.sushi.game.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.math.Vector2;
import com.sushi.game.SushiGame;
import com.sushi.game.component.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ConveyorSystem extends IteratingSystem {

    private final List<Vector2> waypoints = new ArrayList<>();
    private Vector2 spawnPoint = null;
    private boolean beltLoaded = false;
    private float defaultSpeed = 2f;

    private static final float MIN_DISH_SPACING = 0.6f;

    public ConveyorSystem() {
        super(Family.all(DishOnBelt.class, Transform.class).get());
    }

    public void loadBelt(MapObjects objects) {
        if (beltLoaded) return;

        List<WaypointEntry> raw = new ArrayList<>();

        for (MapObject obj : objects) {
            String name = obj.getName();
            if (name == null) continue;

            float px = obj.getProperties().get("x", 0f, Float.class);
            float py = obj.getProperties().get("y", 0f, Float.class);
            Vector2 worldPos = new Vector2(px, py).scl(SushiGame.UNIT_SCALE);

            if ("belt_spawn".equals(name)) {
                spawnPoint   = worldPos;
                defaultSpeed = obj.getProperties().get("belt_speed", 2f, Float.class);
                Gdx.app.log("CONVEYOR", "spawn point: " + spawnPoint);
            } else if ("belt_waypoint".equals(name)) {
                int seq = obj.getProperties().get("sequence", 0, Integer.class);
                raw.add(new WaypointEntry(seq, worldPos));
                Gdx.app.log("CONVEYOR", "waypoint seq=" + seq + " pos=" + worldPos);
            }
        }

        if (spawnPoint == null) {
            Gdx.app.log("CONVEYOR", "WARNING: no 'belt_spawn' point found!");
            return;
        }
        if (raw.isEmpty()) {
            Gdx.app.log("CONVEYOR", "WARNING: no 'belt_waypoint' points found!");
            return;
        }

        raw.sort(Comparator.comparingInt(e -> e.sequence));
        waypoints.clear();
        for (WaypointEntry e : raw) waypoints.add(e.pos);

        beltLoaded = true;
        Gdx.app.log("CONVEYOR", "belt loaded: " + waypoints.size()
            + " waypoints, speed=" + defaultSpeed);
    }

    public Vector2 getSpawnPoint()  { return spawnPoint; }
    public boolean isBeltLoaded()   { return beltLoaded; }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        DishOnBelt dish = DishOnBelt.MAPPER.get(entity);

        if (dish.pickedUp) {
            // destroy body manually since we're not using Physic component
            if (dish.body != null) {
                dish.body.getWorld().destroyBody(dish.body);
                dish.body = null;
            }
            getEngine().removeEntity(entity);
            return;
        }

        if (!beltLoaded || waypoints.isEmpty()) return;

        Transform transform = Transform.MAPPER.get(entity);
        Vector2 pos = transform.getPosition();

        int targetIdx = dish.waypointIndex;
        if (targetIdx >= waypoints.size()) return;  // at end, stopped

        if (isDishAhead(entity, pos, dish.waypointIndex)) return;

        Vector2 target = waypoints.get(targetIdx);
        Vector2 dir    = new Vector2(target).sub(pos);
        float dist     = dir.len();
        float step     = defaultSpeed * deltaTime;

        if (dist <= step) {
            pos.set(target);
            dish.waypointIndex++;
        } else {
            pos.add(dir.nor().scl(step));
        }

        // sync body position manually (no PhysicSystem involved)
        if (dish.body != null) {
            dish.body.setTransform(pos.x, pos.y, 0f);
        }
    }

    private boolean isDishAhead(Entity self, Vector2 myPos, int myWaypointIdx) {
        for (Entity other : getEntities()) {
            if (other == self) continue;
            DishOnBelt otherDish = DishOnBelt.MAPPER.get(other);
            if (otherDish == null || otherDish.pickedUp) continue;
            if (otherDish.waypointIndex < myWaypointIdx) continue;

            Transform otherTransform = Transform.MAPPER.get(other);
            if (otherTransform == null) continue;

            if (myPos.dst(otherTransform.getPosition()) < MIN_DISH_SPACING) return true;
        }
        return false;
    }

    private static class WaypointEntry {
        final int sequence;
        final Vector2 pos;
        WaypointEntry(int sequence, Vector2 pos) {
            this.sequence = sequence;
            this.pos      = pos;
        }
    }
}
