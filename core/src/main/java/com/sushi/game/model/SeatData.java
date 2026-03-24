package com.sushi.game.model;

import com.badlogic.gdx.math.Vector2;

public class SeatData {
    public final int tableId;
    public final int seatIndex;
    public final Vector2 position;
    public boolean isOccupied = false;

    public SeatData(int tableId, int seatIndex, Vector2 position) {
        this.tableId = tableId;
        this.seatIndex = seatIndex;
        this.position = position;
    }


}
