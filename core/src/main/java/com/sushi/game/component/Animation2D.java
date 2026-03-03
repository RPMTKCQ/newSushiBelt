package com.sushi.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.sushi.game.asset.AtlasAsset;

import static com.badlogic.gdx.graphics.g2d.Animation.*;
import static com.sushi.game.component.Facing.*;

public class Animation2D implements Component {
    public static final ComponentMapper<Animation2D> MAPPER = ComponentMapper.getFor(Animation2D.class);

    private final AtlasAsset atlasAsset;
    private final String atlasKey;
    private AnimationType type;
    private FacingDirection direction;
    private PlayMode playmode;
    private float speed;
    private float stateTime;
    private Animation<TextureRegion> animation;
    private boolean dirty;
    private float FRAME_DURATION = 1 / 5f;

    public Animation2D(AtlasAsset atlasAsset,
                       String atlasKey,
                       AnimationType type,
                       PlayMode playmode,
                       float speed
    ) {
        this.atlasAsset = atlasAsset;
        this.atlasKey = atlasKey;
        this.type = type;
        this.direction = null;
        this.playmode = playmode;
        this.speed = speed;
        this.stateTime = 0f;
        this.animation = null;
    }

    public float getFRAME_DURATION() {
        return switch(this.type) {
            case WALK -> 0.1f;
            case IDLE -> 0.2f;
            default -> 0.15f;
        };
    }

    public void setFRAME_DURATION(float FRAME_DURATION) {
        this.FRAME_DURATION = FRAME_DURATION;
    }

    public void setAnimation(Animation<TextureRegion> animation, FacingDirection direction) {
        this.animation = animation;
        this.direction = direction;
        this.stateTime = 0f;
        this.dirty = false;
    }

    public FacingDirection getDirection() {
        return direction;
    }

    public Animation<TextureRegion> getAnimation() {
        return animation;
    }

    public AtlasAsset getAtlasAsset() {
        return atlasAsset;
    }

    public String getAtlasKey() {
        return atlasKey;
    }

    public void setType(AnimationType type) {
        this.type = type;
        this.dirty = true;
    }

    public AnimationType getType() {
        return type;
    }

    public PlayMode getPlaymode() {
        return playmode;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setPlaymode(PlayMode playmode) {
        this.playmode = playmode;
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isFinished() {
        return animation.isAnimationFinished(stateTime);
    }

    public float incAndGetStateTime(float deltaTime) {
        this.stateTime += deltaTime * speed;
        return this.stateTime;
    }

    public enum AnimationType {
        IDLE, WALK;

        private final String atlasKey;

        AnimationType() {
            this.atlasKey = name().toLowerCase();
        }

        public String getAtlasKey() {
            return atlasKey;
        }
    }

}
