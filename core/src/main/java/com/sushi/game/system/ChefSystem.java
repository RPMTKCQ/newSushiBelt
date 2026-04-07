package com.sushi.game.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.sushi.game.SushiGame;
import com.sushi.game.asset.AssetService;
import com.sushi.game.asset.AtlasAsset;
import com.sushi.game.component.Chef;
import com.sushi.game.component.DishOnBelt;
import com.sushi.game.component.Graphic;
import com.sushi.game.component.Transform;
import com.sushi.game.ui.GameScreenUI;

public class ChefSystem extends IteratingSystem {

    private final GameScreenUI gameScreenUI;
    private final Engine engine;
    private final World world;
    private final AssetService assetService;
    private final ConveyorSystem conveyorSystem;

    public ChefSystem(GameScreenUI gameScreenUI, Engine engine, World world,
                      AssetService assetService, ConveyorSystem conveyorSystem) {
        super(Family.all(Chef.class, Transform.class).get());
        this.gameScreenUI = gameScreenUI;
        this.engine = engine;
        this.world = world;
        this.assetService = assetService;
        this.conveyorSystem = conveyorSystem;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Chef chef = Chef.MAPPER.get(entity);

        switch (chef.state) {
            case IDLE:
                if (gameScreenUI.isChefReady()) {
                    String customerId = gameScreenUI.getFirstCustomerId();
                    String dishToCook = gameScreenUI.getFirstDishName();

                    if (dishToCook != null && customerId != null) {
                        chef.currentRecipeId = dishToCook;
                        chef.cookTimer = 0f;

                        // FIX: Varying the cooking times for balancing!
                        if (dishToCook.equals("maguro_nigiri")) chef.cookDuration = 4f;
                        else if (dishToCook.equals("salmon_nigiri")) chef.cookDuration = 3.5f;
                        else chef.cookDuration = 3f; // tuna_roll

                        chef.state = Chef.ChefState.COOKING;

                        gameScreenUI.setChefReady(false);
                        gameScreenUI.setChefCooking(true);
                        gameScreenUI.startCookingFor(customerId);

                        Gdx.app.log("CHEF", "started cooking: " + dishToCook);
                    } else {
                        gameScreenUI.setChefReady(false);
                    }
                }
                break;

            case COOKING:
                chef.cookTimer += deltaTime;
                float progress = chef.cookTimer / chef.cookDuration;
                gameScreenUI.updateCookingProgress(progress);

                if (chef.cookTimer >= chef.cookDuration) {
                    chef.state = Chef.ChefState.DONE;
                    gameScreenUI.setChefCooking(false);
                    gameScreenUI.resetCookingBar();
                }
                break;

            case DONE:
                spawnDishOnBelt(chef.currentRecipeId);
                gameScreenUI.playCookingDoneSound(); // FIX: SOUND IS FINALLY HOOKED UP!
                chef.state = Chef.ChefState.IDLE;
                chef.currentRecipeId = null;
                Gdx.app.log("CHEF", "finished cooking, dish on belt");
                break;
        }
    }

    private void spawnDishOnBelt(String dishId) {
        if (!conveyorSystem.isBeltLoaded()) return;

        Vector2 position = conveyorSystem.getSpawnPoint();
        TextureAtlas atlas = assetService.get(AtlasAsset.OBJECTS);
        String regionName = "Food/" + dishId.replace("_", "-");
        TextureAtlas.AtlasRegion region = atlas.findRegion(regionName);

        if (region == null) {
            Gdx.app.error("CHEF", "Could not find texture for dish: " + regionName);
            return;
        }

        Vector2 size = new Vector2(region.getRegionWidth(), region.getRegionHeight()).scl(SushiGame.UNIT_SCALE);

        Entity dish = engine.createEntity();
        dish.add(new Transform(position.cpy(), 3, size, new Vector2(1f, 1f), 0f));
        dish.add(new Graphic(Color.WHITE.cpy(), region));

        DishOnBelt dishOnBelt = new DishOnBelt();
        dishOnBelt.dishId = dishId;
        dishOnBelt.pickedUp = false;
        dishOnBelt.waypointIndex = 0;
        dish.add(dishOnBelt);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        bodyDef.position.set(position);

        Body body = world.createBody(bodyDef);
        body.setUserData(dish);

        CircleShape shape = new CircleShape();
        shape.setRadius(size.x * 0.4f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        body.createFixture(fixtureDef);
        shape.dispose();

        dishOnBelt.body = body;
        engine.addEntity(dish);
        Gdx.app.log("CHEF", "spawned " + dishId + " at " + position);
    }
}
