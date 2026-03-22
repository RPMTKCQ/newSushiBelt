package com.sushi.game.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.sushi.game.component.*;
import com.sushi.game.factory.TableManager;
import com.sushi.game.model.SeatData;
import com.sushi.game.model.TableData;
import com.sushi.game.ui.RecipeQueueUI;
import com.sushi.game.ui.model.RecipeQueue;

public class CustomerSystem extends IteratingSystem {
    private static final float ORDER_DELAY = 5f;  // seconds after sitting to order
    private static final float EAT_DURATION = 8f; // seconds to eat after food delivered

    // simple list of possible orders — expand this later
    private static final String[] POSSIBLE_ORDERS = {
        "tuna_roll", "salmon_roll", "prawn_roll"
    };

    private final World world;
    private final TableManager tableManager;
    private final Engine engine;
    private final RecipeQueueUI recipeQueueUI;

    public CustomerSystem(World world, TableManager tableManager, Engine engine, RecipeQueueUI recipeQueueUI) {
        super(Family.all(Customer.class, Transform.class).get());
        this.world = world;
        this.tableManager = tableManager;
        this.engine = engine;
        this.recipeQueueUI = recipeQueueUI;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Customer customer = Customer.MAPPER.get(entity);
        if (customer.state != Customer.CustomerState.WAITING) {
            customer.stateTimer += deltaTime;
        }

        switch (customer.state) {
            case WAITING:
                // waiting for player to press SELECT nearby
                // ControllerSystem handles the transition to SEATING
                break;

            case SEATING:
                handleSeating(entity, customer);
                break;

            case ORDERING:
                // waiting for player to pick up the order ticket
                // pickup system handles this automatically
                break;

            case EATING:
                if (customer.stateTimer >= EAT_DURATION) {
                    customer.state = Customer.CustomerState.LEAVING;
                    customer.stateTimer = 0f;
                }
                break;

            case LEAVING:
                removeCustomer(entity, customer);
                break;
        }
    }

    private void handleSeating(Entity entity, Customer customer) {
        // only run once when first entering SEATING state
        if (customer.tablePosition.isZero()) {
            TableData table = tableManager.claimFreeTable();
            if (table == null) {
                // no free tables — send customer back to waiting
                customer.state = Customer.CustomerState.WAITING;
                customer.clickable = true;
                customer.stateTimer = 0f;
                Gdx.app.log("SEATING", "no free table!");
                return;
            }
            Gdx.app.log("SEATING", "table found, seats: " + table.seats.size());

            // assign table
            customer.tableId = table.tableId;
            customer.tablePosition.set(table.position);

            for (SeatData seat : table.seats) {
                Gdx.app.log("SEATING", "seat isOccupied: " + seat.isOccupied);
                if (!seat.isOccupied) {
                    seat.isOccupied = true;
                    customer.claimedSeat = seat;
                    customer.tablePosition.set(seat.position);
                    customer.tableId = table.tableId;


                    // teleport to table position
                    Transform transform = Transform.MAPPER.get(entity);
                    transform.getPosition().set(seat.position);

                    // move physics body too
                    Physic physic = Physic.MAPPER.get(entity);
                    if (physic != null && physic.getBody() != null) {
                        physic.getBody().setTransform(seat.position, 0f);
                    }
                    break;
                }
            }


            // assign a random order
            customer.orderItemId = POSSIBLE_ORDERS[MathUtils.random(0, POSSIBLE_ORDERS.length - 1)];
        }

        // wait ORDER_DELAY seconds then start ordering
        if (customer.stateTimer >= ORDER_DELAY) {
            customer.state = Customer.CustomerState.ORDERING;
            customer.stateTimer = 0f;
            recipeQueueUI.addOrder(customer.orderItemId, String.valueOf(customer.tableId));
        }
    }

    private void removeCustomer(Entity entity, Customer customer) {
        // vacate the table
        if (customer.tableId != -1) {
            tableManager.vacateTable(customer.tableId);
        }

        //free the seat
        if (customer.claimedSeat != null) {
            customer.claimedSeat.isOccupied = false;
        }

        // destroy physics body
        Physic physic = Physic.MAPPER.get(entity);
        if (physic != null && physic.getBody() != null) {
            world.destroyBody(physic.getBody());
        }

        // remove from engine
        engine.removeEntity(entity);
    }
}
