package com.sushi.game.ui.view;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.sushi.game.asset.MapAsset;
import com.sushi.game.ui.model.MenuViewModel;

import java.util.ArrayList;
import java.util.List;

public class MenuView extends View<MenuViewModel> {

    private Image selectionImg;
    private Group selectedItem;
    private boolean isInitialSelectionDone = false;

    // Key Repeat Variables
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private float holdTimer = 0f;
    private float repeatTimer = 0f;
    private static final float HOLD_DELAY = 0.2f;
    private static final float REPEAT_RATE = 0.1f;

    private List<Group> mainMenuItems;
    private List<Group> modeMenuItems;
    private List<Group> levelMenuItems;
    private List<Group> currentMenuItems;

    private enum MenuState { MAIN, MODE, LEVEL }
    private MenuState currentState = MenuState.MAIN;

    private boolean selectedIsEndless = false;

    public MenuView(Stage stage, Skin skin, MenuViewModel viewModel) {
        super(stage, skin, viewModel);
    }

    @Override
    protected void setupUI() {
        mainMenuItems = new ArrayList<>();
        modeMenuItems = new ArrayList<>();
        levelMenuItems = new ArrayList<>();

        this.selectionImg = new Image(skin, "selection-2");
        this.selectionImg.setTouchable(Touchable.disabled);

        setFillParent(true);

        buildMainMenu();
        buildModeSelectMenu();
        buildLevelSelectMenu();

        // Start with the Main Menu
        showMainMenu();

        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (currentState == MenuState.MODE && (keycode == Input.Keys.SHIFT_LEFT || keycode == Input.Keys.SHIFT_RIGHT)) {
                    showMainMenu();
                    return true;
                } else if (currentState == MenuState.LEVEL && (keycode == Input.Keys.SHIFT_LEFT || keycode == Input.Keys.SHIFT_RIGHT)) {
                    showModeSelectMenu();
                    return true;
                }
                return handleKeyDown(keycode);
            }

            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                return handleKeyUp(keycode);
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (currentState == MenuState.MODE && button == Input.Buttons.RIGHT) {
                    showMainMenu();
                    return true;
                } else if (currentState == MenuState.LEVEL && button == Input.Buttons.RIGHT) {
                    showModeSelectMenu();
                    return true;
                }
                return false;
            }
        });
    }

    private void buildMainMenu() {
        TextButton playBtn = createButton("Play", this::showModeSelectMenu);
        Table musicSlider = createSlider("Music Volume", viewModel.getMusicVolume(), viewModel::setMusicVolume);
        Table soundSlider = createSlider("Sound Volume", viewModel.getSoundVolume(), viewModel::setSoundVolume);
        TextButton quitBtn = createButton("Quit", viewModel::quitGame);

        mainMenuItems.add(playBtn);
        mainMenuItems.add(musicSlider);
        mainMenuItems.add(soundSlider);
        mainMenuItems.add(quitBtn);
    }

    private void buildModeSelectMenu() {
        // Group 1: Stage Mode Button & Description
        Table stageTable = new Table();
        TextButton stageBtn = createButton("Stage", () -> showLevelSelectMenu(false));
        stageTable.add(stageBtn).padBottom(10.0f).minWidth(100.0f).maxWidth(100.0f).row();

        Label stageDesc = new Label("Reach the quota within a limited set amount of time!", skin, "small");
        stageDesc.setAlignment(Align.center);
        stageDesc.setWrap(true);
        stageTable.add(stageDesc).padTop(10.0f).minWidth(100.0f);

        // Group 2: Endless Mode Button & Description
        Table endlessTable = new Table();
        TextButton endlessBtn = createButton("Endless", () -> showLevelSelectMenu(true));
        endlessTable.add(endlessBtn).padBottom(10.0f).minWidth(100.0f).maxWidth(100.0f).row();

        Label endlessDesc = new Label("Keep the reputation up as long as possible!", skin, "small");
        endlessDesc.setAlignment(Align.center);
        endlessDesc.setWrap(true);
        endlessTable.add(endlessDesc).padTop(10.0f).minWidth(100.0f);

        // We only add the buttons to the list so the keyboard can navigate to them
        modeMenuItems.add(stageBtn);
        modeMenuItems.add(endlessBtn);

        // Attach the tables as user objects to the buttons, so we can retrieve them in showModeSelectMenu
        stageBtn.setUserObject(stageTable);
        endlessBtn.setUserObject(endlessTable);
    }

    private void buildLevelSelectMenu() {
        Table level1 = createLevelButton("Level 1 (Easy)", "level-1", () -> viewModel.startGame(selectedIsEndless, MapAsset.STAGE_1));
        Table level2 = createLevelButton("Level 2 (Medium)", "level-1", () -> viewModel.startGame(selectedIsEndless, MapAsset.STAGE_2));
        Table level3 = createLevelButton("Level 3 (Hard)", "level-1", () -> viewModel.startGame(selectedIsEndless, MapAsset.STAGE_3));

        levelMenuItems.add(level1);
        levelMenuItems.add(level2);
        levelMenuItems.add(level3);
    }

    private void showMainMenu() {
        currentState = MenuState.MAIN;
        clearChildren();

        Label title = new Label("Sushi Belt", skin, "title");
        add(title).row();

        Table menuBox = new Table();
        menuBox.setBackground(skin.getDrawable("menu-bg"));
        menuBox.padTop(40.0f).padBottom(43.0f).padLeft(25.0f).padRight(25.0f);

        menuBox.add(mainMenuItems.get(0)).grow().minWidth(100f).row();
        menuBox.add(mainMenuItems.get(1)).padTop(10f).row();
        menuBox.add(mainMenuItems.get(2)).padTop(10f).row();
        menuBox.add(mainMenuItems.get(3)).padTop(10f).grow().minWidth(100f);

        add(menuBox).padTop(30.0f).row();

        Label footer = new Label("Three Bits", skin, "small");
        footer.setColor(skin.getColor("white"));
        add(footer).padTop(10.0f).expandX().align(Align.bottom);

        currentMenuItems = mainMenuItems;
        pack();

        if (!currentMenuItems.isEmpty()) {
            selectMenuItem(currentMenuItems.get(0));
        }
    }

    private void showModeSelectMenu() {
        currentState = MenuState.MODE;
        clearChildren();

        Label title = new Label("Choose Game Mode", skin, "title");
        add(title).padBottom(30f).row();

        Table optionsTable = new Table();

        Table stageTable = (Table) modeMenuItems.get(0).getUserObject();
        Table endlessTable = (Table) modeMenuItems.get(1).getUserObject();

        optionsTable.add(stageTable).padRight(30.0f).minSize(100.0f);
        optionsTable.add(endlessTable).padLeft(30.0f).minSize(100.0f);

        add(optionsTable).padBottom(20f).row();

//        Label footer = new Label("Three Bits", skin, "small");
//        footer.setColor(skin.getColor("white"));
//        add(footer).padTop(30.0f).expandX().align(Align.bottom);

        currentMenuItems = modeMenuItems;
        pack();

        if (!currentMenuItems.isEmpty()) {
            selectMenuItem(currentMenuItems.get(0));
        }
    }

    private void showLevelSelectMenu(boolean isEndless) {
        currentState = MenuState.LEVEL;
        selectedIsEndless = isEndless;
        clearChildren();

        Label title = new Label("Choose Level", skin, "title");
        add(title).padBottom(30f).row();

        Table levelsRow = new Table();
        levelsRow.add(levelMenuItems.get(0)).padRight(30f);
        levelsRow.add(levelMenuItems.get(1)).padRight(30f);
        levelsRow.add(levelMenuItems.get(2));

        add(levelsRow).padBottom(20f).row();

        Label footer = new Label("Three Bits", skin, "small");
        footer.setColor(skin.getColor("white"));
        add(footer).padTop(30.0f).expandX().align(Align.bottom);

        currentMenuItems = levelMenuItems;
        pack();

        if (!currentMenuItems.isEmpty()) {
            selectMenuItem(currentMenuItems.get(0));
        }
    }

    private TextButton createButton(String text, Runnable onClick) {
        TextButton button = new TextButton(text, skin);
        button.setColor(skin.getColor("white"));

        button.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onClick.run();
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                selectMenuItem(button);
            }
        });

        button.setName(String.valueOf(onClick.hashCode()));
        return button;
    }

    private Table createSlider(String labelText, float initialValue, java.util.function.Consumer<Float> onUpdate) {
        Table table = new Table();
        table.setColor(skin.getColor("black"));

        Label label = new Label(labelText, skin);
        label.setColor(skin.getColor("black"));
        table.add(label).row();

        Slider slider = new Slider(0f, 1f, 0.05f, false, skin);
        slider.setValue(initialValue);

        slider.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onUpdate.accept(slider.getValue());
            }
        });

        table.add(slider).growX();

        table.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                selectMenuItem(table);
            }
        });
        table.setUserObject(slider);
        return table;
    }

    private Table createLevelButton(String text, String imageKey, Runnable onClick) {
        Table table = new Table();

        Image image = new Image(skin, imageKey);
        image.setName("level-image");
        image.setColor(0.7f, 0.7f, 0.7f, 1f);
        table.add(image).size(130f, 100f).padBottom(10f).row();

        Label label = new Label(text, skin);
        label.setColor(skin.getColor("white"));
        table.add(label);

        table.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onClick.run();
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                selectMenuItem(table);
            }
        });
        table.setUserObject(onClick);
        return table;
    }

    private void selectMenuItem(Group group) {
        if (this.selectedItem instanceof Button oldButton) {
            oldButton.setChecked(false);
        }

        if (selectionImg.getParent() != null) {
            selectionImg.getParent().removeActor(selectionImg);
        }

        this.selectedItem = group;

        if (this.selectedItem instanceof Button newButton) {
            newButton.setChecked(true);
        }

        if (currentState == MenuState.LEVEL) {
            for (Group item : currentMenuItems) {
                Actor img = item.findActor("level-image");
                if (img != null) img.setColor(0.7f, 0.7f, 0.7f, 1f);
            }
            Actor img = group.findActor("level-image");
            if (img != null) img.setColor(Color.WHITE);
        }

        group.addActor(selectionImg);
        selectionImg.toBack();

        float extraSize = 5f;
        float halfExtra = extraSize * 0.5f;
        float resizeTime = 0.365f;

        selectionImg.setSize(group.getWidth() + extraSize, group.getHeight() + extraSize);
        selectionImg.setPosition(-halfExtra, -halfExtra);

        selectionImg.clearActions();
        selectionImg.addAction(Actions.forever(Actions.sequence(
            Actions.parallel(
                Actions.sizeBy(extraSize, extraSize, resizeTime, Interpolation.linear),
                Actions.moveBy(-halfExtra, -halfExtra, resizeTime, Interpolation.linear)
            ),
            Actions.parallel(
                Actions.sizeBy(-extraSize, -extraSize, resizeTime, Interpolation.linear),
                Actions.moveBy(halfExtra, halfExtra, resizeTime, Interpolation.linear)
            )
        )));
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (!isInitialSelectionDone && currentMenuItems != null && !currentMenuItems.isEmpty()) {
            selectMenuItem(currentMenuItems.get(0));
            isInitialSelectionDone = true;
        }

        handleKeyHold(delta);
    }

    private boolean handleKeyDown(int keycode) {
        if (currentMenuItems == null || currentMenuItems.isEmpty()) return false;

        int currentIndex = currentMenuItems.indexOf(selectedItem);
        int newIndex = currentIndex;

        if (currentState == MenuState.MODE || currentState == MenuState.LEVEL) {
            if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
                newIndex = (currentIndex - 1 + currentMenuItems.size()) % currentMenuItems.size();
            } else if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
                newIndex = (currentIndex + 1) % currentMenuItems.size();
            }
        } else {
            if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
                newIndex = (currentIndex - 1 + currentMenuItems.size()) % currentMenuItems.size();
            } else if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
                newIndex = (currentIndex + 1) % currentMenuItems.size();
            } else if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
                leftPressed = true;
                holdTimer = 0f;
                adjustSlider(-0.05f);
            } else if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
                rightPressed = true;
                holdTimer = 0f;
                adjustSlider(0.05f);
            }
        }

        if (keycode == Input.Keys.SPACE || keycode == Input.Keys.ENTER) {
            executeSelectedAction();
        }

        if (newIndex != currentIndex) {
            selectMenuItem(currentMenuItems.get(newIndex));
            return true;
        }
        return false;
    }

    private boolean handleKeyUp(int keycode) {
        if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
            leftPressed = false;
            return true;
        } else if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
            rightPressed = false;
            return true;
        }
        return false;
    }

    private void handleKeyHold(float delta) {
        if (leftPressed || rightPressed) {
            holdTimer += delta;
            if (holdTimer >= HOLD_DELAY) {
                repeatTimer += delta;
                if (repeatTimer >= REPEAT_RATE) {
                    repeatTimer = 0f;
                    adjustSlider(leftPressed ? -0.05f : 0.05f);
                }
            }
        }
    }

    private void adjustSlider(float amount) {
        if (selectedItem != null && selectedItem.getUserObject() instanceof Slider slider) {
            slider.setValue(slider.getValue() + amount);
        }
    }

    private void executeSelectedAction() {
        if (selectedItem != null) {
            if (selectedItem instanceof TextButton button) {
                InputEvent event = new InputEvent();
                event.setType(InputEvent.Type.touchDown);
                button.fire(event);
                event.setType(InputEvent.Type.touchUp);
                button.fire(event);
            } else if (selectedItem.getUserObject() instanceof Runnable action) {
                action.run();
            }
        }
    }

    @Override
    public boolean handle(Event event) {
        return false;
    }
}
