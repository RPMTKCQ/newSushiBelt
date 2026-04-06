package com.sushi.game.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.sushi.game.component.*;
import com.sushi.game.factory.TableManager;
import com.sushi.game.model.SeatData;
import com.sushi.game.model.TableData;
import com.sushi.game.ui.GameScreenUI;

public class CustomerSystem extends IteratingSystem {
    private static final float ORDER_DELAY   = MathUtils.random(3f, 5f);
    private static final float SEAT_Y_OFFSET = 0.5f;

    private static final String[] POSSIBLE_ORDERS = {
        "tuna_roll", "salmon_nigiri", "maguro_nigiri"
    };

    private final World world;
    private final TableManager tableManager;
    private final Engine engine;
    private final LevelSystem levelSystem;
    private final GameScreenUI gameScreenUI;

    public CustomerSystem(World world, TableManager tableManager, Engine engine,
                          GameScreenUI gameScreenUI, LevelSystem levelSystem) {
        super(Family.all(Customer.class, Transform.class).get());
        this.world        = world;
        this.tableManager = tableManager;
        this.engine       = engine;
        this.levelSystem  = levelSystem;
        this.gameScreenUI = gameScreenUI;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Customer customer = Customer.MAPPER.get(entity);

        // Timer runs for all states
        customer.stateTimer += deltaTime;

        switch (customer.state) {
            case WAITING:
                // FIX: Exploit patched! If left waiting at the door for 20 seconds, they leave angry.
                if (customer.stateTimer >= 20f) {
                    customer.leftAngry = true;
                    customer.state = Customer.CustomerState.LEAVING;
                    customer.stateTimer = 0f;
                    levelSystem.onCustomerLeftAngry(); // Burns reputation
                    Gdx.app.log("CUSTOMER", "Got tired of standing at the door and left angry!");
                }
                break;

            case SEATING:
                handleSeating(entity, customer);
                break;

            case ORDERING:
                if (customer.stateTimer >= customer.maxPatience) {
                    customer.leftAngry  = true;
                    customer.state      = Customer.CustomerState.LEAVING;
                    customer.stateTimer = 0f;
                    levelSystem.onCustomerLeftAngry();
                    Gdx.app.log("CUSTOMER", "left angry during ordering");
                }
                break;

            case WAITING_FOR_FOOD:
                if (!customer.leftAngry && customer.stateTimer >= customer.maxPatience) {
                    customer.leftAngry = true;
                    levelSystem.onCustomerLeftAngry();
                    Gdx.app.log("CUSTOMER", "patience ran out — satisfaction hit, still waiting");
                }
                break;

            case EATING:
                if (customer.stateTimer >= 5f) {
                    customer.state      = Customer.CustomerState.PAYING;
                    customer.stateTimer = 0f;
                }
                break;

            case PAYING:
                break;

            case LEAVING:
                removeCustomer(entity, customer);
                break;
        }
    }

    private void handleSeating(Entity entity, Customer customer) {
        if (customer.tablePosition.isZero()) {
            TableData table = tableManager.claimFreeTable();
            if (table == null) {
                customer.state      = Customer.CustomerState.WAITING;
                customer.clickable  = true;
                customer.stateTimer = 0f; // Reset wait timer so they don't instantly leave if a table frees up
                Gdx.app.log("SEATING", "no free table!");
                return;
            }

            customer.tableId = table.tableId;
            customer.tablePosition.set(table.position);

            for (SeatData seat : table.seats) {
                if (!seat.isOccupied) {
                    seat.isOccupied      = true;
                    customer.claimedSeat = seat;
                    customer.tableId     = table.tableId;

                    Vector2 seatPos = new Vector2(seat.position).add(0f, SEAT_Y_OFFSET);
                    customer.tablePosition.set(seatPos);

                    Transform transform = Transform.MAPPER.get(entity);
                    transform.getPosition().set(seatPos);

                    Physic physic = Physic.MAPPER.get(entity);
                    if (physic != null && physic.getBody() != null) {
                        physic.getBody().setTransform(seatPos, 0f);
                        physic.getPrevPosition().set(seatPos);
                    }

                    Animation2D anim = Animation2D.MAPPER.get(entity);
                    if (anim != null) anim.paused = true;

                    break;
                }
            }
            customer.orderItemId = POSSIBLE_ORDERS[MathUtils.random(0, POSSIBLE_ORDERS.length - 1)];
            customer.maxPatience = MathUtils.random(20f, 40f);
        }

        if (customer.stateTimer >= ORDER_DELAY) {
            customer.state      = Customer.CustomerState.ORDERING;
            customer.stateTimer = 0f;
            Gdx.app.log("CUSTOMER", "ready to order: " + customer.orderItemId);
        }
    }

    private void removeCustomer(Entity entity, Customer customer) {
        if (customer.tableId != -1) tableManager.vacateTable(customer.tableId);
        if (customer.claimedSeat != null) customer.claimedSeat.isOccupied = false;

        Physic physic = Physic.MAPPER.get(entity);
        if (physic != null && physic.getBody() != null) world.destroyBody(physic.getBody());

        engine.removeEntity(entity);
    }
}
