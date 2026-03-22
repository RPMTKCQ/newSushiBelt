package com.sushi.game.factory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.math.Vector2;
import com.sushi.game.SushiGame;
import com.sushi.game.model.TableData;

import java.util.ArrayList;
import java.util.List;

public class TableManager {
    private final List<TableData> tables = new ArrayList<>();


    public void loadTables(MapObjects objects) {
        tables.clear();
        for (MapObject obj : objects) {
            if (!"table".equals(obj.getName())) continue;

            int tableId = obj.getProperties().get("tableId", 0, Integer.class);
            int seats = obj.getProperties().get("seats", 2, Integer.class);


            float x = obj.getProperties().get("x", 0f, Float.class);
            float y = obj.getProperties().get("y", 0f, Float.class);


            Gdx.app.log("TABLE", "id=" + tableId + " seats=" + seats + " x=" + x + " y=" + y); // temp log

            //convert pixels to world units
            Vector2 position = new Vector2(x, y).scl(SushiGame.UNIT_SCALE);
            tables.add(new TableData(tableId, position, seats));
        }
    }

    // return free table
    public TableData claimFreeTable() {
        for (TableData table : tables) {
            if (table.hasFreeSeats()) {
                table.occupy();
                return table;
            }
        }
        return null; // restaurant is full
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
}


