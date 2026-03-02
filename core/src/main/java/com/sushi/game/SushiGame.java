package com.sushi.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.*;
import com.sushi.game.asset.AssetService;
import com.sushi.game.screen.GameScreen;
import com.sushi.game.screen.LoadingScreen;

import java.util.HashMap;
import java.util.Map;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class SushiGame extends Game {
    public static final float WORLD_WIDTH = 16f;
    public static final float WORLD_HEIGHT = 9f; //640x320
    public static final  float UNIT_SCALE = 1f/ 30f;


    private Batch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private AssetService assetService;
    private GLProfiler glProfiler;
    private FPSLogger fpsLogger;
    private InputMultiplexer inputMultiplexer;

    private final Map<Class<? extends Screen>, Screen> screenCache = new HashMap<>();

    @Override
    public void create() { // runs once, initializes everything
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        this.inputMultiplexer = new InputMultiplexer();
        Gdx.input.setInputProcessor(inputMultiplexer);

        this.batch = new SpriteBatch();
        this.camera = new OrthographicCamera();
        this.viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        this.assetService = new AssetService(new InternalFileHandleResolver());
        this.glProfiler = new GLProfiler(Gdx.graphics);
        this.glProfiler.enable();
        this.fpsLogger = new FPSLogger();


        addScreen(new LoadingScreen(this, assetService));
        setScreen(LoadingScreen.class);
    }

    @Override
    public void resize(int width,int height) { // runs when window changes
        viewport.update(width,height, true);
        super.resize(width, height); // updates the current screen layout too
    }

    public void addScreen(Screen screen) {
        screenCache.put(screen.getClass(), screen);
    }

    public void removeScreen(Screen screen) {
        screenCache.remove(screen.getClass());
    }

    public void setScreen(Class<? extends Screen> screenClass) {
        Screen screen = screenCache.get(screenClass);
        if(screen == null) {
            throw new GdxRuntimeException("No screen with class " + screenClass + "found in the screen cache");
        }
        super.setScreen(screen);
    }

    @Override
    public void render() { // runs every frame / draws everything / checks for updates
        glProfiler.reset();

        Gdx.gl.glClearColor(0f,0f,0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        super.render();

        Gdx.graphics.setTitle("Sushi Belt - Draw calls: " + glProfiler.getDrawCalls());
        fpsLogger.log();
    }


    @Override
    public void dispose() {
        screenCache.values().forEach(Screen::dispose);
        screenCache.clear();

        this.batch.dispose();
        this.assetService.debugDiagnostics();
        this.assetService.dispose();
    }

    public Batch getBatch() {
        return batch;
    }

    public AssetService getAssetService() {
        return assetService;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public void setInputProcessors(InputProcessor... processors) {
        inputMultiplexer.clear();
        if(processors == null) return;

        for (InputProcessor processor : processors) {
            inputMultiplexer.addProcessor(processor);
        }
    }



}
