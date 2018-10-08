package com.cheatdatabase.businessobjects;

import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;

public class Game extends SystemPlatform implements Serializable {

    private Cheat[] cheats;
    private int gameId, cheatsCount;
    private String gameName;

    public Game() {
    }

    public Game(int gameId, String gameName, int systemId, String systemName) {
        super(systemId, systemName);
        this.gameId = gameId;
        this.gameName = gameName;
    }

    /**
     * Legt die Anzahl Cheat-Objekte fest
     *
     * @param cheatsCount
     */
    public void createCheatCollection(int cheatsCount) {
        this.cheats = new Cheat[cheatsCount];
    }

    public boolean addCheat(Cheat cheat) {
        try {
            for (int i = 0; i < cheats.length; i++) {
                if (cheats[i] == null) {
                    cheats[i] = cheat;
                }
            }
            return true;
        } catch (Exception e) {
            Log.e("Game:addCheat()", e.getMessage());
            return false;
        }
    }

    public int getCheatsCount() {
        return cheatsCount;
    }

    public Cheat[] getCheats() {
        return cheats;
    }

    public int countCheats() {
        return cheats.length;
    }

    public int getGameId() {
        return gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setCheatsCount(int cheatsCount) {
        this.cheatsCount = cheatsCount;
    }

    /**
     * Deletes the cheat collection and adds one single cheat to the game object
     *
     * @param cheat
     */
    public void setCheat(Cheat cheat) {
        this.cheats = new Cheat[1];
        cheats[0] = cheat;
    }

    public void setCheats(Cheat[] cheats) {
        this.cheats = cheats;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

}
