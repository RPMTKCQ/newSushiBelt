package com.sushi.game.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.sushi.game.component.*;
import com.sushi.game.ui.GameScreenUI;

public class ChefSystem extends IteratingSystem {

    private final GameScreenUI gameScreenUI;
    private final Engine engine;

    public ChefSystem(GameScreenUI gameScreenUI, Engine engine) {
        super(Family.all(Chef.class, Transform.class).get());
        this.gameScreenUI = gameScreenUI;
        this.engine       = engine;
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
                if (chef.cookTimer >= chef.cookDuration) {
                    finishCooking(entity, chef);
                }
                break;
            case DONE:
                break;
        }
    }

    private void tryStartCooking(Chef chef) {
        String nextDish = gameScreenUI.getFirstDishName();
        if (nextDish == null) return;

        chef.currentRecipeId = nextDish;
        chef.cookDuration    = getCookDuration(nextDish);
        chef.cookTimer       = 0f;
        chef.state           = Chef.ChefState.COOKING;
        Gdx.app.log("CHEF", "started cooking: " + nextDish);
    }

    private void finishCooking(Entity chefEntity, Chef chef) {
        chef.cookTimer = 0f;

        Transform chefTransform = Transform.MAPPER.get(chefEntity);
        spawnDishOnBelt(chef.currentRecipeId, chefTransform);

        gameScreenUI.removeFirstOrder();

        chef.state           = Chef.ChefState.IDLE;
        chef.currentRecipeId = null;
        Gdx.app.log("CHEF", "finished cooking, dish on belt");
    }

    private void spawnDishOnBelt(String dishId, Transform chefTransform) {
        Entity dish = engine.createEntity();

        Transform t = new Transform(
            chefTransform.getPosition().cpy(),
            2,
            chefTransform.getSize().cpy(),
            chefTransform.getScaling().cpy(),
            0f
        );
        dish.add(t);

        DishOnBelt dishOnBelt = new DishOnBelt();
        dishOnBelt.dishId = dishId;
        dish.add(dishOnBelt);

        engine.addEntity(dish);
    }

    private float getCookDuration(String dishId) {
        switch (dishId) {
            case "tuna_roll":    return 5f;
            case "salmon_roll":  return 6f;
            case "maguro_nigiri":return 7f;
            default:             return 5f;
        }
    }
}
