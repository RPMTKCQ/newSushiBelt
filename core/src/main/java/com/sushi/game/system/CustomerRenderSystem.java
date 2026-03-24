package com.sushi.game.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.sushi.game.asset.AssetService;
import com.sushi.game.asset.AtlasAsset;
import com.sushi.game.component.*;

public class CustomerRenderSystem {

    private static final float SHOW_ANGER_THRESHOLD = 0.6f;
    private static final float BAR_WIDTH    = 0.8f;
    private static final float BAR_HEIGHT   = 0.1f;
    private static final float BAR_OFFSET   = 1.2f;
    private static final float BUBBLE_SIZE  = 0.5f;
    private static final float BUBBLE_OFFSET = 0.5f;

    private static final float CHEF_BAR_WIDTH  = 0.8f;
    private static final float CHEF_BAR_HEIGHT = 0.1f;
    private static final float CHEF_BAR_OFFSET = 1.2f;

    private static final Color COLOR_WAITING   = new Color(1f, 1f, 1f, 1f);
    private static final Color COLOR_READY     = new Color(0.3f, 1f, 0.3f, 1f);
    private static final Color COLOR_URGENT    = new Color(1f, 0.4f, 0.4f, 1f);

    private final ShapeRenderer shapeRenderer;
    private final Batch batch;
    private final OrthographicCamera camera;
    private final Skin skin;
    private final AssetService assetService;
    private final Engine engine;
    private final Family customerFamily;
    private final Family chefFamily;
    private final Family playerFamily;
    private final BitmapFont defaultFont; // FIX: Crash-proof fallback

    public CustomerRenderSystem(OrthographicCamera camera, Skin skin, AssetService assetService,
                                Batch batch, Engine engine) {
        this.camera         = camera;
        this.skin           = skin;
        this.assetService   = assetService;
        this.batch          = batch;
        this.engine         = engine;
        this.customerFamily = Family.all(Customer.class, Transform.class).get();
        this.chefFamily     = Family.all(Chef.class, Transform.class).get();
        this.playerFamily   = Family.all(Inventory.class, Controller.class).get();
        this.shapeRenderer  = new ShapeRenderer();
        this.defaultFont    = new BitmapFont();
    }

    public void update(float deltaTime) {
        ImmutableArray<Entity> customers = engine.getEntitiesFor(customerFamily);
        ImmutableArray<Entity> chefs     = engine.getEntitiesFor(chefFamily);

        Inventory playerInventory = getPlayerInventory();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Entity entity : customers) drawCustomerBars(entity);
        for (Entity entity : chefs)     drawChefBar(entity);
        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Entity entity : customers) {
            drawBubble(entity, playerInventory);
            drawPayment(entity);
        }
        batch.end();
    }

    private Inventory getPlayerInventory() {
        ImmutableArray<Entity> players = engine.getEntitiesFor(playerFamily);
        if (players.size() == 0) return null;
        return Inventory.MAPPER.get(players.first());
    }

    private void drawPayment(Entity entity) {
        Customer customer = Customer.MAPPER.get(entity);
        if (customer.state != Customer.CustomerState.PAYING) return;

        Transform transform = Transform.MAPPER.get(entity);

        float bounce = MathUtils.sin(customer.stateTimer * 5f) * 0.15f;
        float x = transform.getPosition().x - 0.25f;
        float y = transform.getPosition().y + BUBBLE_OFFSET + 0.5f + bounce;

        // FIX: If UI skin is missing the font, use the safe default
        BitmapFont font = skin.has("receipt", BitmapFont.class) ? skin.getFont("receipt") : defaultFont;

        boolean wasInteger = font.usesIntegerPositions();
        font.setUseIntegerPositions(false);
        font.getData().setScale(0.015f);

        font.setColor(Color.GREEN);
        font.draw(batch, "$$$", x, y);

        font.getData().setScale(1f);
        font.setUseIntegerPositions(wasInteger);
        font.setColor(Color.WHITE);
    }

    private void drawCustomerBars(Entity entity) {
        Customer  customer  = Customer.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);

        if (customer.state == Customer.CustomerState.WAITING_FOR_FOOD) {
            float ratio = 1f - (customer.stateTimer / customer.maxPatience);
            float x = transform.getPosition().x - BAR_WIDTH / 2f;
            float y = transform.getPosition().y + BAR_OFFSET;

            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
            shapeRenderer.rect(x, y, BAR_WIDTH, BAR_HEIGHT);

            if (ratio > 0.5f) shapeRenderer.setColor(Color.GREEN);
            else              shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(x, y, BAR_WIDTH * ratio, BAR_HEIGHT);
        }
        else if (customer.state == Customer.CustomerState.EATING) {
            // FIX: Draw the Cyan/Blue Eating Progress Bar
            float ratio = customer.stateTimer / 5f; // Eats for 5 seconds
            float x = transform.getPosition().x - BAR_WIDTH / 2f;
            float y = transform.getPosition().y + BAR_OFFSET;

            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
            shapeRenderer.rect(x, y, BAR_WIDTH, BAR_HEIGHT);

            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(x, y, BAR_WIDTH * ratio, BAR_HEIGHT);
        }
    }

    private void drawChefBar(Entity entity) {
        Chef      chef      = Chef.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);

        if (chef.state != Chef.ChefState.COOKING) return;
        if (chef.cookDuration <= 0f) return;

        float ratio = chef.cookTimer / chef.cookDuration;
        float x     = transform.getPosition().x - CHEF_BAR_WIDTH / 2f;
        float y     = transform.getPosition().y + CHEF_BAR_OFFSET;

        shapeRenderer.setColor(0.4f, 0.3f, 0f, 1f);
        shapeRenderer.rect(x, y, CHEF_BAR_WIDTH, CHEF_BAR_HEIGHT);

        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.rect(x, y, CHEF_BAR_WIDTH * ratio, CHEF_BAR_HEIGHT);
    }

    private void drawBubble(Entity entity, Inventory playerInventory) {
        Customer  customer  = Customer.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);

        boolean isOrdering        = customer.state == Customer.CustomerState.ORDERING;
        boolean isWaitingForFood  = customer.state == Customer.CustomerState.WAITING_FOR_FOOD;
        if (!isOrdering && !isWaitingForFood) return;
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

        boolean playerHasDish = isWaitingForFood
            && playerInventory != null
            && playerInventory.hasDish(customer.orderItemId);

        batch.setColor(playerHasDish ? COLOR_READY : COLOR_WAITING);
        batch.draw(region, x - (drawWidth - BUBBLE_SIZE) / 2f, y, drawWidth, drawHeight);
        batch.setColor(Color.WHITE);
    }

    public void dispose() {
        shapeRenderer.dispose();
        defaultFont.dispose();
    }
}
