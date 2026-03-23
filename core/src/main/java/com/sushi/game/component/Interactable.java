package com.sushi.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class Interactable implements Component {
    public static final ComponentMapper<Interactable> MAPPER = ComponentMapper.getFor(Interactable.class);

    public String tag = "";
}
