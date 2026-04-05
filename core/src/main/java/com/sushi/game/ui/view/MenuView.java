package com.sushi.game.ui.view;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
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
    private List<Group> currentMenuItems;

    private boolean inModeSelect = false;

    public MenuView(Stage stage, Skin skin, MenuViewModel viewModel) {
        super(stage, skin, viewModel);
    }

    @Override
    protected void setupUI() {
        mainMenuItems = new ArrayList<>();
        modeMenuItems = new ArrayList<>();

        this.selectionImg = new Image(skin, "selection-2");
        this.selectionImg.setTouchable(Touchable.disabled);

        setFillParent(true);

        buildMainMenu();
        buildModeSelectMenu();

        // Start with the Main Menu
        showMainMenu();

        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                // SHIFT to go back
                if (inModeSelect && (keycode == Input.Keys.SHIFT_LEFT || keycode == Input.Keys.SHIFT_RIGHT)) {
                    showMainMenu();
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
                // RIGHT CLICK to go back
                if (inModeSelect && button == Input.Buttons.RIGHT) {
                    showMainMenu();
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
        TextButton stageBtn = createButton("Stage Mode", viewModel::startLevelMode);
        TextButton endlessBtn = createButton("Endless Mode", viewModel::startEndlessMode);

        modeMenuItems.add(stageBtn);
        modeMenuItems.add(endlessBtn);
    }

    private void showMainMenu() {
        inModeSelect = false;
        clearChildren();

        Label title = new Label("Sushi Belt", skin, "title");
        add(title).row();

        Table menuBox = new Table();
        menuBox.setBackground(skin.getDrawable("menu-bg"));
        menuBox.padTop(40.0f).padBottom(43.0f).padLeft(25.0f).padRight(25.0f);

        menuBox.add(mainMenuItems.get(0)).minWidth(150f).fillX().row();
        menuBox.add(mainMenuItems.get(1)).padTop(10f).fillX().row();
        menuBox.add(mainMenuItems.get(2)).padTop(10f).fillX().row();
        menuBox.add(mainMenuItems.get(3)).minWidth(100f).padTop(10f).fillX();

        add(menuBox).padTop(30.0f).row();

        Label footer = new Label("Three Bits", skin, "small");
        footer.setColor(skin.getColor("white"));
        add(footer).padTop(10.0f).expandX().align(Align.bottom);

        currentMenuItems = mainMenuItems;

        // Force LibGDX to calculate the dimensions before we try to size the selection ring!
        pack();

        if (!currentMenuItems.isEmpty()) {
            selectMenuItem(currentMenuItems.get(0));
        }
    }

    private void showModeSelectMenu() {
        inModeSelect = true;
        clearChildren();

        Label title = new Label("Sushi Belt", skin, "title");
        add(title).row();

        Table menuBox = new Table();
        menuBox.setBackground(skin.getDrawable("menu-bg"));
        menuBox.padTop(40.0f).padBottom(43.0f).padLeft(25.0f).padRight(25.0f);

        Label modeTitle = new Label("Choose Mode", skin);
        modeTitle.setColor(skin.getColor("black"));
        menuBox.add(modeTitle).padBottom(20f).colspan(2).row();

        menuBox.add(modeMenuItems.get(0)).minWidth(120f).padRight(10f); // Stage
        menuBox.add(modeMenuItems.get(1)).minWidth(120f);               // Endless

        add(menuBox).padTop(30.0f).row();

        Label footer = new Label("Three Bits", skin, "small");
        footer.setColor(skin.getColor("white"));
        add(footer).padTop(10.0f).expandX().align(Align.bottom);

        currentMenuItems = modeMenuItems;

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
        button.setUserObject(onClick);
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

    private void selectMenuItem(Group group) {
        if (selectionImg.getParent() != null) {
            selectionImg.getParent().removeActor(selectionImg);
        }
        this.selectedItem = group;

        // TextButton and Table are both Groups natively in LibGDX!
        group.addActor(selectionImg);
        selectionImg.toBack();

        float extraSize = 10f;
        float halfExtra = extraSize * 0.5f;
        float resizeTime = 0.2f;

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

        if (inModeSelect) {
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
        if (selectedItem != null && selectedItem.getUserObject() instanceof Runnable action) {
            action.run();
        }
    }

    @Override
    public boolean handle(Event event) {
        return false;
    }
}
