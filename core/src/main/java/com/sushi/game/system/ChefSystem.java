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
        this.gameScreenUI   = gameScreenUI;
        this.engine         = engine;
        this.world          = world;
        this.assetService   = assetService;
        this.conveyorSystem = conveyorSystem;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Chef chef = Chef.MAPPER.get(entity);

        switch (chef.state) {
            case IDLE:
                tryStartCooking(chef);
                break;
            case COOKING:
                chef.cookTimer += deltaTime;
                if (chef.cookDuration > 0f) {
                    gameScreenUI.updateCookingProgress(chef.cookTimer / chef.cookDuration);
                }
                if (chef.cookTimer >= chef.cookDuration) {
                    finishCooking(entity, chef);
                }
                break;
            case DONE:
                break;
        }
    }

    private void tryStartCooking(Chef chef) {
        if (!gameScreenUI.isChefReady()) return;

        String nextDish = gameScreenUI.getFirstDishName();
        if (nextDish == null) return;

        chef.currentRecipeId = nextDish;
        chef.cookDuration    = getCookDuration(nextDish);
        chef.cookTimer       = 0f;
        chef.state           = Chef.ChefState.COOKING;
        gameScreenUI.setChefReady(false);
        gameScreenUI.setChefCooking(true);
        Gdx.app.log("CHEF", "started cooking: " + nextDish);
    }

    private void finishCooking(Entity chefEntity, Chef chef) {
        chef.cookTimer = 0f;
        spawnDishOnBelt(chef.currentRecipeId);
        gameScreenUI.removeFirstOrder();
        gameScreenUI.setChefCooking(false);
        gameScreenUI.resetCookingBar();
        chef.state           = Chef.ChefState.IDLE;
        chef.currentRecipeId = null;
        Gdx.app.log("CHEF", "finished cooking, dish on belt");
    }

    private void spawnDishOnBelt(String dishId) {
        if (!conveyorSystem.isBeltLoaded()) {
            Gdx.app.log("CHEF", "belt not loaded, can't spawn dish!");
            return;
        }

        String regionName = "Food/" + dishId.replace("_", "-");
        TextureAtlas.AtlasRegion region =
            assetService.get(AtlasAsset.OBJECTS).findRegion(regionName);
        if (region == null) {
            Gdx.app.log("CHEF", "WARNING: region not found: " + regionName);
            return;
        }

        Vector2 position = conveyorSystem.getSpawnPoint().cpy();
        Vector2 size = new Vector2(region.getRegionWidth(), region.getRegionHeight())
            .scl(SushiGame.UNIT_SCALE);

        Entity dish = engine.createEntity();
        dish.add(new Transform(position.cpy(), 3, size, new Vector2(1f, 1f), 0f));
        dish.add(new Graphic(Color.WHITE.cpy(), region));

        DishOnBelt dishOnBelt = new DishOnBelt();
        dishOnBelt.dishId        = dishId;
        dishOnBelt.pickedUp      = false;
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
        fixtureDef.shape    = shape;
        fixtureDef.isSensor = true;
        body.createFixture(fixtureDef);
        shape.dispose();

        // store body in DishOnBelt — NOT as a Physic component so PhysicSystem
        // never interpolates the dish transform (which caused the teleport bug)
        dishOnBelt.body = body;

        engine.addEntity(dish);

        Gdx.app.log("CHEF", "spawned " + dishId + " at " + position);
    }

    private float getCookDuration(String dishId) {
        switch (dishId) {
            case "tuna_roll":     return 5f;
            case "salmon_nigiri": return 6f;
            case "maguro_nigiri": return 7f;
            default:              return 5f;
        }
    }
}
