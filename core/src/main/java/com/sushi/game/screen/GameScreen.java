package com.sushi.game.screen;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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
import com.sushi.game.asset.AtlasAsset;
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

    //  core
    private final SushiGame game;
    private final Engine engine;
    private final World physicWorld;
    private final AudioService audioService;

    //  tiled
    private final TiledService tiledService;
    private final TiledAshleyConfigurator tiledAshleyConfigurator;

    //  input
    private final KeyboardController keyboardController;

    //  factory / spawning
    private final TableManager tableManager;
    private final CustomerFactory customerFactory;
    private final CustomerSpawner customerSpawner;

    //  ui
    private final Skin skin;
    private final Viewport uiViewport;
    private final Stage stage;
    private final GameScreenUI gameScreenUI;

    //  systems
    private final PowerUpSystem powerUpSystem;
    private final LevelSystem levelSystem;
    private final StrikeSystem strikeSystem;
    private final ChefSystem chefSystem;
    private final ConveyorSystem conveyorSystem;
    private final ChefFactory chefFactory;
    private final ChefSpawner chefSpawner;

    public GameScreen(SushiGame game) {
        this.game = game;

        //  core
        this.physicWorld = new World(Vector2.Zero, true);
        this.physicWorld.setAutoClearForces(false);
        this.audioService = game.getAudioService();
        this.engine = new Engine();

        //  tiled
        this.tiledService = new TiledService(game.getAssetService(), this.physicWorld);
        this.tiledAshleyConfigurator = new TiledAshleyConfigurator(this.engine, game.getAssetService(), physicWorld);

        //  input
        this.keyboardController = new KeyboardController(GameControllerState.class, engine);

        //  factory / spawning
        this.tableManager = new TableManager();
        this.customerFactory = new CustomerFactory(engine, physicWorld, game.getAssetService());
        this.customerSpawner = new CustomerSpawner(customerFactory, tableManager, engine);
        this.chefFactory = new ChefFactory(engine, game.getAssetService());
        this.chefSpawner = new ChefSpawner(chefFactory);

        //  ui
        this.skin = game.getAssetService().get(SkinAsset.DEFAULT);
        this.uiViewport = new FitViewport(1920f, 1080f);
        this.stage = new Stage(uiViewport, game.getBatch());
        this.gameScreenUI = new GameScreenUI(stage,
            game.getAssetService().get(SkinAsset.GAME),
            game.getAssetService());

        //  systems
        this.powerUpSystem = new PowerUpSystem(engine);
        this.levelSystem = new LevelSystem(gameScreenUI, powerUpSystem);
        this.strikeSystem = new StrikeSystem(gameScreenUI, () ->
            game.setScreen(new GameOverScreen(game, levelSystem.getScore())));
        this.chefSystem = new ChefSystem(gameScreenUI, engine);
        this.conveyorSystem = new ConveyorSystem();

        engine.addSystem(new CustomerSystem(physicWorld, tableManager, engine,
            gameScreenUI, levelSystem, strikeSystem));
        engine.addSystem(powerUpSystem);
        engine.addSystem(levelSystem);
        engine.addSystem(strikeSystem);
        engine.addSystem(chefSystem);
        engine.addSystem(conveyorSystem);
        engine.addSystem(new CustomerRenderSystem(game.getCamera()));
        engine.addSystem(new ControllerSystem(game.getAudioService(), this.physicWorld, gameScreenUI));
        engine.addSystem(new FsmSystem());
        engine.addSystem(new FacingSystem());
        engine.addSystem(new PhysicMoveSystem());
        engine.addSystem(new PhysicSystem(this.physicWorld, 1 / 60f));
        engine.addSystem(new AnimationSystem(game.getAssetService()));
        engine.addSystem(new CameraSystem(game.getCamera()));
        engine.addSystem(new RenderSystem(game.getBatch(), game.getViewport(), game.getCamera()));
        engine.addSystem(new PhysicDebugRenderSystem(physicWorld, game.getCamera()));

        game.getCamera().zoom = 1f;
    }

    @Override
    public void show() {
        game.setInputProcessors(keyboardController, stage);
        keyboardController.setActiveState(GameControllerState.class);

        Consumer<TiledMap> renderConsumer = engine.getSystem(RenderSystem.class)::setMap;
        Consumer<TiledMap> cameraConsumer = engine.getSystem(CameraSystem.class)::setMap;
        Consumer<TiledMap> audioConsumer = audioService::setMap;

        tiledService.setMapChangeConsumer(renderConsumer.andThen(cameraConsumer).andThen(audioConsumer));
        tiledService.setLoadObjectConsumer(tiledAshleyConfigurator::onLoadObject);
        tiledService.setLoadTileConsumer(tiledAshleyConfigurator::onLoadTile);

        TiledMap tiledMap = tiledService.loadMap(MapAsset.MAIN);
        tiledService.setMap(tiledMap);

        MapLayer objectLayer = tiledMap.getLayers().get("objects");
        if (objectLayer != null) {
            tableManager.loadTables(objectLayer.getObjects());
            customerSpawner.loadSpawnPoints(objectLayer.getObjects());
            chefSpawner.loadSpawnPoints(objectLayer.getObjects());
        }
        chefSpawner.spawnAll();

        for (TextureAtlas.AtlasRegion r : game.getAssetService().get(AtlasAsset.OBJECTS).getRegions()) {
            Gdx.app.log("ATLAS", r.name);
        }
        Gdx.app.log("TABLES", "loaded tables: " + tableManager.getTableCount());
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1 / 30f);

        //  game logic
        customerSpawner.update(delta);
        engine.update(delta);

        //  ui
        uiViewport.apply();
        stage.getBatch().setColor(Color.WHITE);
        stage.act(delta);
        stage.draw();

        // tick receipt patience bars
        gameScreenUI.updateReceipts(delta);
    }

    @Override
    public void resize(int width, int height) {
        uiViewport.update(width, height, true);
    }

    @Override
    public void hide() {
        engine.removeAllEntities();
        stage.clear();
    }

    @Override
    public void dispose() {
        for (EntitySystem system : engine.getSystems()) {
            if (system instanceof Disposable disposable) {
                disposable.dispose();
            }
        }
        physicWorld.dispose();
        stage.dispose();
    }
}
