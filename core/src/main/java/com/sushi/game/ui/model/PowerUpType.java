package com.sushi.game.ui.model;

public enum PowerUpType {
    MOVEMENT_SPEED {
        @Override public String displayName()  { return "Movement Speed"; }
        @Override public String description()  { return "Increase movement speed by 10%"; }
        @Override public String iconName()     { return "icon-speed"; }
        @Override public float  multiplier()   { return 1.10f; }
    },
    COOKING_SPEED {
        @Override public String displayName()  { return "Cooking Speed"; }
        @Override public String description()  { return "Increase cooking speed by 20%"; }
        @Override public String iconName()     { return "icon-cook"; }
        @Override public float  multiplier()   { return 1.20f; }
    },
    RUSH_HOUR {
        @Override public String displayName()  { return "Rush Hour"; }
        @Override public String description()  { return "Next 3 serves give 30% bonus points"; }
        @Override public String iconName()     { return "icon-rush"; }
        @Override public float  multiplier()   { return 1.30f; }
    },
    DOUBLE_TIPS {
        @Override public String displayName()  { return "Double Tips"; }
        @Override public String description()  { return "Next 3 serves grant 2x Money"; }
        @Override public String iconName()     { return "icon-money"; }
        @Override public float  multiplier()   { return 2.00f; }
    };

    public abstract String displayName();
    public abstract String description();
    public abstract String iconName();
    public abstract float  multiplier();
}
