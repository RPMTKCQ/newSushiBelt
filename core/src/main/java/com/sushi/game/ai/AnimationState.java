package com.sushi.game.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.sushi.game.component.Animation2D;
import com.sushi.game.component.Fsm;
import com.sushi.game.component.Move;

import static com.sushi.game.component.Animation2D.*;

public enum AnimationState implements State<Entity> {
    IDLE {
        @Override
        public void enter(Entity entity) {
            MAPPER.get(entity).setType(AnimationType.IDLE);
        }

        @Override
        public void update(Entity entity) {
            Move move = Move.MAPPER.get(entity);
            if (move != null && !move.isRooted() && !move.getDirection().isZero()) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(WALK);
                return;
            }

        }

        @Override
        public void exit(Entity entity) {

        }

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) {
            return false;
        }
    },

    WALK {
        @Override
        public void enter(Entity entity) {
            MAPPER.get(entity).setType(AnimationType.WALK);

        }

        @Override
        public void update(Entity entity) {
            Move move = Move.MAPPER.get(entity);
            if (move == null || move.getDirection().isZero() || move.isRooted()) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(IDLE);
            }
        }

        @Override
        public void exit(Entity entity) {

        }

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) {
            return false;
        }
    }
}
