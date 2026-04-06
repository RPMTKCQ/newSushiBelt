package com.sushi.game.ui.view;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.sushi.game.asset.AssetService;
import com.sushi.game.asset.AtlasAsset;

import java.util.ArrayList;
import java.util.List;

public class TopBarUI extends Table {

    private final Skin skin;
    private final AssetService assetService;

    private Label moneyLabel;
    private Label scoreLabel;

    private Table receiptRow;
    private final List<ReceiptCardData> receiptCards = new ArrayList<>();

    private String cookingCustomerId = null;
    private ReceiptCardData currentlyDraggingCard = null;
    private int receiptKeyboardSelectedIndex = -1;
    private boolean receiptIsGrabbed = false;
    private boolean lastSubmitSorted = false;

    private static class ReceiptCardData {
        Table card;
        Image dishImage;
        Label dishLabel;
        ProgressBar timerBar;
        ProgressBar cookingBar;
        Label secondsLabel;
        String customerId;
        float timer;
        float maxTime;
        boolean cooked = false;

        ReceiptCardData(Table card, Image img, Label label, ProgressBar bar, ProgressBar cookingBar, Label secondsLabel, String customerId, float maxTime) {
            this.card = card;
            this.dishImage = img;
            this.dishLabel = label;
            this.timerBar = bar;
            this.cookingBar = cookingBar;
            this.secondsLabel = secondsLabel;
            this.customerId = customerId;
            this.maxTime = maxTime;
        }
    }

    public TopBarUI(Skin skin, AssetService assetService) {
        super(skin);
        this.skin = skin;
        this.assetService = assetService;

        setColor(skin.getColor("sand"));
        setTouchable(Touchable.childrenOnly);
        padLeft(50f).padRight(50f).align(Align.left);

        add(buildReceiptRow()).growX().align(Align.left);
        add(buildScorePanel()).padTop(30f).grow().align(Align.topRight);
    }

    private Table buildReceiptRow() {
        receiptRow = new Table();
        receiptRow.align(Align.left);
        receiptRow.setTouchable(Touchable.childrenOnly);
        return receiptRow;
    }

    private Table buildScorePanel() {
        Table scoreMoneyTable = new Table();
        scoreMoneyTable.align(Align.topRight);

        Table moneyTable = new Table();
        moneyTable.align(Align.left);
        Image moneyImg = new Image(skin, "money");
        moneyImg.setScaling(Scaling.fit);
        moneyTable.add(moneyImg).padRight(15f).minSize(32f);
        moneyLabel = new Label("0", skin, "powerup");
        moneyTable.add(moneyLabel).minWidth(0f);
        scoreMoneyTable.add(moneyTable).padRight(30f).align(Align.right).minWidth(300f).row();

        Table scoreTable = new Table();
        scoreTable.align(Align.left);
        Image scoreImg = new Image(skin, "bell");
        scoreImg.setScaling(Scaling.fit);
        scoreTable.add(scoreImg).padRight(15f).minSize(32f);
        scoreLabel = new Label("0", skin, "powerup");
        scoreTable.add(scoreLabel);
        scoreMoneyTable.add(scoreTable).padRight(30f).minWidth(300f);

        return scoreMoneyTable;
    }

    // FIX: Now accepts the formatted String from LevelSystem
    public void setMoney(String text) {
        moneyLabel.setText(text);
    }

    // FIX: Now accepts the formatted String from LevelSystem
    public void setScore(String text) {
        scoreLabel.setText(text);
    }

    public void setMoney(int money) {
        moneyLabel.setText(String.valueOf(money));
    }

    public void setScore(int score) {
        scoreLabel.setText(String.valueOf(score));
    }

    public void setLastSubmitSorted(boolean sorted) { this.lastSubmitSorted = sorted; }
    public boolean wasLastSubmitSorted() { return lastSubmitSorted; }

