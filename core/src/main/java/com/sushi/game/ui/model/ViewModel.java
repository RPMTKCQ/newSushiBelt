package com.sushi.game.ui.model;

import com.sushi.game.SushiGame;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class ViewModel {
    protected final SushiGame game;
    protected final PropertyChangeSupport propertyChangeSupport;

    public ViewModel(SushiGame game) {
        this.game = game;
        this.propertyChangeSupport = new PropertyChangeSupport(this);

    }

    public <T> void onPropertychange(String propertyName, Class<T> propType, OnPropertyChange<T> consumer) {
    this.propertyChangeSupport.addPropertyChangeListener(propertyName, evt -> {
        consumer.onChange(propType.cast(evt.getNewValue()));
    });
    }

    public void clearPropertyChanges() {
        for (PropertyChangeListener listener : this.propertyChangeSupport.getPropertyChangeListeners()) {
            this.propertyChangeSupport.removePropertyChangeListener(listener);
        }
    }

    @FunctionalInterface
    public interface OnPropertyChange<T> {
        void onChange(T value);
    }
}
