package com.sushi.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sushi.game.SushiGame;
import com.sushi.game.component.Graphic;
import com.sushi.game.component.Transform;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RenderSystem extends SortedIteratingSystem implements Disposable {
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final Batch batch;
    private final Viewport viewport;
    private final OrthographicCamera camera;
    private final List<MapLayer> fgdLayers;
    private final List<MapLayer> bgdLayers;

    public RenderSystem(Batch batch, Viewport viewport, OrthographicCamera camera) {
        // FIX: Keep your existing Z-sorting, but add Y-sorting as a tie-breaker!
        super(
            Family.all(Transform.class, Graphic.class).get(),
            new Comparator<Entity>() {
                @Override
                public int compare(Entity e1, Entity e2) {
                    Transform t1 = Transform.MAPPER.get(e1);
                    Transform t2 = Transform.MAPPER.get(e2);

                    // 1. Sort by your existing Z-layer logic first
                    int zCompare = t1.compareTo(t2);
                    if (zCompare != 0) {
                        return zCompare;
                    }
                    // 2. Y-SORTING: If Z is identical, higher Y (further back) draws FIRST
                    return Float.compare(t2.getPosition().y, t1.getPosition().y);
                }
            }
        );
        this.batch = batch;
        this.viewport = viewport;
        this.camera = (OrthographicCamera) viewport.getCamera();
        this.mapRenderer = new OrthogonalTiledMapRenderer(null, SushiGame.UNIT_SCALE, this.batch);
        this.fgdLayers = new ArrayList<>();
        this.bgdLayers = new ArrayList<>();
    }

    @Override
    public void update(float deltaTime) {
        AnimatedTiledMapTile.updateAnimationBaseTime();
        this.viewport.apply();

        batch.begin();
        this.batch.setColor(Color.WHITE);
        this.mapRenderer.setView(this.camera);
        bgdLayers.forEach(mapRenderer::renderMapLayer);

        forceSort();
        super.update(deltaTime);

        this.batch.setColor(Color.WHITE);
        fgdLayers.forEach(mapRenderer::renderMapLayer);
        batch.end();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Transform transform = Transform.MAPPER.get(entity);
        Graphic graphic = Graphic.MAPPER.get(entity);
        if (graphic.getRegion() == null) return;

        Vector2 position = transform.getPosition();
        Vector2 scaling = transform.getScaling();
        Vector2 size = transform.getSize();
        this.batch.setColor(graphic.getColor());
        this.batch.draw(
            graphic.getRegion(),
            position.x - (1f - scaling.x) * size.x * 0.5f,
            position.y - (1f - scaling.y) * size.y * 0.5f,
            size.x * 0.5f, size.y * 0.5f,
            size.x, size.y,
            scaling.x, scaling.y,
            transform.getRotationDeg()
        );
    }

    public void setMap(TiledMap tiledMap) {
        this.mapRenderer.setMap(tiledMap);
        this.fgdLayers.clear();
        this.bgdLayers.clear();

        for (MapLayer layer : tiledMap.getLayers()) {
            if (layer.getClass().equals(MapLayer.class)) continue;

            boolean isForeground = layer.getProperties().get("foreground", false, Boolean.class);
            if (isForeground) {
                fgdLayers.add(layer);
            } else {
                bgdLayers.add(layer);
            }
        }
    }

    @Override
    public void dispose() {
        this.mapRenderer.dispose();
    }
}
