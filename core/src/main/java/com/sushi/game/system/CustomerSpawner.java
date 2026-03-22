package com.sushi.game.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PointMapObject;
import com.badlogic.gdx.math.Vector2;
import com.sushi.game.SushiGame;
import com.sushi.game.component.Customer;
import com.sushi.game.component.Transform;
import com.sushi.game.factory.CustomerFactory;
import com.sushi.game.factory.TableManager;

import com.badlogic.gdx.utils.Array;

public class CustomerSpawner {
    private static final float SPAWN_INTERVAL = 3f;
    private static final int MAX_CUSTOMERS = 3;

    private final CustomerFactory factory;
    private final TableManager tableManager;
    private final Array<Vector2> spawnPoints = new Array<>();
    private final Array<Vector2> occupiedPoints = new Array<>();
    private float timer = 0f;
    private int currentCustomers = 0;
    private final ImmutableArray<Entity> customers;

    public CustomerSpawner(CustomerFactory factory, TableManager tableManager, Engine engine) {
        this.factory = factory;
        this.tableManager = tableManager;
        this.customers = engine.getEntitiesFor(Family.all(Customer.class).get());
    }

    // call after map loads, pass the objects layer
    public void loadSpawnPoints(MapObjects objects) {
        spawnPoints.clear();
        for (MapObject obj : objects) {
            Gdx.app.log("SPAWNER", "object: '" + obj.getName() + "'");
            if (!"customer_spawn".equals(obj.getName())) continue;

            float x = obj.getProperties().get("x", 0f, Float.class);
            float y = obj.getProperties().get("y", 0f, Float.class);

            spawnPoints.add(new Vector2(x, y));
            Gdx.app.log("SPAWNER", "loaded x=" + x + " y=" + y);
        }
        Gdx.app.log("SPAWNER", "total: " + spawnPoints.size);
    }

    public void update(float delta) {
//        Gdx.app.log("SPAWNER", "timer=" + timer + " customers=" + currentCustomers + " freeTables=" + tableManager.hasFreeTables()); // temp logs
        if (!tableManager.hasFreeTables()) return;
        if (currentCustomers >= MAX_CUSTOMERS) return;
        if (spawnPoints.isEmpty()) return;

        Vector2 freePoint = getFreeSpawnPoint();
        if (freePoint == null) return; // all points occupied

        timer += delta;
        if (timer >= SPAWN_INTERVAL) {
            timer = 0f;
            spawnCustomer();
        }
    }

    private Vector2 getFreeSpawnPoint() {
        for (Vector2 point : spawnPoints) {
            if (!occupiedPoints.contains(point, false)) {
                return point;
            }
        }
        return null;
    }

    private void spawnCustomer() {
        for (Vector2 point : spawnPoints) {
            if (!isPointOccupied(point)) {
                factory.createCustomer(point.x, point.y);
                currentCustomers++;
                return;
            }
        }
    }


    private boolean isPointOccupied(Vector2 point) {
        // check if any existing customer is close to this spawn point
        for (Entity entity : customers) {
            Transform t = Transform.MAPPER.get(entity);
            Vector2 pos = t.getPosition();
            Vector2 scaledPoint = new Vector2(point).scl(SushiGame.UNIT_SCALE);
            if (pos.dst(scaledPoint) < 0.5f) return true;
        }
        return false;
    }

    public void onCustomerLeft() {
        currentCustomers--;
    }

    public void addSpawnPoint(float x, float y) {
        spawnPoints.add(new Vector2(x, y));
        Gdx.app.log("SPAWNER", "manually added spawn point x=" + x + " y=" + y);
    }
}
