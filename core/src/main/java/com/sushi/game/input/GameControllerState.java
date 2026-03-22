package com.sushi.game.input;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.sushi.game.component.Controller;

import java.util.List;


public class GameControllerState implements ControllerState {

    private final ImmutableArray<Entity> controllerEntities;

    public GameControllerState(Engine engine) {
        this.controllerEntities = engine.getEntitiesFor(Family.all(Controller.class).get());
    }

    @Override
    public void keyDown(Command command) {
        for (Entity entity : controllerEntities) {
            List<Command> pressed = Controller.MAPPER.get(entity).getPressedCommands();
            if (!pressed.contains(command)) { // only add if not already there
                pressed.add(command);
            }
        }
    }

    @Override
    public void keyUp(Command command) {
        for (Entity entity : controllerEntities){
            Controller.MAPPER.get(entity).getReleasedCommands().add(command);
        }
    }
}
