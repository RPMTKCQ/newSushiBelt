package com.sushi.game.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.math.Vector2;
import com.sushi.game.SushiGame;
import com.sushi.game.component.Customer;
import com.sushi.game.component.Transform;
import com.sushi.game.factory.CustomerFactory;
import com.sushi.game.factory.TableManager;
import com.badlogic.gdx.utils.Array;

public class CustomerSpawner {

    private static final float BASE_SPAWN_INTERVAL  = 7f;
    private static final float INTERVAL_REDUCTION   = 1f;
    private static final int   SCORE_PER_REDUCTION  = 100;
    private static final float MIN_SPAWN_INTERVAL   = 4f;

    private final CustomerFactory factory;
    private final TableManager tableManager;
    private final LevelSystem levelSystem;
    private final Array<Vector2> spawnPoints = new Array<>();
    private final ImmutableArray<Entity> customers;

    private float timer = 0f;

    public CustomerSpawner(CustomerFactory factory, TableManager tableManager,
                           Engine engine, LevelSystem levelSystem) {
        this.factory      = factory;
        this.tableManager = tableManager;
        this.levelSystem  = levelSystem;
        this.customers    = engine.getEntitiesFor(Family.all(Customer.class).get());
    }

    public void loadSpawnPoints(MapObjects objects) {
        spawnPoints.clear();
        for (MapObject obj : objects) {
            if (!"customer_spawn".equals(obj.getName())) continue;
            float x = obj.getProperties().get("x", 0f, Float.class);
            float y = obj.getProperties().get("y", 0f, Float.class);
            spawnPoints.add(new Vector2(x, y));
        }
    }

    public void update(float delta) {
        if (!tableManager.hasFreeTables()) return;

        // Base capacity of 3. Adds +1 capacity for every 100 points you scored
        int dynamicMax = Math.min(tableManager.getTotalCapacity(), 3 + (levelSystem.getScore() / SCORE_PER_REDUCTION));

        // Always reads the exact number of active customers in the room
        if (customers.size() >= dynamicMax) return;

        if (spawnPoints.isEmpty()) return;
        Vector2 point = getFreeSpawnPoint();
        if (point == null) return;

        timer += delta;
        float interval = getSpawnInterval();
        if (timer >= interval) {
            timer = 0f;
            factory.createCustomer(point.x, point.y);
        }
    }

    private float getSpawnInterval() {
        int brackets  = levelSystem.getScore() / SCORE_PER_REDUCTION;
        float interval = BASE_SPAWN_INTERVAL - (brackets * INTERVAL_REDUCTION);
        return Math.max(interval, MIN_SPAWN_INTERVAL);
    }

    private Vector2 getFreeSpawnPoint() {
        for (Vector2 point : spawnPoints) {
            if (!isPointOccupied(point)) return point;
        }
        return null;
    }

    private boolean isPointOccupied(Vector2 point) {
        for (Entity entity : customers) {
            Transform t = Transform.MAPPER.get(entity);
            Vector2 scaledPoint = new Vector2(point).scl(SushiGame.UNIT_SCALE);
            if (t.getPosition().dst(scaledPoint) < 0.5f) return true;
        }
        return false;
    }

    public void onCustomerLeft() {}
}
