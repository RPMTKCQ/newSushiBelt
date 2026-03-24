package com.sushi.game.ui.view;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.sushi.game.ui.model.MenuViewModel;

public class MenuView extends View<MenuViewModel> {

    private final Image selectionImg;
    private Group selectedItem;
    private boolean isInitialSelectionDone = false;

    // Key Repeat Variables
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private float holdTimer = 0f;
    private float repeatTimer = 0f;
    private static final float HOLD_DELAY = 0.2f;  // 1 second before fast-sliding starts
    private static final float REPEAT_RATE = 0.1f; // How fast it slides once holding

    public MenuView(Stage stage, Skin skin, MenuViewModel viewModel) {
        super(stage, skin, viewModel);

        this.selectionImg = new Image(skin, "selection");
        this.selectionImg.setTouchable(Touchable.disabled);

        stage.setKeyboardFocus(this);

        this.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                return handleKeyDown(keycode);
            }

            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                return handleKeyUp(keycode);
            }
        });
    }

    private void selectMenuItem(Group menuItem) {
        if(selectionImg.getParent() != null) {
            selectionImg.getParent().removeActor(selectionImg);
        }
        this.selectedItem = menuItem;

        float extraSize = 0f;
        float halfExtraSize = extraSize * 0.5f;
        float resizeTime = 0.2f;

        menuItem.addActor(selectionImg);
        selectionImg.setPosition(-halfExtraSize, -halfExtraSize);

        selectionImg.setSize(menuItem.getWidth() + extraSize, menuItem.getHeight() + extraSize);

        selectionImg.clearActions();
        selectionImg.addAction(Actions.forever(Actions.sequence(
            Actions.parallel(
                Actions.sizeBy(extraSize, extraSize, resizeTime, Interpolation.linear),
                Actions.moveBy(-halfExtraSize, -halfExtraSize, resizeTime, Interpolation.linear)
            ),
            Actions.parallel(
                Actions.sizeBy(-extraSize, -extraSize, resizeTime, Interpolation.linear),
                Actions.moveBy(halfExtraSize, halfExtraSize, resizeTime, Interpolation.linear)
            )
        )));
    }

    @Override
    protected void setupUI() {
        setFillParent(true);

        Label label = new Label("Sushi Belt", skin, "title");
        add(label);

        setupMenuContent();

        row();
        label = new Label("Three Bits", skin, "small");
        label.setColor(skin.getColor("white"));
        add(label).padTop(10.0f).expandX().align(Align.bottom);
    }

    private void setupMenuContent() {
        row();
        Table contentTable = new Table();
        contentTable.setBackground(skin.getDrawable("menu-bg"));
        contentTable.padLeft(25.0f);
        contentTable.padRight(25.0f);
        contentTable.padTop(40.0f);
        contentTable.padBottom(43.0f);

        TextButton textButton = new TextButton("Start", skin);
        textButton.setName(MenuOption.START_GAME.name());
        textButton.setColor(skin.getColor("white"));
        onClick(textButton, viewModel::startGame);
        onEnter(textButton, this::selectMenuItem);
        contentTable.add(textButton).minWidth(100.0f).fillX();

        contentTable.row();

        Slider musicSlider = setupVolumeSlider(contentTable,"Music Volume", MenuOption.MUSIC_VOLUME);
        musicSlider.setValue(viewModel.getMusicVolume());
        onChange(musicSlider, (slider-> viewModel.setMusicVolume(slider.getValue())));

        Slider soundSlider = setupVolumeSlider(contentTable,"Sound Volume", MenuOption.SOUND_VOLUME);
        onChange(soundSlider, (slider-> viewModel.setSoundVolume(slider.getValue())));
        soundSlider.setValue(viewModel.getSoundVolume());

        contentTable.row();

        TextButton quitButton = new TextButton("Quit", skin);
        quitButton.setName(MenuOption.QUIT_GAME.name());
        contentTable.add(quitButton).padTop(10.0f).minWidth(100.0f).fillX();
        onClick(quitButton, viewModel::quitGame);
        onEnter(quitButton, this::selectMenuItem);

        add(contentTable).padTop(30.0f);
    }

    private Slider setupVolumeSlider(Table contentTable, String title, MenuOption volume) {
        Table table = new Table();
        table.setName(volume.name());
        table.setColor(skin.getColor("black"));

        Label label = new Label(title, skin);
        label.setColor(skin.getColor("black"));
        table.add(label);

        table.row();
        Slider slider = new Slider(0f, 1f, 0.1f, false, skin);

        table.add(slider).growX();
        contentTable.add(table).padTop(10.0f).fillX().row();

        onEnter(table, this::selectMenuItem);
        return slider;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (!isInitialSelectionDone) {
            this.selectedItem = findActor(MenuOption.START_GAME.name());
            if (this.selectedItem != null) {
                selectMenuItem(this.selectedItem);
            }
            isInitialSelectionDone = true;
        }

        // Key Repeat Logic for Sliders
        if (leftPressed || rightPressed) {
            holdTimer += delta;
            if (holdTimer >= HOLD_DELAY) {
                repeatTimer += delta;
                if (repeatTimer >= REPEAT_RATE) {
                    repeatTimer = 0f;
                    MenuOption currentOption = MenuOption.valueOf(selectedItem.getName());
                    adjustSliderViaKeyboard(currentOption, leftPressed ? -0.1f : 0.1f);
                }
            }
        }
    }

    private boolean handleKeyDown(int keycode) {
        if (selectedItem == null) return false;

        MenuOption[] options = MenuOption.values();
        int currentIndex = 0;
        for (int i = 0; i < options.length; i++) {
            if (options[i].name().equals(selectedItem.getName())) {
                currentIndex = i;
                break;
            }
        }

        if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
            currentIndex = Math.max(0, currentIndex - 1);
            updateSelectionFromKeyboard(options[currentIndex]);
            return true;
        } else if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
            currentIndex = Math.min(options.length - 1, currentIndex + 1);
            updateSelectionFromKeyboard(options[currentIndex]);
            return true;
        } else if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
            leftPressed = true;
            holdTimer = 0f; // Reset hold timer on fresh press
            adjustSliderViaKeyboard(options[currentIndex], -0.1f);
            return true;
        } else if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
            rightPressed = true;
            holdTimer = 0f; // Reset hold timer on fresh press
            adjustSliderViaKeyboard(options[currentIndex], 0.1f);
            return true;
        } else if (keycode == Input.Keys.SPACE || keycode == Input.Keys.ENTER) {
            executeActionViaKeyboard(options[currentIndex]);
            return true;
        }
        return false;
    }

    private boolean handleKeyUp(int keycode) {
        if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
            leftPressed = false;
            holdTimer = 0f;
            return true;
        } else if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
            rightPressed = false;
            holdTimer = 0f;
            return true;
        }
        return false;
    }

    private void updateSelectionFromKeyboard(MenuOption option) {
        Group actor = findActor(option.name());
        if (actor != null) {
            selectMenuItem(actor);
        }
    }

    private void adjustSliderViaKeyboard(MenuOption option, float amount) {
        if (option == MenuOption.MUSIC_VOLUME || option == MenuOption.SOUND_VOLUME) {
            Group actor = findActor(option.name());
            if (actor != null) {
                for (Actor child : actor.getChildren()) {
                    if (child instanceof Slider slider) {
                        slider.setValue(slider.getValue() + amount);
                        break;
                    }
                }
            }
        }
    }

    private void executeActionViaKeyboard(MenuOption option) {
        if (option == MenuOption.START_GAME) {
            viewModel.startGame();
        } else if (option == MenuOption.QUIT_GAME) {
            viewModel.quitGame();
        }
    }

    enum MenuOption {
        START_GAME,
        MUSIC_VOLUME,
        SOUND_VOLUME,
        QUIT_GAME
    }

    @Override
    public boolean handle(Event event) {
        return false;
    }
}
