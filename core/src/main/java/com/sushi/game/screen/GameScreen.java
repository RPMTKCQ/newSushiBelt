package com.sushi.game.screen;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sushi.game.SushiGame;
import com.sushi.game.asset.MapAsset;
import com.sushi.game.asset.SkinAsset;
import com.sushi.game.audio.AudioService;
import com.sushi.game.factory.ChefFactory;
import com.sushi.game.factory.CustomerFactory;
import com.sushi.game.factory.TableManager;
import com.sushi.game.input.GameControllerState;
import com.sushi.game.input.KeyboardController;
import com.sushi.game.system.*;
import com.sushi.game.tiled.TiledAshleyConfigurator;
import com.sushi.game.tiled.TiledService;
import com.sushi.game.ui.GameScreenUI;

import java.util.function.Consumer;

public class GameScreen extends ScreenAdapter {

    private final SushiGame game;
    private final Engine engine;
    private final World physicWorld;
    private final AudioService audioService;

    private final TiledService tiledService;
    private final TiledAshleyConfigurator tiledAshleyConfigurator;

    private final KeyboardController keyboardController;

    private final TableManager tableManager;
    private final CustomerFactory customerFactory;
    private final CustomerSpawner customerSpawner;
    private final ChefFactory chefFactory;
    private final ChefSpawner chefSpawner;

    private final Skin skin;
    private final Viewport uiViewport;
    private final Stage stage;
    private final GameScreenUI gameScreenUI;

    private final PowerUpSystem powerUpSystem;
    private LevelSystem levelSystem = null;
    private final ChefSystem chefSystem;
    private final ConveyorSystem conveyorSystem;
    private final CustomerRenderSystem customerRenderSystem;

    private boolean isPaused = false;

    private final MapAsset currentStage;
    private final boolean isEndlessMode;

