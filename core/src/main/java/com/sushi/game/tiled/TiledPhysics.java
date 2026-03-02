package com.sushi.game.tiled;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.sushi.game.SushiGame;

public final class TiledPhysics {


    public static FixtureDef fixtureDefOf(MapObject mapObject, Vector2 scaling, Vector2 relativeTo) {
        if (mapObject instanceof RectangleMapObject rectMapObj) {
            return rectangleFixtureDef(rectMapObj, scaling, relativeTo);
        } else if (mapObject instanceof EllipseMapObject ellipseMapObj) {
            return ellipseFixtureDef(ellipseMapObj, scaling, relativeTo);
        } else if (mapObject instanceof PolygonMapObject polygonMapObj) {
            Polygon polygon = polygonMapObj.getPolygon();
            float offsetX = polygon.getX() * SushiGame.UNIT_SCALE;
            float offsetY = polygon.getY() * SushiGame.UNIT_SCALE;
            return polygonFixtureDef(polygonMapObj, polygon.getVertices(), offsetX, offsetY, scaling, relativeTo);
        } else if (mapObject instanceof PolylineMapObject polylineMapObj) {
            Polyline polyline = polylineMapObj.getPolyline();
            float offsetX = polyline.getX() * SushiGame.UNIT_SCALE;
            float offsetY = polyline.getY() * SushiGame.UNIT_SCALE;
            return polygonFixtureDef(polylineMapObj, polyline.getVertices(), offsetX, offsetY, scaling, relativeTo);
        } else {
            throw new GdxRuntimeException("Unsupported MapObject: " + mapObject);
        }
    }


    private static FixtureDef rectangleFixtureDef(RectangleMapObject mapObject, Vector2 scaling, Vector2 relativeTo) {
        Rectangle rectangle = mapObject.getRectangle();
        float rectX = rectangle.x;
        float rectY = rectangle.y;
        float rectW = rectangle.width;
        float rectH = rectangle.height;

        float boxX = rectX * SushiGame.UNIT_SCALE * scaling.x - relativeTo.x;
        float boxY = rectY * SushiGame.UNIT_SCALE * scaling.y - relativeTo.y;
        float boxW = rectW * SushiGame.UNIT_SCALE * scaling.x * 0.5f;
        float boxH = rectH * SushiGame.UNIT_SCALE * scaling.y * 0.5f;

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(boxW, boxH, new Vector2(boxX + boxW, boxY + boxH), 0f);
        return fixtureDefOfMapObjectAndShape(mapObject, shape);
    }

    private static FixtureDef ellipseFixtureDef(EllipseMapObject mapObject, Vector2 scaling, Vector2 relativeTo) {
        Ellipse ellipse = mapObject.getEllipse();
        float x = ellipse.x;
        float y = ellipse.y;
        float w = ellipse.width;
        float h = ellipse.height;

        float ellipseX = x * SushiGame.UNIT_SCALE * scaling.x - relativeTo.x;
        float ellipseY = y * SushiGame.UNIT_SCALE * scaling.y - relativeTo.y;
        float ellipseW = w * SushiGame.UNIT_SCALE * scaling.x * 0.5f;
        float ellipseH = h * SushiGame.UNIT_SCALE * scaling.y * 0.5f;

        if (MathUtils.isEqual(ellipseW, ellipseH, 0.1f)) {
            // width and height are equal -> return a circle shape
            CircleShape shape = new CircleShape();
            shape.setPosition(new Vector2(ellipseX + ellipseW, ellipseY + ellipseH));
            shape.setRadius(ellipseW);
            return fixtureDefOfMapObjectAndShape(mapObject, shape);
        }


        final int numVertices = 8;
        float angleStep = MathUtils.PI2 / numVertices;
        Vector2[] vertices = new Vector2[numVertices];

        for (int vertexIdx = 0; vertexIdx < numVertices; vertexIdx++) {
            float angle = vertexIdx * angleStep;
            float offsetX = ellipseW * MathUtils.cos(angle);
            float offsetY = ellipseH * MathUtils.sin(angle);
            vertices[vertexIdx] = new Vector2(ellipseX + ellipseW + offsetX, ellipseY + ellipseH + offsetY);
        }

        PolygonShape shape = new PolygonShape();
        shape.set(vertices);
        return fixtureDefOfMapObjectAndShape(mapObject, shape);
    }

    private static FixtureDef polygonFixtureDef(
        MapObject mapObject, // Could be PolygonMapObject or PolylineMapObject
        float[] polyVertices,
        float offsetX,
        float offsetY,
        Vector2 scaling,
        Vector2 relativeTo
    ) {
        offsetX = (offsetX * scaling.x) - relativeTo.x;
        offsetY = (offsetY * scaling.y) - relativeTo.y;
        float[] vertices = new float[polyVertices.length];
        for (int vertexIdx = 0; vertexIdx < polyVertices.length; vertexIdx += 2) {
            // x-coordinate
            vertices[vertexIdx] = offsetX + polyVertices[vertexIdx] * SushiGame.UNIT_SCALE * scaling.x;
            // y-coordinate
            vertices[vertexIdx + 1] = offsetY + polyVertices[vertexIdx + 1] * SushiGame.UNIT_SCALE * scaling.y;
        }

        ChainShape shape = new ChainShape();
        if (mapObject instanceof PolygonMapObject) {
            shape.createLoop(vertices);
        } else { // PolylineMapObject
            shape.createChain(vertices);
        }
        return fixtureDefOfMapObjectAndShape(mapObject, shape);
    }

    private static FixtureDef fixtureDefOfMapObjectAndShape(MapObject mapObject, Shape shape) {
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = mapObject.getProperties().get("friction", 0f, Float.class);
        fixtureDef.restitution = mapObject.getProperties().get("restitution", 0f, Float.class);
        fixtureDef.density = mapObject.getProperties().get("density", 0f, Float.class);
        fixtureDef.isSensor = mapObject.getProperties().get("sensor", false, Boolean.class);
        return fixtureDef;
    }
}
