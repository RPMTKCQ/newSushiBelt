package com.sushi.game.model;

import com.badlogic.gdx.math.Vector2;

public class TableData {
    public final int tableId;
    public final Vector2 position;
    public final int totalSeats;
    public int occupiedSeats = 0;

    public TableData(int tableId, Vector2 position, int totalSeats) {
        this.tableId = tableId;
        this.position = position;
        this.totalSeats = totalSeats;
    }

    public boolean hasFreeSeats() {
        return occupiedSeats < totalSeats;
    }

    public void occupy() {
        occupiedSeats++;
    }

    public void vacate() {
        if (occupiedSeats > 0) occupiedSeats--;
    }
}
