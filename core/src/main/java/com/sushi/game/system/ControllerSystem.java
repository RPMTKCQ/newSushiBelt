package com.sushi.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.sushi.game.asset.SoundAsset;
import com.sushi.game.audio.AudioService;
import com.sushi.game.component.*;
import com.sushi.game.input.Command;
import com.sushi.game.component.Physic;
import com.sushi.game.component.Customer;
import com.sushi.game.component.Interactable;

public class ControllerSystem extends IteratingSystem {

    private final AudioService audioService;
    private final World world;

    public ControllerSystem(AudioService audioService, World world) {
        super(Family.all(Controller.class).get());
        this.audioService = audioService;
        this.world = world;
    }

    private void startGiveFood(Entity playerEntity) {
        Physic physic = Physic.MAPPER.get(playerEntity);
        if (physic == null) return;

        // get player position in world units
        Vector2 playerPos = physic.getBody().getPosition();
        float range = 1.2f; // interaction range in world units — tweak this

        // query a box around the player for any nearby interactable
        world.QueryAABB(fixture -> {
                Object userData = fixture.getBody().getUserData();
                if (!(userData instanceof Entity)) return true; // keep searching

                Entity nearby = (Entity) userData;
                Interactable interactable = Interactable.MAPPER.get(nearby);
                if (interactable == null) return true; // not interactable, keep searching

                Customer customer = Customer.MAPPER.get(nearby);
                if (customer == null) return true; // has no customer component, keep searching

                // act based on customer state
                switch (customer.state) {
                    case WAITING -> seatCustomer(nearby, customer);
                    case ORDERING -> deliverFood(nearby, customer, playerEntity);
                    default -> {
                    } // nothing to do in other states
                }

                return false; // stop querying, found one
            },
            playerPos.x - range, playerPos.y - range,
            playerPos.x + range, playerPos.y + range);
    }

    private void seatCustomer(Entity customerEntity, Customer customer) {
        // CustomerSystem will handle the actual transition
        // just flag it here
        customer.state = Customer.CustomerState.SEATING;
        customer.stateTimer = 0f;
        customer.clickable = false;
    }

    private void deliverFood(Entity customerEntity, Customer customer, Entity playerEntity) {
        // check player is actually carrying food first
        // for now just transition — inventory check comes later
        customer.state = Customer.CustomerState.EATING;
        customer.stateTimer = 0f;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Controller controller = Controller.MAPPER.get(entity);
        if (controller.getPressedCommands().isEmpty() && controller.getReleasedCommands().isEmpty()) {
            return;
        }

        for (Command command : controller.getPressedCommands()) {
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
