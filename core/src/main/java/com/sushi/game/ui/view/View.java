package com.sushi.game.ui.view;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.sushi.game.ui.model.ViewModel;


public abstract class View<T extends ViewModel> extends Table implements EventListener {

    protected final Stage stage;
    protected final Skin skin;
    protected final T viewModel;

    public View(Stage stage, Skin skin, T viewModel) {
        super(skin);
        this.stage = stage;
        this.skin = skin;
        this.viewModel = viewModel;
        setupUI();
    }


    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        if (stage == null) {
            viewModel.clearPropertyChanges();
        } else {
            setupPropertyChanges();
        }
    }

    protected abstract void setupUI();

    protected void setupPropertyChanges() {
    }

    public static void onClick(Actor actor, OnEventConsumer consumer) {
        actor.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                consumer.onEvent();
            }
        });
    }

    public static <T extends Actor> void onEnter(T actor, OnActorEvent<T> consumer) {
        actor.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                consumer.onEvent(actor);
            }
        });
    }

    public static <T extends Actor> void onChange(T actor, OnActorEvent<T> consumer) {
        actor.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor eventActor) {
                consumer.onEvent(actor);
            }
        });
    }


    @FunctionalInterface
    public interface OnEventConsumer {
        void onEvent();
    }

    @FunctionalInterface
    public interface OnActorEvent<T extends Actor> {
        void onEvent(T actor);
    }

}
