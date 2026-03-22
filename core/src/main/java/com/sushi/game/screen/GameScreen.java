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
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sushi.game.SushiGame;
import com.sushi.game.asset.AtlasAsset;
import com.sushi.game.asset.MapAsset;
import com.sushi.game.audio.AudioService;
import com.sushi.game.factory.CustomerFactory;
import com.sushi.game.system.CustomerSpawner;
import com.sushi.game.factory.TableManager;
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
    private final AudioService audioService;
    private final Stage stage;
    private final Viewport uiViewport;
    private final TableManager tableManager;
    private final CustomerFactory customerFactory;
    private final CustomerSpawner customerSpawner;

    public GameScreen(SushiGame game) {
        this.game = game;
        this.physicWorld = new World(Vector2.Zero, true);
        this.physicWorld.setAutoClearForces(false);
        this.tiledService = new TiledService(game.getAssetService(), this.physicWorld);
        this.engine = new Engine();
        this.tiledAshleyConfigurator = new TiledAshleyConfigurator(this.engine, game.getAssetService(), physicWorld);
        this.tableManager = new TableManager();
        this.customerFactory = new CustomerFactory(engine, physicWorld, game.getAssetService());
        this.customerSpawner = new CustomerSpawner(customerFactory, tableManager, engine);
        this.engine.addSystem(new CustomerSystem(physicWorld, tableManager, engine));
        this.keyboardController = new KeyboardController(GameControllerState.class, engine);
        this.audioService = game.getAudioService();
        this.uiViewport = new FitViewport(320f, 180f);
        this.stage = new Stage(uiViewport, game.getBatch());

        this.engine.addSystem(new ControllerSystem(game.getAudioService(), this.physicWorld)); // added this.physic world, might break, might change
        this.engine.addSystem(new FsmSystem());
        this.engine.addSystem(new FacingSystem());
        this.engine.addSystem(new PhysicMoveSystem());
        this.engine.addSystem(new PhysicSystem(this.physicWorld, 1 / 60f));
        this.engine.addSystem(new AnimationSystem(game.getAssetService()));
        this.engine.addSystem(new CameraSystem(game.getCamera()));
        this.engine.addSystem(new RenderSystem(game.getBatch(), game.getViewport(), game.getCamera()));
        this.engine.addSystem(new PhysicDebugRenderSystem(physicWorld, game.getCamera()));

        // adjust camera distance
        game.getCamera().zoom = 1f;
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        this.uiViewport.update(width, height, true);
    }

    @Override
    public void show() {
        game.setInputProcessors(keyboardController, stage);
        keyboardController.setActiveState(GameControllerState.class);

        Consumer<TiledMap> renderConsumer = this.engine.getSystem(RenderSystem.class)::setMap;
        Consumer<TiledMap> cameraConsumer = this.engine.getSystem(CameraSystem.class)::setMap;
        Consumer<TiledMap> audioConsumer = audioService::setMap;

        this.tiledService.setMapChangeConsumer(renderConsumer.andThen(cameraConsumer).andThen(audioConsumer));
        this.tiledService.setLoadObjectConsumer(this.tiledAshleyConfigurator::onLoadObject);
        this.tiledService.setLoadTileConsumer(tiledAshleyConfigurator::onLoadTile);


        TiledMap tiledMap = this.tiledService.loadMap(MapAsset.MAIN);
        this.tiledService.setMap(tiledMap);

        // load tables and spawn points from the objects layer
        MapLayer objectLayer = tiledMap.getLayers().get("objects");
        if (objectLayer != null) {
            tableManager.loadTables(objectLayer.getObjects());
            customerSpawner.loadSpawnPoints(objectLayer.getObjects());
//            customerSpawner.addSpawnPoint(304f, 312f);
        }

        //get regions
        for (TextureAtlas.AtlasRegion r : game.getAssetService().get(AtlasAsset.OBJECTS).getRegions()) {
            Gdx.app.log("ATLAS", r.name);
        }

        //temp logs
        Gdx.app.log("TABLES", "loaded tables: " + tableManager.getTableCount());

    }

    @Override
    public void hide() {
        this.engine.removeAllEntities();
        this.stage.clear();
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1 / 30f);
        customerSpawner.update(delta); // new
        this.engine.update(delta);

        uiViewport.apply();
        stage.getBatch().setColor(Color.WHITE);
        stage.act(delta);
        stage.draw();

    }

    @Override
    public void dispose() {
        for (EntitySystem system : this.engine.getSystems()) {
            if (system instanceof Disposable disposableSystem) {
                disposableSystem.dispose();
            }
        }
        this.physicWorld.dispose();
        this.stage.dispose();


//        stage = new Stage(new ScreenViewport());
//        skin = new Skin(Gdx.files.internal("skin.json"));
//        Gdx.input.setInputProcessor(stage);
//
//        Table table = new Table();
//        table.setFillParent(true);
//
//        table.add();
//
//        table.add();
//
//        table.row();
//        table.add();
//
//        table.add();
//        stage.addActor(table);
//
//        table = new Table();
//        table.setFillParent(true);
//
//        Button button = new Button(skin, "flare");
//        table.add(button);
//
//        Label label = new Label("You bum", skin);
//        label.setWrap(true);
//        label.setColor(skin.getColor("BLACK"));
//        table.add(label).growX();
//
//        table.row();
//        table.add();
//
//        table.add();
//        stage.addActor(table);


    }
}
