package com.cheatdatabase.businessobjects;

import android.util.Log;

import java.io.Serializable;

public class SystemPlatform implements Serializable {

    private final String TAG = SystemPlatform.class.getSimpleName();

    private int systemId;
    private int gameCount;
    private int cheatCount;
    private String systemName;
    private String dateLocallyAdded;
    private Game[] games;
    private long lastModTimeStamp;

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

    public SystemPlatform() {

    }

    public int getCheatCount() {
        return cheatCount;
    }

    public void setCheatCount(int cheatCount) {
        this.cheatCount = cheatCount;
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
            Log.e(TAG, "SystemPlatform:addGame(): " + e.getMessage());
            return false;
        }
    }

    public String getDateLocallyAdded() {
        return dateLocallyAdded;
    }

    public void setDateLocallyAdded(String dateLocallyAdded) {
        this.dateLocallyAdded = dateLocallyAdded;
    }

    public int getSystemId() {
        return systemId;
    }

    public void setSystemId(int systemId) {
        this.systemId = systemId;
    }

    public String getSystemName() {
        return systemName;
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

    public long getLastModTimeStamp() {
        return lastModTimeStamp;
    }

    public void setLastModTimeStamp(long lastModTimeStamp) {
        this.lastModTimeStamp = lastModTimeStamp;
    }
}
