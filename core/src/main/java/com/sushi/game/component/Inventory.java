package com.sushi.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import java.util.ArrayList;
import java.util.List;

public class Inventory implements Component {
    public static final ComponentMapper<Inventory> MAPPER = ComponentMapper.getFor(Inventory.class);

    public static final int MAX_SLOTS = 3;
    public final List<String> dishes = new ArrayList<>();  // dishIds, max 3

    public boolean isFull() { return dishes.size() >= MAX_SLOTS; }
    public boolean isEmpty() { return dishes.isEmpty(); }

    public boolean addDish(String dishId) {
        if (isFull()) return false;
        dishes.add(dishId);
        return true;
    }

    public String removeDish(String dishId) {
        dishes.remove(dishId);
        return dishId;
    }

    public boolean hasDish(String dishId) {
        return dishes.contains(dishId);
    }
}
