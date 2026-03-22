package com.sushi.game.factory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.sushi.game.SushiGame;
import com.sushi.game.model.SeatData;
import com.sushi.game.model.TableData;

import java.util.ArrayList;
import java.util.List;

public class TableManager {
    private final List<TableData> tables = new ArrayList<>();

    public void loadTables(MapObjects objects) {
        tables.clear();
        for (MapObject obj : objects) {
            if ("table".equals(obj.getName())) {

                int tableId = obj.getProperties().get("tableId", 0, Integer.class);
                int seats = obj.getProperties().get("seats", 2, Integer.class);

                // get table coords
                float x = obj.getProperties().get("x", 0f, Float.class);
                float y = obj.getProperties().get("y", 0f, Float.class);


                Gdx.app.log("TABLE", "id=" + tableId + " seats=" + seats + " x=" + x + " y=" + y); // temp log

                //convert pixels to world units
                Vector2 position = new Vector2(x, y).scl(SushiGame.UNIT_SCALE);
                tables.add(new TableData(tableId, position, seats));
            }
        }
        for (MapObject obj : objects) {
            if ("seat".equals(obj.getName())) {
                int tableId = obj.getProperties().get("tableId", 0, Integer.class);
                int seatIndex = obj.getProperties().get("seatIndex", 2, Integer.class);

                // get seat coords
                float x = obj.getProperties().get("x", 0f, Float.class);
                float y = obj.getProperties().get("y", 0f, Float.class);
                Gdx.app.log("SEAT", "loading seat tableId=" + tableId + " seatIndex=" + seatIndex + " x=" + x + " y=" + y);
                //seat position + new seatData object
                Vector2 position = new Vector2(x, y).scl(SushiGame.UNIT_SCALE);
                SeatData seatData = new SeatData(tableId, seatIndex, position);

                //find matching table and add seat
                for (TableData table : tables) {
                    Gdx.app.log("SEAT", "checking table.tableId=" + table.tableId);
                    if (table.tableId == tableId) {
                        table.seats.add(seatData);
                        Gdx.app.log("SEAT", "added seat to table " + tableId);
                        break;
                    }
                }
            }

        }
    }


    // return free table
    public TableData claimFreeTable() {
        for (TableData table : tables) {
            if (table.hasFreeSeats() && !table.seats.isEmpty()) {
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


