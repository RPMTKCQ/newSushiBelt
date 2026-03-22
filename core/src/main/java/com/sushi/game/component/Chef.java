package com.sushi.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class Chef implements Component {
    public static final ComponentMapper<Chef> MAPPER = ComponentMapper.getFor(Chef.class);

    public enum ChefState { IDLE, COOKING, DONE }

    public ChefState state = ChefState.IDLE;
    public int stationId = -1;       // which cooking station this chef is at
    public String currentRecipeId;   // the recipe being cooked atm
    public float cookTimer = 0f;     // counts up while cooking
    public float cookDuration = 0f;  // cooking time for the recipes
}
