package com.sushi.game.asset;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;

public enum SoundAsset implements Asset<Sound> {
    // UI Sounds
    MENU_HOVER("menu_hover2.mp3"),
    MENU_SELECT("menu_hover.mp3"),
    MENU_BACK("menu_back.mp3"),

    // Gameplay Sounds
    COOKING_DONE("cooking_done2.mp3"),
    CUSTOMER_ANGRY("customer_angry.mp3"),
    CUSTOMER_ORDER("customer_order.mp3"),
    CUSTOMER_SIT("customer_sit2.mp3"),
    RECEIPT_SUBMIT("receipt_submit2.mp3"),
    SERVE_FOOD("serve_food.mp3"),
    TAKE_MONEY("take_money.mp3"),
    LEVEL_UP("level_up.mp3");



    private final AssetDescriptor<Sound> descriptor;

    SoundAsset(String musicFile){
        this.descriptor = new AssetDescriptor<>("audio/sound/"+musicFile, Sound.class);
    }

    @Override
    public AssetDescriptor<Sound> getDescriptor() {
        return descriptor;
    }
}
