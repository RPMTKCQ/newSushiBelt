package com.sushi.game.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.World;
import com.sushi.game.component.*;
import com.sushi.game.factory.TableManager;
import com.sushi.game.model.TableData;

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

    public CustomerSystem(World world, TableManager tableManager, Engine engine) {
        super(Family.all(Customer.class, Transform.class).get());
        this.world = world;
        this.tableManager = tableManager;
        this.engine = engine;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Customer customer = Customer.MAPPER.get(entity);
        customer.stateTimer += deltaTime;

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
                return;
            }

            // assign table
            customer.tableId = table.tableId;
            customer.tablePosition.set(table.position);

            // teleport to table position
            Transform transform = Transform.MAPPER.get(entity);
            transform.getPosition().set(table.position);

            // move physics body too
            Physic physic = Physic.MAPPER.get(entity);
            if (physic != null && physic.getBody() != null) {
                physic.getBody().setTransform(table.position, 0f);
            }

            // assign a random order
            customer.orderItemId = POSSIBLE_ORDERS[MathUtils.random(0, POSSIBLE_ORDERS.length - 1)];
        }

        // wait ORDER_DELAY seconds then start ordering
        if (customer.stateTimer >= ORDER_DELAY) {
            customer.state = Customer.CustomerState.ORDERING;
            customer.stateTimer = 0f;
            // order ticket spawning will go here later
        }
    }

    private void removeCustomer(Entity entity, Customer customer) {
        // vacate the table
        if (customer.tableId != -1) {
            tableManager.vacateTable(customer.tableId);
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
