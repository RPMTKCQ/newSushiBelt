package com.sushi.game.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.sushi.game.factory.ChefFactory;

public class ChefSpawner {

    private final ChefFactory factory;
    private final Array<Vector2> spawnPoints = new Array<>();

    public ChefSpawner(ChefFactory factory) {
        this.factory = factory;
    }

    public void loadSpawnPoints(MapObjects objects) {
        spawnPoints.clear();
        for (MapObject obj : objects) {
            if (!"chef_spawn".equals(obj.getName())) continue;
            float x = obj.getProperties().get("x", 0f, Float.class);
            float y = obj.getProperties().get("y", 0f, Float.class);
            spawnPoints.add(new Vector2(x, y));
            Gdx.app.log("CHEF_SPAWNER", "loaded x=" + x + " y=" + y);
        }
        Gdx.app.log("CHEF_SPAWNER", "total: " + spawnPoints.size);
    }

    // called once after map loads — chefs spawn immediately
    public void spawnAll() {
        for (int i = 0; i < spawnPoints.size; i++) {
            Vector2 point = spawnPoints.get(i);
            factory.createChef(point.x, point.y, i);
            Gdx.app.log("CHEF_SPAWNER", "spawned chef " + i + " at " + point);
        }
    }
}
