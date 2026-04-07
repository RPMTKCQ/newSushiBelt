package com.sushi.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
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
    private final LevelSystem levelSystem;

    public ControllerSystem(AudioService audioService, World world, GameScreenUI gameScreenUI,
                            LevelSystem levelSystem) {
        super(Family.all(Controller.class).get());
        this.audioService = audioService;
        this.world        = world;
        this.gameScreenUI = gameScreenUI;
        this.levelSystem  = levelSystem;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Controller controller = Controller.MAPPER.get(entity);
        if (controller.getPressedCommands().isEmpty() &&
            controller.getReleasedCommands().isEmpty()) return;

        for (Command command : controller.getPressedCommands()) {
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
        float range = 1f; //

        final boolean[] interacted = {false};
        world.QueryAABB(fixture -> {
                if (interacted[0]) return false;
                Object userData = fixture.getBody().getUserData();
                if (!(userData instanceof Entity)) return true;

                Entity nearby = (Entity) userData;

                DishOnBelt dish = DishOnBelt.MAPPER.get(nearby);
                if (dish != null && !dish.pickedUp && !inventory.isFull()) {
                    inventory.addDish(dish.dishId);
                    dish.pickedUp = true;
                    gameScreenUI.updateInventory(inventory.dishes);
                    Gdx.app.log("PICKUP", "picked up: " + dish.dishId);
                    interacted[0] = true;
                    return false;
                }

                Customer customer = Customer.MAPPER.get(nearby);
                Interactable interactable = Interactable.MAPPER.get(nearby);
                if (customer == null || interactable == null) return true;

                boolean success = false;
                switch (customer.state) {
                    case WAITING -> {
                        seatCustomer(nearby, customer);
                        success = true;
                    }
                    case ORDERING -> {
                        customer.maxPatience = MathUtils.random(20f, 60f);
                        customer.stateTimer = 0f;

                        gameScreenUI.addOrder(
                            customer.orderItemId,
                            String.valueOf(customer.id),
                            customer.maxPatience
                        );

                        audioService.playSound(SoundAsset.CUSTOMER_ORDER);
                        customer.state = Customer.CustomerState.WAITING_FOR_FOOD;
                        success = true;
                    }
                    case WAITING_FOR_FOOD -> {
                        success = deliverFood(nearby, customer, playerEntity, inventory);
                    }
                    case PAYING -> {
                        collectPayment(nearby, customer);
                        success = true;
                    }
                }

                if (success) {
                    interacted[0] = true;
                    return false;
                }

                return true; //
            },
            playerPos.x - range, playerPos.y - range,
            playerPos.x + range, playerPos.y + range);
    }

    private void submitReceiptToChef(Entity playerEntity) {
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

                boolean sorted = gameScreenUI.isQueueSortedByUrgency();
                gameScreenUI.setLastSubmitSorted(sorted);
                gameScreenUI.setChefReady(true);

                audioService.playSound(SoundAsset.RECEIPT_SUBMIT);

                Gdx.app.log("BUZZER", "submitted: " + firstDish + " | sorted=" + sorted);
                found[0] = true;
                return false;
            },
            playerPos.x - range, playerPos.y - range,
            playerPos.x + range, playerPos.y + range);
    }

    private void seatCustomer(Entity customerEntity, Customer customer) {
        audioService.playSound(SoundAsset.CUSTOMER_SIT);
        customer.state      = Customer.CustomerState.SEATING;
        customer.stateTimer = 0f;
        customer.clickable  = false;
    }

    // FIX: Changed to boolean to report success or failure back to the interaction loop
    private boolean deliverFood(Entity customerEntity, Customer customer,
                                Entity playerEntity, Inventory inventory) {
        if (inventory.isEmpty()) {
            Gdx.app.log("DELIVER", "player has no food!");
            return false;
        }
        if (!inventory.hasDish(customer.orderItemId)) {
            Gdx.app.log("DELIVER", "wrong dish! wants: " + customer.orderItemId);
            return false;
        }

        inventory.removeDish(customer.orderItemId);
        gameScreenUI.updateInventory(inventory.dishes);

        customer.servedSorted = gameScreenUI.wasLastSubmitSorted();
        customer.servedLate   = customer.leftAngry;

        gameScreenUI.removeOrder(String.valueOf(customer.id));

        audioService.playSound(SoundAsset.SERVE_FOOD);

        customer.state      = Customer.CustomerState.EATING;
        customer.stateTimer = 0f;
        Gdx.app.log("DELIVER", "delivered: " + customer.orderItemId);
        return true;
    }

    private void collectPayment(Entity customerEntity, Customer customer) {
        levelSystem.onServeCompleted(customer.servedSorted, customer.servedLate);

        audioService.playSound(SoundAsset.TAKE_MONEY);

        customer.state      = Customer.CustomerState.LEAVING;
        customer.stateTimer = 0f;
        levelSystem.addMoneyFromPickup(15);
        Gdx.app.log("PAYMENT", "collected from table " + customer.tableId);
    }

    private void moveEntity(Entity entity, float directionX, float directionY) {
        Move move = Move.MAPPER.get(entity);
        if (move == null) return;

        move.getDirection().x = MathUtils.clamp(move.getDirection().x + directionX, -1f, 1f);
        move.getDirection().y = MathUtils.clamp(move.getDirection().y + directionY, -1f, 1f);
    }
}
