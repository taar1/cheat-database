package com.cheatdatabase.businessobjects;

import android.util.Log;

import java.io.Serializable;

public class SystemPlatform implements Serializable {

    private int systemId, gameCount, cheatCount;
    private String systemName, dateLocallyAdded;
    private Game[] games;

    public SystemPlatform(int systemId, String systemName, String dateLocallyAdded) {
        super();
        this.systemId = systemId;
        this.systemName = systemName;
        this.dateLocallyAdded = dateLocallyAdded;
    }

    public SystemPlatform(int systemId, String systemName) {
        super();
        this.systemId = systemId;
        this.systemName = systemName;
    }

    public int getCheatCount() {
        return cheatCount;
    }

    public void setCheatCount(int cheatCount) {
        this.cheatCount = cheatCount;
    }

    public SystemPlatform() {

    }

    /**
     * Legt die Anzahl Game-Objekte fest
     *
     * @param gamesCount
     */
    public void createGameCollection(int gamesCount) {
        this.games = new Game[gamesCount];
    }

    public boolean addGame(Game game) {
        try {
            for (int i = 0; i < games.length; i++) {
                if (games[i] == null) {
                    games[i] = game;
                }
            }
            return true;
        } catch (Exception e) {
            Log.e("SystemPlatform:addGame()", e.getMessage());
            return false;
        }
    }

    public String getDateLocallyAdded() {
        return dateLocallyAdded;
    }

    public int getSystemId() {
        return systemId;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setDateLocallyAdded(String dateLocallyAdded) {
        this.dateLocallyAdded = dateLocallyAdded;
    }

    public void setSystemId(int systemId) {
        this.systemId = systemId;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public int getGameCount() {
        return gameCount;
    }

    public void setGameCount(int gameCount) {
        this.gameCount = gameCount;
    }

}
