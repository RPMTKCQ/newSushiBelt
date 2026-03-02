package com.sushi.game.screen;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.sushi.game.SushiGame;
import com.sushi.game.asset.MapAsset;
import com.sushi.game.input.GameControllerState;
import com.sushi.game.input.KeyboardController;
import com.sushi.game.system.*;
import com.sushi.game.system.AnimationSystem;
import com.sushi.game.system.ControllerSystem;
import com.sushi.game.system.RenderSystem;
import com.sushi.game.tiled.TiledAshleyConfigurator;
import com.sushi.game.tiled.TiledService;

import java.util.function.Consumer;


public class GameScreen extends ScreenAdapter {
    private final Engine engine;
    private final TiledService tiledService;
    private final TiledAshleyConfigurator tiledAshleyConfigurator;
    private final KeyboardController keyboardController;
    private final SushiGame game;
    private final World physicWorld;


    public GameScreen(SushiGame game) {
        this.game = game;
        this.tiledService = new TiledService(game.getAssetService());
        this.engine = new Engine();
        this.physicWorld = new World(Vector2.Zero, true);
        this.physicWorld.setAutoClearForces(false);
        this.tiledAshleyConfigurator = new TiledAshleyConfigurator(this.engine, game.getAssetService(), physicWorld);
        this.keyboardController = new KeyboardController(GameControllerState.class, engine);


        this.engine.addSystem(new ControllerSystem());
        this.engine.addSystem(new PhysicMoveSystem());
        this.engine.addSystem(new PhysicSystem(this.physicWorld, 1 / 60f));
        this.engine.addSystem(new AnimationSystem(game.getAssetService()));
        this.engine.addSystem(new RenderSystem(game.getBatch(), game.getViewport(), game.getCamera()));

    }

    @Override
    public void show() {
        game.setInputProcessors(keyboardController);
        keyboardController.setActiveState(GameControllerState.class);

        Consumer<TiledMap> renderConsumer = this.engine.getSystem(RenderSystem.class)::setMap;
        this.tiledService.setMapChangeConsumer(renderConsumer);
        this.tiledService.setLoadObjectConsumer(this.tiledAshleyConfigurator::onLoadObject);
        this.tiledService.setLoadTileConsumer(tiledAshleyConfigurator::onLoadTile);


        TiledMap tiledMap = this.tiledService.loadMap(MapAsset.MAIN);
        this.tiledService.setMap(tiledMap);

    }

    @Override
    public void hide() {
        this.engine.removeAllEntities();
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1 / 30f);
        this.engine.update(delta);
    }

    @Override
    public void dispose() {
        for (EntitySystem system : this.engine.getSystems()) {
            if (system instanceof Disposable disposableSystem) {
                disposableSystem.dispose();
            }
        }
        this.physicWorld.dispose();

    }
}