    public GameScreen(SushiGame game, MapAsset currentStage, boolean isEndlessMode) {
        this.game = game;
        this.currentStage = currentStage;
        this.isEndlessMode = isEndlessMode;

        this.physicWorld = new World(Vector2.Zero, true);
        this.physicWorld.setAutoClearForces(false);
        this.audioService = game.getAudioService();
        this.engine = new Engine();

        this.tiledService = new TiledService(game.getAssetService(), this.physicWorld);
        this.tiledAshleyConfigurator = new TiledAshleyConfigurator(this.engine, game.getAssetService(), physicWorld);

        this.keyboardController = new KeyboardController(GameControllerState.class, engine);

        this.tableManager    = new TableManager();
        this.customerFactory = new CustomerFactory(engine, physicWorld, game.getAssetService());
        this.chefFactory     = new ChefFactory(engine, game.getAssetService());
        this.chefSpawner     = new ChefSpawner(chefFactory);

        this.skin         = game.getAssetService().get(SkinAsset.DEFAULT);
        this.uiViewport   = new FitViewport(1920f, 1080f);
        this.stage        = new Stage(uiViewport, game.getBatch());

        Skin gameUISkin = game.getAssetService().get(SkinAsset.GAME);
        // FIX: Passed audioService globally into the UI!
        this.gameScreenUI = new GameScreenUI(stage, gameUISkin, game.getAssetService(), this.audioService);

        this.powerUpSystem = new PowerUpSystem(engine);

        this.levelSystem = new LevelSystem(gameScreenUI, powerUpSystem, isEndlessMode,
            () -> game.setScreen(new WinScreen(game, levelSystem.getScore(), levelSystem.getMoney())),
            () -> game.setScreen(new GameOverScreen(game, levelSystem.getScore(), this.currentStage, isEndlessMode)));

        this.customerSpawner = new CustomerSpawner(customerFactory, tableManager, engine, levelSystem);

        this.conveyorSystem = new ConveyorSystem();
        this.chefSystem     = new ChefSystem(gameScreenUI, engine, physicWorld, game.getAssetService(), conveyorSystem);
        this.customerRenderSystem = new CustomerRenderSystem(game.getCamera(), gameUISkin, game.getAssetService(), game.getBatch(), engine);

        engine.addSystem(new CustomerSystem(physicWorld, tableManager, engine, gameScreenUI, levelSystem));
        engine.addSystem(powerUpSystem);
        engine.addSystem(levelSystem);
        engine.addSystem(chefSystem);
        engine.addSystem(conveyorSystem);

        engine.addSystem(new ControllerSystem(game.getAudioService(), this.physicWorld, gameScreenUI, levelSystem));

        engine.addSystem(new FsmSystem());
        engine.addSystem(new FacingSystem());
        engine.addSystem(new PhysicMoveSystem());
        engine.addSystem(new PhysicSystem(this.physicWorld, 1 / 60f));
        engine.addSystem(new AnimationSystem(game.getAssetService()));
        engine.addSystem(new CameraSystem(game.getCamera()));
        engine.addSystem(new RenderSystem(game.getBatch(), game.getViewport(), game.getCamera()));

        game.getCamera().zoom = 1f;
    }

    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(keyboardController);
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);

        keyboardController.setActiveState(GameControllerState.class);

        Consumer<TiledMap> renderConsumer = engine.getSystem(RenderSystem.class)::setMap;
        Consumer<TiledMap> cameraConsumer = engine.getSystem(CameraSystem.class)::setMap;
        Consumer<TiledMap> audioConsumer  = audioService::setMap;

        tiledService.setMapChangeConsumer(renderConsumer.andThen(cameraConsumer).andThen(audioConsumer));
        tiledService.setLoadObjectConsumer(tiledAshleyConfigurator::onLoadObject);
        tiledService.setLoadTileConsumer(tiledAshleyConfigurator::onLoadTile);

        TiledMap tiledMap = tiledService.loadMap(this.currentStage);
        tiledService.setMap(tiledMap);

        MapLayer objectLayer      = tiledMap.getLayers().get("objects");
        MapLayer smallObjectLayer = tiledMap.getLayers().get("small-objects");

        if (objectLayer != null) {
            tableManager.loadTables(objectLayer.getObjects());
            customerSpawner.loadSpawnPoints(objectLayer.getObjects());
            chefSpawner.loadSpawnPoints(objectLayer.getObjects());
        }
        if (objectLayer != null)      conveyorSystem.loadBelt(objectLayer.getObjects());
        if (smallObjectLayer != null) conveyorSystem.loadBelt(smallObjectLayer.getObjects());
        chefSpawner.spawnAll();
    }

    public void resumeGame() {
        isPaused = false;
        for (EntitySystem system : engine.getSystems()) {
            if (system instanceof RenderSystem || system instanceof CameraSystem || system instanceof PhysicDebugRenderSystem) {
                continue;
            }
            system.setProcessing(true);
        }

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(keyboardController);
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);

        // CLEANED UP
        gameScreenUI.togglePauseOverlay(false, null, null);
    }

    public void pauseGame() {
        isPaused = true;
        for (EntitySystem system : engine.getSystems()) {
            if (system instanceof RenderSystem || system instanceof CameraSystem || system instanceof PhysicDebugRenderSystem) {
                continue;
            }
            system.setProcessing(false);
        }

        keyboardController.reset();
        Gdx.input.setInputProcessor(stage);

        // CLEANED UP
        gameScreenUI.togglePauseOverlay(true, this::resumeGame, () -> {
            game.setScreen(new MenuScreen(game));
        });
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !isPaused) {
            pauseGame();
        }

        delta = Math.min(delta, 1 / 30f);

        if (!isPaused) {
            customerSpawner.update(delta);
        }

        engine.update(delta);

        if (!isPaused) {
            customerRenderSystem.update(delta);
            gameScreenUI.updateReceipts(delta);
        } else {
            customerRenderSystem.update(0f);
            gameScreenUI.updatePauseMenu(delta);
        }

        uiViewport.apply();
        stage.getBatch().setColor(Color.WHITE);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) { uiViewport.update(width, height, true); }

    @Override
    public void hide() {
        engine.removeAllEntities();
        stage.clear();
    }

    @Override
    public void dispose() {
        for (EntitySystem system : engine.getSystems()) {
            if (system instanceof Disposable disposable) disposable.dispose();
        }
        customerRenderSystem.dispose();
        physicWorld.dispose();
        stage.dispose();
    }
}
