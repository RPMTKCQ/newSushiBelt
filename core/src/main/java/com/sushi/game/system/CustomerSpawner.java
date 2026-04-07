package com.sushi.game.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sushi.game.asset.MapAsset;
import com.sushi.game.component.Customer;
import com.sushi.game.factory.CustomerFactory;
import com.sushi.game.factory.TableManager;

import java.util.ArrayList;
import java.util.List;

public class CustomerSpawner {

    private final CustomerFactory customerFactory;
    private final TableManager tableManager;
    private final Engine engine;
    private final LevelSystem levelSystem;
    private final MapAsset currentStage;
    private final boolean isEndlessMode;

    private final List<Vector2> spawnPoints = new ArrayList<>();
    private float spawnTimer = 0f;
    private float totalEndlessTime = 0f;

    public CustomerSpawner(CustomerFactory customerFactory, TableManager tableManager, Engine engine, LevelSystem levelSystem, MapAsset currentStage, boolean isEndlessMode) {
        this.customerFactory = customerFactory;
        this.tableManager = tableManager;
        this.engine = engine;
        this.levelSystem = levelSystem;
        this.currentStage = currentStage;
        this.isEndlessMode = isEndlessMode;
    }

    public void loadSpawnPoints(com.badlogic.gdx.maps.MapObjects objects) {
        spawnPoints.clear();
        for (com.badlogic.gdx.maps.MapObject obj : objects) {
            if ("customer_spawn".equals(obj.getName())) {
                if (obj instanceof com.badlogic.gdx.maps.objects.RectangleMapObject rectObj) {
                    Rectangle rect = rectObj.getRectangle();
                    spawnPoints.add(new Vector2(rect.x, rect.y));
                }
            }
        }
    }

    private int getMaxCustomers() {
        return switch (currentStage) {
            case STAGE_1 -> 3;
            case STAGE_2 -> 4;
            case STAGE_3 -> 5;
            default -> 3;
        };
    }

    private float getDynamicSpawnInterval() {
        float interval = switch (currentStage) {
            case STAGE_1 -> MathUtils.random(4f, 6f);
            case STAGE_2 -> MathUtils.random(2.5f, 4f);
            case STAGE_3 -> MathUtils.random(1.5f, 2.5f);
            default -> 5f;
        };

        if (isEndlessMode) {
            float minutesSurvived = totalEndlessTime / 60f;
            float multiplier = (float) Math.pow(0.85, minutesSurvived);
            interval *= multiplier;
        }

        return Math.max(0.5f, interval);
    }

    public void update(float delta) {
        if (spawnPoints.isEmpty()) return;

        totalEndlessTime += delta;
        spawnTimer += delta;

        int currentCustomerCount = engine.getEntitiesFor(Family.all(Customer.class).get()).size();
        int maxAllowed = Math.min(getMaxCustomers(), tableManager.getTotalCapacity());

        if (spawnTimer >= getDynamicSpawnInterval() && currentCustomerCount < maxAllowed) {
            spawnTimer = 0f;
            Vector2 baseSpawn = spawnPoints.get(MathUtils.random(0, spawnPoints.size() - 1));

            float offsetX = baseSpawn.x + MathUtils.random(-15f, 15f);
            float offsetY = baseSpawn.y + MathUtils.random(-15f, 15f);

            Entity newCustomer = customerFactory.createCustomer(offsetX, offsetY);
            engine.addEntity(newCustomer);
        }
    }
}
