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
            } else if ("belt_waypoint".equals(name)) {
                int seq = obj.getProperties().get("sequence", 0, Integer.class);
                raw.add(new WaypointEntry(seq, worldPos));
            }
        }

        if (spawnPoint == null || raw.isEmpty()) return;

        raw.sort(Comparator.comparingInt(e -> e.sequence));
        waypoints.clear();
        for (WaypointEntry e : raw) waypoints.add(e.pos);

        beltLoaded = true;
    }

    public Vector2 getSpawnPoint()  { return spawnPoint; }
    public boolean isBeltLoaded()   { return beltLoaded; }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        DishOnBelt dish = DishOnBelt.MAPPER.get(entity);

        if (dish.pickedUp) {
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

        // FIX: Modulo creates an infinite loop around your map waypoints
        int targetIdx = dish.waypointIndex % waypoints.size();

        if (isDishAhead(entity, pos, targetIdx)) return;

        Vector2 target = waypoints.get(targetIdx);
        Vector2 dir    = new Vector2(target).sub(pos);
        float dist     = dir.len();
        float step     = defaultSpeed * deltaTime;

        if (dist <= step) {
            pos.set(target);
            dish.waypointIndex++; // Grows infinitely to track laps
        } else {
            pos.add(dir.nor().scl(step));
        }

        if (dish.body != null) {
            dish.body.setTransform(pos.x, pos.y, 0f);
        }
    }

    // FIX: Using Vector Dot Product for robust collision that works across loops
    private boolean isDishAhead(Entity self, Vector2 myPos, int targetIdx) {
        for (Entity other : getEntities()) {
            if (other == self) continue;
            DishOnBelt otherDish = DishOnBelt.MAPPER.get(other);
            if (otherDish == null || otherDish.pickedUp) continue;

            Transform otherTransform = Transform.MAPPER.get(other);
            if (otherTransform == null) continue;

            float distToOther = myPos.dst(otherTransform.getPosition());

            // If another dish is physically too close
            if (distToOther < MIN_DISH_SPACING) {
                Vector2 target = waypoints.get(targetIdx);

                Vector2 dirToTarget = new Vector2(target).sub(myPos);
                if (dirToTarget.isZero()) continue;
                Vector2 myDir = dirToTarget.nor();

                Vector2 dirToOther = new Vector2(otherTransform.getPosition()).sub(myPos);
                if (dirToOther.isZero()) return true; // Overlapping perfectly
                Vector2 toOther = dirToOther.nor();

                // Dot product checks the angle. > 0.3 means the other dish is directly in front of me!
                if (myDir.dot(toOther) > 0.3f) return true;
            }
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
