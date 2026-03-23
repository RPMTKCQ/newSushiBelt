package com.sushi.game.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.sushi.game.asset.AssetService;
import com.sushi.game.asset.AtlasAsset;
import com.sushi.game.component.*;

public class CustomerRenderSystem {

    private static final float SHOW_ANGER_THRESHOLD = 0.30f;
    private static final float BAR_WIDTH    = 0.8f;
    private static final float BAR_HEIGHT   = 0.1f;
    private static final float BAR_OFFSET   = 1.2f;
    private static final float BUBBLE_SIZE  = 0.5f;
    private static final float BUBBLE_OFFSET = 0.5f;

    // chef bar
    private static final float CHEF_BAR_WIDTH  = 0.8f;
    private static final float CHEF_BAR_HEIGHT = 0.1f;
    private static final float CHEF_BAR_OFFSET = 1.2f;

    private final ShapeRenderer shapeRenderer;
    private final Batch batch;
    private final OrthographicCamera camera;
    private final AssetService assetService;
    private final Engine engine;
    private final Family customerFamily;
    private final Family chefFamily;

    public CustomerRenderSystem(OrthographicCamera camera, AssetService assetService,
                                Batch batch, Engine engine) {
        this.camera         = camera;
        this.assetService   = assetService;
        this.batch          = batch;
        this.engine         = engine;
        this.customerFamily = Family.all(Customer.class, Transform.class).get();
        this.chefFamily     = Family.all(Chef.class, Transform.class).get();
        this.shapeRenderer  = new ShapeRenderer();
    }

    public void update(float deltaTime) {
        ImmutableArray<Entity> customers = engine.getEntitiesFor(customerFamily);
        ImmutableArray<Entity> chefs     = engine.getEntitiesFor(chefFamily);

        // pass 1 — shape renderer: anger bars + chef cooking bar
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Entity entity : customers) drawAngerBar(entity);
        for (Entity entity : chefs)     drawChefBar(entity);
        shapeRenderer.end();

        // pass 2 — batch: dish bubbles
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Entity entity : customers) drawBubble(entity);
        batch.end();
    }

    private void drawAngerBar(Entity entity) {
        Customer  customer  = Customer.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);

        if (customer.state != Customer.CustomerState.ORDERING) return;

        float ratio = 1f - (customer.stateTimer / customer.maxPatience);
        if (ratio > SHOW_ANGER_THRESHOLD) return;

        float x = transform.getPosition().x - BAR_WIDTH / 2f;
        float y = transform.getPosition().y + BAR_OFFSET;

        // background
        shapeRenderer.setColor(0.3f, 0f, 0f, 1f);
        shapeRenderer.rect(x, y, BAR_WIDTH, BAR_HEIGHT);

        // foreground
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(x, y, BAR_WIDTH * ratio, BAR_HEIGHT);
    }

    private void drawChefBar(Entity entity) {
        Chef      chef      = Chef.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);

        if (chef.state != Chef.ChefState.COOKING) return;
        if (chef.cookDuration <= 0f) return;

        float ratio = chef.cookTimer / chef.cookDuration;
        float x     = transform.getPosition().x - CHEF_BAR_WIDTH / 2f;
        float y     = transform.getPosition().y + CHEF_BAR_OFFSET;

        // background (dark yellow)
        shapeRenderer.setColor(0.4f, 0.3f, 0f, 1f);
        shapeRenderer.rect(x, y, CHEF_BAR_WIDTH, CHEF_BAR_HEIGHT);

        // foreground (yellow filling up)
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.rect(x, y, CHEF_BAR_WIDTH * ratio, CHEF_BAR_HEIGHT);
    }

    private void drawBubble(Entity entity) {
        Customer  customer  = Customer.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);

        if (customer.state != Customer.CustomerState.ORDERING) return;
        if (customer.orderItemId == null) return;

        TextureAtlas atlas   = assetService.get(AtlasAsset.OBJECTS);
        String regionName    = "Food/" + customer.orderItemId.replace("_", "-");
        TextureRegion region = atlas.findRegion(regionName);
        if (region == null) return;

        float x = transform.getPosition().x - BUBBLE_SIZE / 2f;
        float y = transform.getPosition().y + BUBBLE_OFFSET;

        float aspectRatio = (float) region.getRegionWidth() / region.getRegionHeight();
        float drawWidth   = BUBBLE_SIZE * aspectRatio;
        float drawHeight  = BUBBLE_SIZE;

        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(region, x - (drawWidth - BUBBLE_SIZE) / 2f, y, drawWidth, drawHeight);
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
