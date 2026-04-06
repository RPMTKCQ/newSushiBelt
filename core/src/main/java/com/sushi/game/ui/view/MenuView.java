package com.sushi.game.ui.view;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.sushi.game.asset.MapAsset;
import com.sushi.game.asset.SoundAsset;
import com.sushi.game.ui.model.MenuViewModel;

import java.util.ArrayList;
import java.util.List;

public class MenuView extends View<MenuViewModel> {

    private Group selectedItem;
    private boolean isInitialSelectionDone = false;

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private float holdTimer = 0f;
    private static final float REPEAT_RATE = 0.08f;

    private List<Group> mainMenuItems;
    private List<Group> modeMenuItems;
    private List<Group> levelMenuItems;
    private List<Group> currentMenuItems;

    private Table modeStageTable;
    private Table modeEndlessTable;

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

        setFillParent(true);

        buildMainMenu();
        buildModeSelectMenu();
        buildLevelSelectMenu();

        showMainMenu();

        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.SHIFT_LEFT || keycode == Input.Keys.SHIFT_RIGHT) {
                    if (currentState == MenuState.MODE) {
                        viewModel.playSound(SoundAsset.MENU_BACK);
                        showMainMenu();
                        return true;
                    } else if (currentState == MenuState.LEVEL) {
                        viewModel.playSound(SoundAsset.MENU_BACK);
                        showModeSelectMenu();
                        return true;
                    }
                }
                return handleKeyDown(keycode);
            }

            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                return handleKeyUp(keycode);
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button == Input.Buttons.RIGHT) {
                    if (currentState == MenuState.MODE) {
                        viewModel.playSound(SoundAsset.MENU_BACK);
                        showMainMenu();
                        return true;
                    } else if (currentState == MenuState.LEVEL) {
                        viewModel.playSound(SoundAsset.MENU_BACK);
                        showModeSelectMenu();
                        return true;
                    }
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
        modeStageTable = new Table();
        TextButton stageBtn = createButton("Stage", () -> showLevelSelectMenu(false));
        modeStageTable.add(stageBtn).padBottom(10.0f).minWidth(150.0f).maxWidth(150.0f).row();

        Label stageDesc = new Label("Reach the quota within a limited amount of time!", skin, "small");
        stageDesc.setAlignment(Align.center);
        stageDesc.setWrap(true);
        modeStageTable.add(stageDesc).padTop(10.0f).minWidth(150.0f);

        modeEndlessTable = new Table();
        TextButton endlessBtn = createButton("Endless", () -> showLevelSelectMenu(true));
        modeEndlessTable.add(endlessBtn).padBottom(10.0f).minWidth(150.0f).maxWidth(150.0f).row();

        Label endlessDesc = new Label("Keep the reputation up as long as possible!", skin, "small");
        endlessDesc.setAlignment(Align.center);
        endlessDesc.setWrap(true);
        modeEndlessTable.add(endlessDesc).padTop(10.0f).minWidth(150.0f);

        modeMenuItems.add(stageBtn);
        modeMenuItems.add(endlessBtn);
    }

    private void buildLevelSelectMenu() {
        Table level1 = createLevelButton("Level 1 (Easy)", "level-1", () -> viewModel.startGame(selectedIsEndless, MapAsset.STAGE_1));
        Table level2 = createLevelButton("Level 2 (Medium)", "level-2", () -> viewModel.startGame(selectedIsEndless, MapAsset.STAGE_2));
        Table level3 = createLevelButton("Level 3 (Hard)", "level-1", () -> viewModel.startGame(selectedIsEndless, MapAsset.STAGE_3));

        levelMenuItems.add(level1);
        levelMenuItems.add(level2);
        levelMenuItems.add(level3);
    }

    private void showMainMenu() {
        currentState = MenuState.MAIN;
        clearChildren();
        this.selectedItem = null;

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
        this.selectedItem = null;

        Label title = new Label("Choose Game Mode", skin, "title");
        add(title).padBottom(30f).row();

        Table optionsTable = new Table();
        optionsTable.add(modeStageTable).padRight(30.0f).minSize(100.0f);
        optionsTable.add(modeEndlessTable).padLeft(30.0f).minSize(100.0f);

        add(optionsTable).padBottom(20f).row();

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
        this.selectedItem = null;

        Label title = new Label("Choose Level", skin, "title");
        add(title).padBottom(30f).row();

        Table levelsRow = new Table();
        levelsRow.add(levelMenuItems.get(0)).padRight(30f);
        levelsRow.add(levelMenuItems.get(1)).padRight(30f);
        levelsRow.add(levelMenuItems.get(2));

        add(levelsRow).padBottom(20f).row();

        currentMenuItems = levelMenuItems;
        pack();

        if (!currentMenuItems.isEmpty()) {
            selectMenuItem(currentMenuItems.get(0));
        }
    }

    private TextButton createButton(String text, Runnable onClick) {
        TextButton.TextButtonStyle customStyle = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
        customStyle.over = null;
        customStyle.checkedOver = null;
        customStyle.down = null;

        TextButton button = new TextButton(text, customStyle);
        button.setTransform(true);
        button.setColor(Color.LIGHT_GRAY);

        Runnable unifiedAction = () -> {
            viewModel.playSound(SoundAsset.MENU_SELECT);
            onClick.run();
        };
        button.setUserObject(unifiedAction);

        button.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                unifiedAction.run();
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
        table.setTransform(true);
        table.setColor(Color.LIGHT_GRAY);

        Label label = new Label(labelText, skin);
        label.setColor(skin.getColor("black"));
        table.add(label).row();

        Slider slider = new Slider(0f, 1f, 0.1f, false, skin);
        slider.setValue(initialValue);

        slider.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onUpdate.accept(slider.getValue());
                viewModel.playSound(SoundAsset.MENU_HOVER);
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
        table.setTransform(true);
        table.setColor(Color.LIGHT_GRAY);

        Image image = new Image(skin, imageKey);
        image.setName("level-image");
        image.setColor(0.7f, 0.7f, 0.7f, 1f);
        table.add(image).size(130f, 100f).padBottom(10f).row();

        Label label = new Label(text, skin);
        label.setColor(skin.getColor("white"));
        table.add(label);

        Runnable unifiedAction = () -> {
            viewModel.playSound(SoundAsset.MENU_SELECT);
            onClick.run();
        };
        table.setUserObject(unifiedAction);

        table.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                unifiedAction.run();
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                selectMenuItem(table);
            }
        });
        return table;
    }

    private void selectMenuItem(Group group) {
        if (this.selectedItem == group) return;

        if (this.selectedItem != null) {
            this.selectedItem.clearActions();
            this.selectedItem.setScale(1f);
            this.selectedItem.setColor(Color.LIGHT_GRAY);
        }

        if (this.selectedItem != null) {
            viewModel.playSound(SoundAsset.MENU_HOVER);
        }

        this.selectedItem = group;

        if (this.selectedItem != null) {
            this.selectedItem.setOrigin(this.selectedItem.getWidth() / 2f, this.selectedItem.getHeight() / 2f);
            this.selectedItem.clearActions();
            this.selectedItem.setScale(1.15f);
            this.selectedItem.setColor(Color.WHITE);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (!isInitialSelectionDone && currentMenuItems != null && !currentMenuItems.isEmpty()) {
            selectMenuItem(currentMenuItems.get(0));
            isInitialSelectionDone = true;
        }

        if (leftPressed || rightPressed) {
            holdTimer += delta;
            while (holdTimer >= REPEAT_RATE) {
                holdTimer -= REPEAT_RATE;
                adjustSlider(leftPressed ? -0.1f : 0.1f);
            }
        }
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
                adjustSlider(-0.1f);
            } else if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
                rightPressed = true;
                holdTimer = 0f;
                adjustSlider(0.1f);
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

    private void adjustSlider(float amount) {
        if (selectedItem != null && selectedItem.getUserObject() instanceof Slider slider) {
            slider.setValue(slider.getValue() + amount);
        }
    }

    private void executeSelectedAction() {
        if (selectedItem != null && selectedItem.getUserObject() instanceof Runnable action) {
            action.run();
        }
    }

    // NEW: Lets external screens force the menu to skip to the Mode Select tab!
    public void jumpToModeSelect() {
        showModeSelectMenu();
    }

    @Override
    public boolean handle(Event event) {
        return false;
    }
}