    public boolean handleKeyDown(int keycode) {
        if (receiptCards.isEmpty()) return false;
        int lockedCount = getLockedCount();

        if (keycode == Input.Keys.LEFT) {
            if (receiptKeyboardSelectedIndex == -1) {
                receiptKeyboardSelectedIndex = receiptCards.size() - 1;
            } else {
                if (receiptIsGrabbed && receiptKeyboardSelectedIndex > lockedCount) {
                    // This is the array shift for Insertion Sort via keyboard
                    ReceiptCardData temp = receiptCards.remove(receiptKeyboardSelectedIndex);
                    receiptKeyboardSelectedIndex--;
                    receiptCards.add(receiptKeyboardSelectedIndex, temp);
                    relayoutReceiptRow();
                } else if (!receiptIsGrabbed) {
                    receiptKeyboardSelectedIndex--;
                    if (receiptKeyboardSelectedIndex < 0) receiptKeyboardSelectedIndex = receiptCards.size() - 1;
                }
            }
            updateCardColors();
            return true;
        } else if (keycode == Input.Keys.RIGHT) {
            if (receiptKeyboardSelectedIndex == -1) {
                receiptKeyboardSelectedIndex = 0;
            } else {
                if (receiptIsGrabbed && receiptKeyboardSelectedIndex < receiptCards.size() - 1) {
                    // This is the array shift for Insertion Sort via keyboard
                    ReceiptCardData temp = receiptCards.remove(receiptKeyboardSelectedIndex);
                    receiptKeyboardSelectedIndex++;
                    receiptCards.add(receiptKeyboardSelectedIndex, temp);
                    relayoutReceiptRow();
                } else if (!receiptIsGrabbed) {
                    receiptKeyboardSelectedIndex = (receiptKeyboardSelectedIndex + 1) % receiptCards.size();
                }
            }
            updateCardColors();
            return true;
        } else if (keycode == Input.Keys.UP) {
            if (receiptKeyboardSelectedIndex >= lockedCount && !receiptIsGrabbed) {
                receiptIsGrabbed = true;
                updateCardColors();
            }
            return true;
        } else if (keycode == Input.Keys.DOWN) {
            if (receiptIsGrabbed) receiptIsGrabbed = false;
            else receiptKeyboardSelectedIndex = -1;
            updateCardColors();
            return true;
        }
        return false;
    }

    public void updateCardColors() {
        for (int i = 0; i < receiptCards.size(); i++) {
            ReceiptCardData d = receiptCards.get(i);
            d.card.clearActions();

            if (d.cooked) {
                d.card.addAction(Actions.color(new com.badlogic.gdx.graphics.Color(1f, 0.95f, 0.2f, 1f), 0.15f));
            } else if (cookingCustomerId != null && d.customerId.equals(cookingCustomerId)) {
                d.card.addAction(Actions.color(new com.badlogic.gdx.graphics.Color(1f, 0.65f, 0.1f, 1f), 0.15f));
            } else if (d == currentlyDraggingCard || (receiptIsGrabbed && i == receiptKeyboardSelectedIndex)) {
                d.card.addAction(Actions.color(skin.getColor("sand"), 0.1f));
            } else if (currentlyDraggingCard == null && !receiptIsGrabbed && i == receiptKeyboardSelectedIndex) {
                d.card.addAction(Actions.color(new com.badlogic.gdx.graphics.Color(0.8f, 0.9f, 1f, 1f), 0.1f));
            } else {
                d.card.addAction(Actions.color(skin.getColor("white"), 0.1f));
            }
        }
    }

    private int getLockedCount() {
        int lockedCount = 0;
        for (ReceiptCardData d : receiptCards) {
            if (d.cooked || (cookingCustomerId != null && d.customerId.equals(cookingCustomerId))) lockedCount++;
        }
        return lockedCount;
    }

    public boolean isQueueSortedByUrgency() {
        List<ReceiptCardData> active = new ArrayList<>();
        for (ReceiptCardData d : receiptCards) {
            if (!d.cooked && !d.customerId.equals(cookingCustomerId)) active.add(d);
        }
        for (int i = 0; i < active.size() - 1; i++) {
            float timeRemainingA = active.get(i).maxTime - active.get(i).timer;
            float timeRemainingB = active.get(i + 1).maxTime - active.get(i + 1).timer;
            if (timeRemainingA > timeRemainingB) return false;
        }
        return true;
    }

