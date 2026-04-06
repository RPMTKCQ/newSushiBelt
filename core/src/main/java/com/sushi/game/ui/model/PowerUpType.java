package com.sushi.game.ui.model;

public enum PowerUpType {
    BASIC_MOVE_SPEED {
        @Override public String displayName()  { return "Running Shoes"; }
        @Override public String description()  { return "Increase movement speed by 15%."; }
        @Override public String iconName()     { return "playerIcon"; }
        @Override public String category()     { return "[Player]"; }
        @Override public String corruption()   { return ""; }
    },
    BASIC_COOK_SPEED {
        @Override public String displayName()  { return "Sharpened Knives"; }
        @Override public String description()  { return "Increase cooking speed by 15%."; }
        @Override public String iconName()     { return "kitchenIcon"; }
        @Override public String category()     { return "[Kitchen]"; }
        @Override public String corruption()   { return ""; }
    },
    SUGAR_RUSH {
        @Override public String displayName()  { return "Sugar Rush"; }
        @Override public String description()  { return "Increase movement speed by 40%."; }
        @Override public String iconName()     { return "playerIcon"; }
        @Override public String category()     { return "[Player]"; }
        @Override public String corruption()   { return "Customer patience drains 20% faster."; }
    },
    HEAVY_LIFTER {
        @Override public String displayName()  { return "Heavy Lifter"; }
        @Override public String description()  { return "Carry up to 5 items at once."; }
        @Override public String iconName()     { return "playerIcon"; }
        @Override public String category()     { return "[Player]"; }
        @Override public String corruption()   { return "Decreases movement speed by 15%."; }
    },
    DOUBLE_TIPS {
        @Override public String displayName()  { return "Double Tips"; }
        @Override public String description()  { return "Next 3 serves grant 2x Money."; }
        @Override public String iconName()     { return "serviceIcon"; }
        @Override public String category()     { return "[Service]"; }
        @Override public String corruption()   { return ""; }
    },
    GREEN_TEA {
        @Override public String displayName()  { return "Green Tea"; }
        @Override public String description()  { return "Serving a customer with >20% patience has a 50% chance to restore 5 HP."; }
        @Override public String iconName()     { return "serviceIcon"; }
        @Override public String category()     { return "[Service]"; }
        @Override public String corruption()   { return "Customers no longer leave tips ($0)."; }
    },
    PRISTINE_KITCHEN {
        @Override public String displayName()  { return "Pristine Kitchen"; }
        @Override public String description()  { return "Earn 2x tips while at exactly 100/100 HP."; }
        @Override public String iconName()     { return "kitchenIcon"; }
        @Override public String category()     { return "[Kitchen]"; }
        @Override public String corruption()   { return "Taking damage disables this buff for 30 seconds."; }
    },
    GREEDY_ALGORITHM {
        @Override public String displayName()  { return "Greedy Algorithm"; }
        @Override public String description()  { return "Tips multiply up to 3x based on how LOW the customer's patience is."; }
        @Override public String iconName()     { return "algorithmIcon"; }
        @Override public String category()     { return "[Algorithm]"; }
        @Override public String corruption()   { return "Customer patience drains 15% faster."; }
    };

    public abstract String displayName();
    public abstract String description();
    public abstract String iconName();
    public abstract String category();
    public abstract String corruption();
}
