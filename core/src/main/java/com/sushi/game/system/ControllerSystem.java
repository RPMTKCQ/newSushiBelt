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
import com.sushi.game.ui.GameScreenUI;

public class ControllerSystem extends IteratingSystem {

    private final AudioService audioService;
    private final World world;
    private final GameScreenUI gameScreenUI;

    public ControllerSystem(AudioService audioService, World world, GameScreenUI gameScreenUI) {
        super(Family.all(Controller.class).get());
        this.audioService = audioService;
        this.world        = world;
        this.gameScreenUI = gameScreenUI;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Controller controller = Controller.MAPPER.get(entity);
        if (controller.getPressedCommands().isEmpty() &&
            controller.getReleasedCommands().isEmpty()) return;

        for (Command command : controller.getPressedCommands()) {
            Gdx.app.log("CMD", "processing: " + command);
            switch (command) {
                case UP     -> moveEntity(entity,  0f,  1f);
                case DOWN   -> moveEntity(entity,  0f, -1f);
                case LEFT   -> moveEntity(entity, -1f,  0f);
                case RIGHT  -> moveEntity(entity,  1f,  0f);
                case SELECT -> startGiveFood(entity);
                case SUBMIT -> submitReceiptToChef(entity);
            }
        }
        controller.getPressedCommands().clear();

        for (Command command : controller.getReleasedCommands()) {
            switch (command) {
                case UP    -> moveEntity(entity,  0f, -1f);
                case DOWN  -> moveEntity(entity,  0f,  1f);
                case LEFT  -> moveEntity(entity,  1f,  0f);
                case RIGHT -> moveEntity(entity, -1f,  0f);
            }
        }
        controller.getReleasedCommands().clear();
    }

    private void startGiveFood(Entity playerEntity) {
        Physic physic = Physic.MAPPER.get(playerEntity);
        if (physic == null) return;

        Inventory inventory = Inventory.MAPPER.get(playerEntity);
        if (inventory == null) return;

        Vector2 playerPos = physic.getBody().getPosition();
        float range = 1.2f;

        final boolean[] interacted = {false};
        world.QueryAABB(fixture -> {
                if (interacted[0]) return false;
                Object userData = fixture.getBody().getUserData();
                if (!(userData instanceof Entity)) return true;

                Entity nearby = (Entity) userData;

                // try pick up dish from belt
                DishOnBelt dish = DishOnBelt.MAPPER.get(nearby);
                if (dish != null && !dish.pickedUp && !inventory.isFull()) {
                    inventory.addDish(dish.dishId);
                    dish.pickedUp = true;
                    gameScreenUI.updateInventory(inventory.dishes);
                    Gdx.app.log("PICKUP", "picked up: " + dish.dishId);
                    interacted[0] = true;
                    return false;
                }

                // try interact with customer
                Customer     customer     = Customer.MAPPER.get(nearby);
                Interactable interactable = Interactable.MAPPER.get(nearby);
                if (customer == null || interactable == null) return true;

                switch (customer.state) {
                    case WAITING -> seatCustomer(nearby, customer);
                    case ORDERING -> {
                        gameScreenUI.addOrder(
                            customer.orderItemId,
                            String.valueOf(customer.tableId),
                            customer.maxPatience - customer.stateTimer
                        );
                        customer.state      = Customer.CustomerState.WAITING_FOR_FOOD;
                        customer.stateTimer = 0f;
                    }
                    case WAITING_FOR_FOOD -> deliverFood(nearby, customer, playerEntity, inventory);
                    default -> { return true; }
                }
                interacted[0] = true;
                return false;
            },
            playerPos.x - range, playerPos.y - range,
            playerPos.x + range, playerPos.y + range);
    }

    private void submitReceiptToChef(Entity playerEntity) {
        // block if chef is already busy
        if (gameScreenUI.isChefReady() || gameScreenUI.isChefCooking()) {
            Gdx.app.log("BUZZER", "chef busy, ignoring");
            return;
        }

        Physic physic = Physic.MAPPER.get(playerEntity);
        if (physic == null) return;

        Vector2 playerPos = physic.getBody().getPosition();
        float range = 1.5f;

        final boolean[] found = {false};
        world.QueryAABB(fixture -> {
                if (found[0]) return false;
                Object userData = fixture.getBody().getUserData();
                if (!(userData instanceof Entity)) return true;

                Entity nearby             = (Entity) userData;
                Interactable interactable = Interactable.MAPPER.get(nearby);
                if (interactable == null) return true;
                if (!"buzzer_table".equals(interactable.tag)) return true;

                String firstDish = gameScreenUI.getFirstDishName();
                if (firstDish == null) {
                    Gdx.app.log("BUZZER", "no receipt to submit");
                    return false;
                }

                // snapshot whether the queue was sorted before handing off to chef
                boolean sorted = gameScreenUI.isQueueSortedByUrgency();
                gameScreenUI.setLastSubmitSorted(sorted);
                // pin the cooking bar to this specific card before chef takes over
                gameScreenUI.startCookingFor(gameScreenUI.getFirstCustomerId());
                gameScreenUI.setChefReady(true);

                Gdx.app.log("BUZZER", "submitted: " + firstDish + " | sorted=" + sorted);
                found[0] = true;
                return false;
            },
            playerPos.x - range, playerPos.y - range,
            playerPos.x + range, playerPos.y + range);
    }

    private void seatCustomer(Entity customerEntity, Customer customer) {
        customer.state      = Customer.CustomerState.SEATING;
        customer.stateTimer = 0f;
        customer.clickable  = false;
    }

    private void deliverFood(Entity customerEntity, Customer customer,
                             Entity playerEntity, Inventory inventory) {
        if (inventory.isEmpty()) {
            Gdx.app.log("DELIVER", "player has no food!");
            return;
        }
        if (!inventory.hasDish(customer.orderItemId)) {
            Gdx.app.log("DELIVER", "wrong dish! wants: " + customer.orderItemId);
            return;
        }
        inventory.removeDish(customer.orderItemId);
        gameScreenUI.updateInventory(inventory.dishes);
        gameScreenUI.onOrderDelivered(String.valueOf(customer.tableId));
        // set to EATING with timer already at max so CustomerSystem triggers
        // score/level/strike on the very next frame then immediately goes to LEAVING
        customer.state      = Customer.CustomerState.EATING;
        customer.stateTimer = 999f;
        Gdx.app.log("DELIVER", "delivered: " + customer.orderItemId);
    }

    private void moveEntity(Entity entity, float directionX, float directionY) {
        Move move = Move.MAPPER.get(entity);
        if (move == null) return;
        audioService.playSound(SoundAsset.WALKING);
        move.getDirection().x += directionX;
        move.getDirection().y += directionY;
    }
}
