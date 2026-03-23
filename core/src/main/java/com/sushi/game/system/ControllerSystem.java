package com.sushi.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.sushi.game.asset.SoundAsset;
import com.sushi.game.audio.AudioService;
import com.sushi.game.component.*;
import com.sushi.game.input.Command;
import com.sushi.game.component.Physic;
import com.sushi.game.component.Customer;
import com.sushi.game.component.Interactable;
import com.sushi.game.ui.GameScreenUI;

public class ControllerSystem extends IteratingSystem {

    private final AudioService audioService;
    private final World world;
    private final GameScreenUI gameScreenUI;

    public ControllerSystem(AudioService audioService, World world, GameScreenUI gameScreenUI) {
        super(Family.all(Controller.class).get());
        this.audioService = audioService;
        this.world = world;
        this.gameScreenUI = gameScreenUI;
    }

    private void startGiveFood(Entity playerEntity) {
        Physic physic = Physic.MAPPER.get(playerEntity);
        if (physic == null) return;

        Inventory inventory = Inventory.MAPPER.get(playerEntity);
        if (inventory == null) return;

        Vector2 playerPos = physic.getBody().getPosition();
        float range = 1.2f;

        world.QueryAABB(fixture -> {
                Object userData = fixture.getBody().getUserData();
                if (!(userData instanceof Entity)) return true;

                Entity nearby = (Entity) userData;

                // ── try pick up dish from belt ────────────────────────────────────
                DishOnBelt dish = DishOnBelt.MAPPER.get(nearby);
                if (dish != null && !dish.pickedUp && !inventory.isFull()) {
                    inventory.addDish(dish.dishId);
                    dish.pickedUp = true;
                    gameScreenUI.updateInventory(inventory.dishes);
                    Gdx.app.log("PICKUP", "picked up: " + dish.dishId);
                    return false;
                }

                // ── try deliver to customer ───────────────────────────────────────
                Customer customer = Customer.MAPPER.get(nearby);
                Interactable interactable = Interactable.MAPPER.get(nearby);
                if (customer == null || interactable == null) return true;

                switch (customer.state) {
                    case WAITING -> seatCustomer(nearby, customer);
                    case ORDERING -> deliverFood(nearby, customer, playerEntity, inventory);
                    default -> {
                    }
                }

                return false;
            },
            playerPos.x - range, playerPos.y - range,
            playerPos.x + range, playerPos.y + range);
    }

    private void seatCustomer(Entity customerEntity, Customer customer) {

        customer.state = Customer.CustomerState.SEATING;
        customer.stateTimer = 0f;
        customer.clickable = false;
    }

    private void deliverFood(Entity customerEntity, Customer customer,
                             Entity playerEntity, Inventory inventory) {
        if (inventory.isEmpty()) {
            Gdx.app.log("DELIVER", "player has no food!");
            return;
        }

        // check player carries the dish the customer actually ordered
        if (!inventory.hasDish(customer.orderItemId)) {
            Gdx.app.log("DELIVER", "wrong dish! customer wants: " + customer.orderItemId);
            return;
        }

        inventory.removeDish(customer.orderItemId);
        gameScreenUI.updateInventory(inventory.dishes);
        customer.state = Customer.CustomerState.EATING;
        customer.stateTimer = 0f;
        Gdx.app.log("DELIVER", "delivered: " + customer.orderItemId);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Controller controller = Controller.MAPPER.get(entity);
        if (controller.getPressedCommands().isEmpty() && controller.getReleasedCommands().isEmpty()) {
            return;
        }

        for (Command command : controller.getPressedCommands()) {
            Gdx.app.log("CMD", "processing: " + command); // temp logs
            switch (command) {
                case UP -> moveEntity(entity, 0f, 1f);
                case DOWN -> moveEntity(entity, 0f, -1f);
                case LEFT -> moveEntity(entity, -1f, 0f);
                case RIGHT -> moveEntity(entity, 1f, 0f);
                case SELECT -> startGiveFood(entity);

            }
        }
        controller.getPressedCommands().clear();

        for (Command command : controller.getReleasedCommands()) {
            switch (command) {
                case UP -> moveEntity(entity, 0f, -1f);
                case DOWN -> moveEntity(entity, 0f, 1f);
                case LEFT -> moveEntity(entity, 1f, 0f);
                case RIGHT -> moveEntity(entity, -1f, 0f);

            }
        }
        controller.getReleasedCommands().clear();
    }

    private void moveEntity(Entity entity, float directionX, float directionY) {
        Move move = Move.MAPPER.get(entity);
        if (move == null) return;
        audioService.playSound(SoundAsset.WALKING);

        move.getDirection().x += directionX;
        move.getDirection().y += directionY;
    }
}
