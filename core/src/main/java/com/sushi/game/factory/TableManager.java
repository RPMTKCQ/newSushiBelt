package com.sushi.game.factory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.sushi.game.SushiGame;
import com.sushi.game.model.SeatData;
import com.sushi.game.model.TableData;

import java.util.ArrayList;
import java.util.List;

public class TableManager {
    private final List<TableData> tables = new ArrayList<>();

    public void loadTables(MapObjects objects) {
        tables.clear();

        // PASS 1: Load all the Tables first!
        // This prevents the copy-paste bug where Tiled loads a seat before its table exists.
        for (MapObject obj : objects) {
            if ("table".equals(obj.getName())) {
                int tableId = obj.getProperties().get("tableId", 0, Integer.class);
                int seats = obj.getProperties().get("seats", 2, Integer.class);

                float x = obj.getProperties().get("x", 0f, Float.class);
                float y = obj.getProperties().get("y", 0f, Float.class);

                Gdx.app.log("TABLE", "id=" + tableId + " seats=" + seats + " x=" + x + " y=" + y);

                Vector2 position = new Vector2(x, y).scl(SushiGame.UNIT_SCALE);
                TableData tableData = new TableData(tableId, position, seats);
                tables.add(tableData);
            }
        }

        // PASS 2: Load all the Seats and attach them to the tables
        for (MapObject obj : objects) {
            if ("seat".equals(obj.getName())) {
                int tableId = obj.getProperties().get("tableId", 0, Integer.class);
                int seatIndex = obj.getProperties().get("seatIndex", 0, Integer.class);
                float x = obj.getProperties().get("x", 0f, Float.class);
                float y = obj.getProperties().get("y", 0f, Float.class);

                Gdx.app.log("SEAT", "loading seat tableId=" + tableId + " seatIndex=" + seatIndex + " x=" + x + " y=" + y);

                Vector2 position = new Vector2(x, y).scl(SushiGame.UNIT_SCALE);
                SeatData seatData = new SeatData(tableId, seatIndex, position);

                for (TableData table : tables) {
                    if (table.tableId == tableId) {
                        table.seats.add(seatData);
                        Gdx.app.log("SEAT", "added seat to table " + tableId);
                        break;
                    }
                }
            }
        }
    }

    public TableData claimFreeTable() {
        // Gather all free tables into a list
        List<TableData> freeTables = new ArrayList<>();
        for (TableData table : tables) {
            if (table.hasFreeSeats() && !table.seats.isEmpty()) {
                freeTables.add(table);
            }
        }

        // If no tables are available, return null
        if (freeTables.isEmpty()) {
            return null;
        }

        // Pick a random table from the available options!
        TableData randomTable = freeTables.get(MathUtils.random(freeTables.size() - 1));
        randomTable.occupy();
        return randomTable;
    }

    public void vacateTable(int tableId) {
        for (TableData table : tables) {
            if (table.tableId == tableId) {
                table.vacate();
                return;
            }
        }
    }

    public boolean hasFreeTables() {
        for (TableData table : tables) {
            if (table.hasFreeSeats()) return true;
        }
        return false;
    }

    public int getTableCount() {
        return tables.size();
    }

    public int getTotalCapacity() {
        int capacity = 0;
        for (TableData table : tables) {
            capacity += table.totalSeats;
        }
        return capacity;
    }
}
