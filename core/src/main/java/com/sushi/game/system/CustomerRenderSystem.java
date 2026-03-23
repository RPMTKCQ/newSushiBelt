package com.sushi.game.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.sushi.game.component.*;

public class CustomerRenderSystem extends IteratingSystem {

    private static final float SHOW_ANGER_THRESHOLD = 0.30f;  // show below 30% patience
    private static final float BAR_WIDTH  = 0.8f;
    private static final float BAR_HEIGHT = 0.1f;
    private static final float BAR_OFFSET = 1.2f;  // world units above sprite

    private final ShapeRenderer  shapeRenderer;
    private final OrthographicCamera camera;

    public CustomerRenderSystem(OrthographicCamera camera) {
        super(Family.all(Customer.class, Transform.class).get());
        this.camera        = camera;
        this.shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void update(float deltaTime) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        super.update(deltaTime);
        shapeRenderer.end();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Customer  customer  = Customer.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);

        if (customer.state != Customer.CustomerState.ORDERING) return;

        float ratio = 1f - (customer.stateTimer / customer.maxPatience);
        if (ratio > SHOW_ANGER_THRESHOLD) return;  // still patient, don't show

        float x = transform.getPosition().x - BAR_WIDTH / 2f;
        float y = transform.getPosition().y + BAR_OFFSET;

        // background (dark red)
        shapeRenderer.setColor(0.3f, 0f, 0f, 1f);
        shapeRenderer.rect(x, y, BAR_WIDTH, BAR_HEIGHT);

        // foreground (red → scales with remaining patience)
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(x, y, BAR_WIDTH * ratio, BAR_HEIGHT);
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
