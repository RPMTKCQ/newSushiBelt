package com.sushi.game.input;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.HashMap;
import java.util.Map;

public class KeyboardController extends InputAdapter {
    private static final Map<Integer, Command> KEY_MAPPING = Map.ofEntries(
        Map.entry(Input.Keys.W,      Command.UP),
        Map.entry(Input.Keys.S,      Command.DOWN),
        Map.entry(Input.Keys.A,      Command.LEFT),
        Map.entry(Input.Keys.D,      Command.RIGHT),
        Map.entry(Input.Keys.SPACE,  Command.SELECT),
        Map.entry(Input.Keys.ESCAPE, Command.CANCEL),
        Map.entry(Input.Keys.F,      Command.SUBMIT)
    );

    private final boolean[] commandState;
    private final Map<Class<? extends ControllerState>, ControllerState> stateCache;
    private ControllerState activeState;

    public KeyboardController(Class<? extends ControllerState> initialState, Engine engine) {
        this.stateCache   = new HashMap<>();
        this.activeState  = null;
        this.commandState = new boolean[Command.values().length];

        this.stateCache.put(IdleControllerState.class, new IdleControllerState());
        this.stateCache.put(GameControllerState.class, new GameControllerState(engine));
        setActiveState(initialState);
    }

    public void setActiveState(Class<? extends ControllerState> stateClass) {
        ControllerState controllerState = stateCache.get(stateClass);
        if (controllerState == null) {
            throw new GdxRuntimeException("No state with class " + stateClass + " found in the state cache");
        }
        reset();
        this.activeState = controllerState;
    }

    public void reset() {
        for (Command command : Command.values()) {
            if (this.commandState[command.ordinal()]) {
                this.commandState[command.ordinal()] = false;
                if (this.activeState != null) {
                    this.activeState.keyUp(command);
                }
            }
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        Command command = KEY_MAPPING.get(keycode);
        if (command == null) return false;

        // FIX: Safely consumes OS repeat keys without passing them to the Stage!
        if (this.commandState[command.ordinal()]) return true;

        this.commandState[command.ordinal()] = true;
        this.activeState.keyDown(command);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        Command command = KEY_MAPPING.get(keycode);
        if (command == null) return false;

        if (!this.commandState[command.ordinal()]) return false;

        this.commandState[command.ordinal()] = false;
        this.activeState.keyUp(command);
        return true;
    }
}
