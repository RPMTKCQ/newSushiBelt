package com.sushi.game.ui.view;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.sushi.game.ui.model.MenuViewModel;

public class MenuView extends View<MenuViewModel> {

    private final Image selectionImg;
    private Group selectedItem;

    public MenuView(Stage stage, Skin skin, MenuViewModel viewModel) {
        super(stage, skin, viewModel);

        this.selectionImg = new Image(skin, "selection");
        this.selectionImg.setTouchable(Touchable.disabled);
        this.selectedItem = findActor(MenuOption.START_GAME.name());
        selectMenuItem(this.selectedItem);

        stage.setDebugAll(true); // debug lines
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
//        stage.addActor(table);

    }

    private void setupMenuContent() {
        Label label;
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
        contentTable.add(textButton).minWidth(100.0f);

        contentTable.row();

        Slider musicSlider = setupVolumeSlider(contentTable,"Music Volume", MenuOption.MUSIC_VOLUME);
        musicSlider.setValue(viewModel.getMusicVolume());
        onChange(musicSlider, (slider-> viewModel.setMusicVolume(slider.getValue())));
        Slider soundSlider = setupVolumeSlider(contentTable,"Sound Volume", MenuOption.SOUND_VOLUME);
        onChange(soundSlider, (slider-> viewModel.setSoundVolume(slider.getValue())));
        soundSlider.setValue(viewModel.getSoundVolume());



        contentTable.row();
        textButton = new TextButton("Quit", skin);
        textButton.setName(MenuOption.QUIT_GAME.name());
        contentTable.add(textButton).padTop(10.0f).minWidth(100.0f);
        onClick(textButton, viewModel::quitGame);
        onEnter(textButton, this::selectMenuItem);
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
        table.add(slider);
        contentTable.add(table).padTop(10.0f).row();

        onEnter(table, this::selectMenuItem);
        return slider;
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
