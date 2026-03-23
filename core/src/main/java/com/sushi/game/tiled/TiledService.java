package com.sushi.game.tiled;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.sushi.game.SushiGame;
import com.sushi.game.asset.AssetService;
import com.sushi.game.asset.MapAsset;
import com.badlogic.gdx.maps.objects.PointMapObject;

import java.util.function.Consumer;

public class TiledService {
    private final AssetService assetService;
    private final World physicWorld;

    private TiledMap currentMap;

    private Consumer<TiledMap> mapChangeConsumer;
    private Consumer<TiledMapTileMapObject> loadObjectConsumer;
    private LoadTileConsumer loadTileConsumer;

    public TiledService(AssetService assetService, World physicWorld) {
        this.assetService = assetService;
        this.physicWorld = physicWorld;
        this.mapChangeConsumer = null;
        this.loadObjectConsumer = null;
        this.currentMap = null;
        this.loadTileConsumer = null;
    }

    public TiledMap loadMap(MapAsset mapAsset) {
        TiledMap tiledMap = this.assetService.load(mapAsset);
        tiledMap.getProperties().put("mapAsset", mapAsset);

        return tiledMap;

    }

    public void setMap(TiledMap map) {
        if (this.currentMap != null) {
            this.assetService.unload(this.currentMap.getProperties().get("mapAsset", MapAsset.class));


            Array<Body> bodies = new Array<>();
            physicWorld.getBodies(bodies);
            for (Body body : bodies) {
                if ("environment".equals(body.getUserData())) {
                    physicWorld.destroyBody(body);
                }
            }
        }

        this.currentMap = map;
        loadMapObjects(map);
        if (this.mapChangeConsumer != null) {
            this.mapChangeConsumer.accept(map);
        }

    }

    private void loadMapObjects(TiledMap tiledMap) {
        for (MapLayer layer : tiledMap.getLayers()) {
            String name = layer.getName();
            if ("objects".equals(name) || "small-objects".equals(name) || "wall-object".equals(name)) {
                loadingObjectLayer(layer);
            } else if (layer instanceof TiledMapTileLayer tileLayer) {
                loadTileLayer(tileLayer);
            }
        }
        spawnMapBoundary(tiledMap);
    }

    private void spawnMapBoundary(TiledMap tiledMap) {
        Integer width = tiledMap.getProperties().get("width", 0, Integer.class);
        Integer height = tiledMap.getProperties().get("height", 0, Integer.class);
        Integer tileWidth = tiledMap.getProperties().get("tilewidth", 0, Integer.class);
        Integer tileHeight = tiledMap.getProperties().get("tileheight", 0, Integer.class);

        float mapWidth = width * tileWidth * SushiGame.UNIT_SCALE;
        float mapHeight = height * tileHeight * SushiGame.UNIT_SCALE;
        float halfW = mapWidth * 0.5f;
        float halfH = mapHeight * 0.5f;
        float boxThickness = 0.5f;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.setZero();
        bodyDef.fixedRotation = true;
        Body body = physicWorld.createBody(bodyDef);
        body.setUserData("environment");

        // left edge
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(boxThickness, halfH, new Vector2(-boxThickness + 1.1f, halfH), 0f);
        body.createFixture(shape, 0f).setFriction(0f);
        shape.dispose();
        // right edge
        shape = new PolygonShape();
        shape.setAsBox(boxThickness, halfH, new Vector2(mapWidth + boxThickness - 1.1f, halfH), 0f);
        body.createFixture(shape, 0f).setFriction(0f);
        shape.dispose();
        // bottom edge
        shape = new PolygonShape();
        shape.setAsBox(halfW, boxThickness, new Vector2(halfW, -boxThickness), 0f);
        body.createFixture(shape, 0f).setFriction(0f);
        shape.dispose();
        // top edge
        shape = new PolygonShape();
        shape.setAsBox(halfW, boxThickness, new Vector2(halfW, mapHeight + boxThickness - 2f), 0f);
        body.createFixture(shape, 0f).setFriction(0f);
        shape.dispose();


    }


    private void loadTileLayer(TiledMapTileLayer tileLayer) {
        if (loadTileConsumer == null) return;

        for (int y = 0; y < tileLayer.getHeight(); y++) {
            for (int x = 0; x < tileLayer.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
                if (cell == null) continue;

                loadTileConsumer.accept(cell.getTile(), x, y);
            }
        }
    }

    private void loadingObjectLayer(MapLayer objectLayer) {
        if (loadObjectConsumer == null) return;

        for (MapObject mapObject : objectLayer.getObjects()) {
            if (mapObject instanceof TiledMapTileMapObject tileMapObject) {
                loadObjectConsumer.accept(tileMapObject);
            } else if (mapObject instanceof PointMapObject) {
                // skip — used for spawn points, handled separately
            } else {
                // skip non-tile objects (rectangles, polygons etc used for conveyor_belt bounds etc)
                Gdx.app.log("TILED", "skipping non-tile object: "
                    + mapObject.getName() + " (" + mapObject.getClass().getSimpleName() + ")");
            }
        }
    }

    public void setMapChangeConsumer(Consumer<TiledMap> mapChangeConsumer) {
        this.mapChangeConsumer = mapChangeConsumer;
    }

    public void setLoadObjectConsumer(Consumer<TiledMapTileMapObject> loadObjectConsumer) {
        this.loadObjectConsumer = loadObjectConsumer;
    }

    public void setLoadTileConsumer(LoadTileConsumer loadTileConsumer) {
        this.loadTileConsumer = loadTileConsumer;
    }


    @FunctionalInterface
    public interface LoadTileConsumer {
        void accept(TiledMapTile tile, float x, float y);
    }
}
