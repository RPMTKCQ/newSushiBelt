package com.sushi.game.model;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class TableData {
    public final int tableId;
    public final Vector2 position;
    public final int totalSeats;
    public int occupiedSeats = 0;
    public final List<SeatData> seats = new ArrayList<>();

    public TableData(int tableId, Vector2 position, int totalSeats) {
        this.tableId = tableId;
        this.position = position;
        this.totalSeats = totalSeats;
    }

    public boolean hasFreeSeats() {
        if (seats.isEmpty()) return occupiedSeats < totalSeats; // fallback if no seats loaded
        for (SeatData seat : seats) {
            if (!seat.isOccupied) return true;
        }
        return false;
    }

    public void occupy() {
        occupiedSeats++;
    }

    public void vacate() {
        if (occupiedSeats > 0) occupiedSeats--;
    }
}
