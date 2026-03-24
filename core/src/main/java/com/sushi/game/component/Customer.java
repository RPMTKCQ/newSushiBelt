package com.sushi.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Vector2;
import com.sushi.game.model.SeatData;

public class Customer implements Component {
    public static final ComponentMapper<Customer> MAPPER = ComponentMapper.getFor(Customer.class);

    private static int ID_GENERATOR = 0;
    public final int id = ID_GENERATOR++; // CRITICAL FIX: Truly unique ID per customer

    public enum CustomerState { WAITING, SEATING, ORDERING, WAITING_FOR_FOOD, EATING, PAYING, LEAVING }

    public CustomerState state = CustomerState.WAITING;
    public int spriteIndex = 0;
    public int tableId = -1;
    public Vector2 tablePosition = new Vector2();
    public String orderItemId;
    public float stateTimer = 0f;
    public boolean clickable = true;
    public SeatData claimedSeat = null;
    public float maxPatience = 60f;
    public boolean leftAngry = false;

    // Snapshot variables for scoring when payment is collected
    public boolean servedSorted = false;
    public boolean servedLate = false;

    public void reset() {
        state = CustomerState.WAITING;
        tableId = -1;
        orderItemId = null;
        stateTimer = 0f;
        clickable = true;
        servedSorted = false;
        servedLate = false;
    }
}
