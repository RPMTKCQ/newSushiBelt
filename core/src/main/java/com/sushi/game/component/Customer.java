package com.sushi.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Vector2;

public class Customer implements Component {
    public static final ComponentMapper<Customer> MAPPER = ComponentMapper.getFor(Customer.class);

    public enum CustomerState { WAITING, SEATING, ORDERING, EATING, LEAVING }

    public CustomerState state = CustomerState.WAITING;
    public int spriteIndex = 0; // picking a random sprite
    public int tableId= -1; // tiled object id
    public Vector2 tablePosition = new Vector2();
    public String orderItemId;        // e.g. "tuna_roll"
    public float stateTimer = 0f;
    public boolean clickable = true;

    public void reset() {
        state = CustomerState.WAITING;
        tableId = -1;
        orderItemId = null;
        stateTimer = 0f;
        clickable = true;
    }
}
