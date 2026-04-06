package com.sushi.game.ui.model;

public enum PowerUpType {
    MOVEMENT_SPEED {
        @Override public String displayName()  { return "Running Shoes"; }
        @Override public String description()  { return "Increase movement speed by 8%."; }
        @Override public String iconName()     { return "playerIcon"; }
        @Override public float  multiplier()   { return 1.08f; }
        @Override public String category()     { return "[Player]"; }
        @Override public String corruption()   { return ""; }
    },
    COOKING_SPEED {
        @Override public String displayName()  { return "Sharpened Knives"; }
        @Override public String description()  { return "Increase cooking speed by 20%."; }
        @Override public String iconName()     { return "kitchenIcon"; }
        @Override public float  multiplier()   { return 1.20f; }
        @Override public String category()     { return "[Kitchen]"; }
        @Override public String corruption()   { return ""; }
    },
    SUGAR_RUSH {
        @Override public String displayName()  { return "Sugar Rush"; }
        @Override public String description()  { return "+20% Movement Speed."; }
        @Override public String iconName()     { return "playerIcon"; }
        @Override public float  multiplier()   { return 1.20f; }
        @Override public String category()     { return "[Player]"; }
        @Override public String corruption()   { return "Take 20 damage to Reputation immediately upon choosing."; }
    },
    HEAVY_LIFTER {
        @Override public String displayName()  { return "Heavy Lifter"; }
        @Override public String description()  { return "Allows you to carry more items."; }
        @Override public String iconName()     { return "playerIcon"; }
        @Override public float  multiplier()   { return 0.85f; } // Slows you down!
        @Override public String category()     { return "[Player]"; }
        @Override public String corruption()   { return "Permanent -15% Movement Speed."; }
    },
    DOUBLE_TIPS {
        @Override public String displayName()  { return "Double Tips"; }
        @Override public String description()  { return "Next 3 serves grant 2x Money."; }
        @Override public String iconName()     { return "serviceIcon"; }
        @Override public float  multiplier()   { return 2.00f; }
        @Override public String category()     { return "[Service]"; }
        @Override public String corruption()   { return ""; }
    },
    PRISTINE_KITCHEN {
        @Override public String displayName()  { return "Pristine Kitchen"; }
        @Override public String description()  { return "All money gained is doubled while Reputation is at 100/100."; }
        @Override public String iconName()     { return "kitchenIcon"; }
        @Override public float  multiplier()   { return 1.0f; }
        @Override public String category()     { return "[Kitchen]"; }
        @Override public String corruption()   { return "If Reputation takes a hit, the buff goes on cooldown for 30s."; }
    },
    GREEN_TEA {
        @Override public String displayName()  { return "Green Tea"; }
        @Override public String description()  { return "25% chance to restore 2 Reputation HP on every successful serve."; }
        @Override public String iconName()     { return "serviceIcon"; }
        @Override public float  multiplier()   { return 1.0f; }
        @Override public String category()     { return "[Service]"; }
        @Override public String corruption()   { return "Base money from serves is reduced to $0."; }
    },
    GREEDY_ALGORITHM {
        @Override public String displayName()  { return "Greedy Algorithm"; }
        @Override public String description()  { return "All money payouts are permanently increased by 50%."; }
        @Override public String iconName()     { return "algorithmIcon"; }
        @Override public float  multiplier()   { return 1.50f; }
        @Override public String category()     { return "[Anomaly]"; }
        @Override public String corruption()   { return "Angry customers deal 2x Reputation damage."; }
    };

    public abstract String displayName();
    public abstract String description();
    public abstract String iconName();
    public abstract float  multiplier();
    public abstract String category();
    public abstract String corruption();
}