    public void startCookingFor(String customerId) {
        this.cookingCustomerId = customerId;
        updateCardColors();
    }

    private ReceiptCardData getCookingCard() {
        if (cookingCustomerId == null) return null;
        for (ReceiptCardData d : receiptCards) {
            if (d.customerId.equals(cookingCustomerId)) return d;
        }
        return null;
    }

    public void updateCookingProgress(float ratio) {
        ReceiptCardData d = getCookingCard();
        if (d == null) return;
        d.cookingBar.setValue(ratio * 100f);
        d.cookingBar.setVisible(true);
    }

    public void resetCookingBar() {
        ReceiptCardData d = getCookingCard();
        if (d == null) return;
        d.cookingBar.setValue(0f);
        d.cookingBar.setVisible(false);
        d.cooked = true;
        cookingCustomerId = null;
        updateCardColors();
    }

    public void addOrder(String dishId, String customerId, float maxTime) {
        // The 8-card Hard Limit
        if (receiptCards.size() >= 8) return;

        Table card = new Table();
        card.setBackground(skin.getDrawable("rct-border"));
        card.align(Align.top);
        card.setTouchable(Touchable.enabled);

        Image img = new Image(assetService.get(AtlasAsset.OBJECTS).findRegion("Food/" + dishId.replace("_", "-")));
        img.setScaling(Scaling.fit);
        card.add(img).padTop(10f).minSize(80f).row();

        String[] parts = dishId.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
        Label label = new Label(sb.toString().trim(), skin, "receipt");
        label.setColor(skin.getColor("black"));
        card.add(label).spaceTop(10f).row();

        ProgressBar bar = new ProgressBar(0f, 100f, 1f, false, skin);
        bar.setValue(100f);
        card.add(bar).padLeft(-50f).padRight(-50f).spaceTop(10f).maxWidth(100f).row();

        ProgressBar cookingBar = new ProgressBar(0f, 100f, 1f, false, skin);
        cookingBar.setValue(0f);
        cookingBar.setVisible(false);
        card.add(cookingBar).padLeft(-50f).padRight(-50f).spaceTop(4f).maxWidth(100f).row();

        Label secondsLabel = new Label("30s", skin, "receipt");
        secondsLabel.setColor(skin.getColor("black"));
        secondsLabel.setAlignment(Align.center);
        card.add(secondsLabel).spaceTop(4f);

        ReceiptCardData data = new ReceiptCardData(card, img, label, bar, cookingBar, secondsLabel, customerId, maxTime);
        receiptCards.add(data);
        attachDragListener(card, data);
        relayoutReceiptRow();
        updateCardColors();
    }

    public void removeOrder(String customerId) {
        ReceiptCardData toRemove = null;
        for (ReceiptCardData d : receiptCards) {
            if (d.customerId.equals(customerId)) {
                toRemove = d;
                break;
            }
        }
        if (toRemove == null) return;

        if (!toRemove.cooked) {
            for (ReceiptCardData d : receiptCards) {
                if (d != toRemove && d.cooked && d.dishLabel.getText().toString().equals(toRemove.dishLabel.getText().toString())) {
                    d.cooked = false;
                    break;
                }
            }
        }
        receiptCards.remove(toRemove);
        toRemove.card.remove();

        if (currentlyDraggingCard == toRemove) {
            currentlyDraggingCard = null;
            receiptIsGrabbed = false;
        }

        if (receiptKeyboardSelectedIndex >= receiptCards.size()) {
            receiptKeyboardSelectedIndex = receiptCards.size() - 1;
        }
        relayoutReceiptRow();
        updateCardColors();
    }

    public String getFirstDishName() {
        for (ReceiptCardData d : receiptCards) {
            if (!d.cooked && !d.customerId.equals(cookingCustomerId)) return d.dishLabel.getText().toString().toLowerCase().replace(" ", "_");
        }
        return null;
    }

    public String getFirstCustomerId() {
        for (ReceiptCardData d : receiptCards) {
            if (!d.cooked && !d.customerId.equals(cookingCustomerId)) return d.customerId;
        }
        return null;
    }

    public void updateReceipts(float delta) {
        for (ReceiptCardData d : receiptCards) {
            d.timer += delta;
            float ratio = 1f - (d.timer / d.maxTime);
            d.timerBar.setValue(Math.max(ratio * 100f, 0f));

            int secondsLeft = Math.max(0, (int) (d.maxTime - d.timer));
            d.secondsLabel.setText(secondsLeft + "s");
            // Hardcoded black for readability
            d.secondsLabel.setColor(skin.getColor("black"));
        }
    }

    private void relayoutReceiptRow() {
        receiptRow.clearChildren();
        for (ReceiptCardData d : receiptCards) {
            if (d == currentlyDraggingCard) {
                // The "Empty Gap" shift for Insertion Sort
                Table dummySpace = new Table();
                receiptRow.add(dummySpace).padRight(10f).growY().align(Align.top).minSize(160f, 220f).maxSize(160f, 220f);
            } else {
                receiptRow.add(d.card).padRight(10f).growY().align(Align.top).minSize(160f, 220f).maxSize(160f, 220f);
            }
        }
    }

    private void attachDragListener(Table card, ReceiptCardData data) {
        final Vector2 dragStartStagePos = new Vector2();
        final Vector2 cardStartStagePos = new Vector2();

        card.addListener(new DragListener() {
            { setTapSquareSize(4f); }

            @Override
            public void dragStart(InputEvent e, float x, float y, int ptr) {
                if ((cookingCustomerId != null && data.customerId.equals(cookingCustomerId)) || data.cooked) { cancel(); return; }

                currentlyDraggingCard = data;
                receiptKeyboardSelectedIndex = receiptCards.indexOf(data);
                receiptIsGrabbed = true;

                // 1. Extract the "Key" (Detaching the card to float)
                Vector2 stagePos = card.localToStageCoordinates(new Vector2(0, 0));
                dragStartStagePos.set(e.getStageX(), e.getStageY());
                cardStartStagePos.set(stagePos);

                if (card.getStage() != null) {
                    card.getStage().addActor(card);
                    card.setPosition(stagePos.x, stagePos.y);
                }

                relayoutReceiptRow();
                card.toFront();
                updateCardColors();
            }

            @Override
            public void drag(InputEvent e, float x, float y, int ptr) {
                if ((cookingCustomerId != null && data.customerId.equals(cookingCustomerId)) || data.cooked) return;

                // Lock the Y axis to where the card started. Horizontal drag only!
                float newX = cardStartStagePos.x + (e.getStageX() - dragStartStagePos.x);
                card.setPosition(newX, cardStartStagePos.y);

                float cardCX = newX + card.getWidth() / 2f;
                int from = receiptCards.indexOf(data);
                int to = from;
                int lockedCount = getLockedCount();

                // 2. Scan the array to find the correct insertion point
                for (int i = lockedCount; i < receiptCards.size(); i++) {
                    if (i == from) continue;

                    Table otherCard = receiptCards.get(i).card;
                    if (otherCard.getStage() == null) continue;

                    Vector2 otherPos = otherCard.localToStageCoordinates(new Vector2(0, 0));
                    float otherCX = otherPos.x + otherCard.getWidth() / 2f;

                    if (cardCX < otherCX && i < to) to = i;
                    else if (cardCX > otherCX && i > to) to = i;
                }

                to = Math.max(lockedCount, Math.min(to, receiptCards.size() - 1));

                // 3. Shift the array elements to make an empty gap for the insertion
                if (to != from) {
                    receiptCards.remove(from);
                    receiptCards.add(to, data);
                    receiptKeyboardSelectedIndex = to;
                    relayoutReceiptRow();
                }
            }

            @Override
            public void dragStop(InputEvent e, float x, float y, int ptr) {
                if ((cookingCustomerId != null && data.customerId.equals(cookingCustomerId)) || data.cooked) return;

                currentlyDraggingCard = null;
                receiptIsGrabbed = false;
                receiptKeyboardSelectedIndex = -1;

                // 4. Finalize the insertion into the Array
                relayoutReceiptRow();
                updateCardColors();
            }
        });
    }
}
