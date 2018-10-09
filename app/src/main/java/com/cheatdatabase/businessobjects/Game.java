package com.cheatdatabase.businessobjects;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Game extends SystemPlatform implements Serializable {

    private List<Cheat> cheatList;
    private int gameId, cheatsCount;
    private String gameName;

    public Game() {
        cheatList = new ArrayList<>();
    }

    public Game(int gameId, String gameName, int systemId, String systemName) {
        super(systemId, systemName);
        this.gameId = gameId;
        this.gameName = gameName;
    }

//    /**
//     * Legt die Anzahl Cheat-Objekte fest
//     *
//     * @param cheatsCount
//     */
//    public void createCheatCollection(int cheatsCount) {
//        this.cheatList = new Cheat[cheatsCount];
//    }

    public boolean addCheat(Cheat cheat) {
        try {
            cheatList.add(cheat);
            return true;
        } catch (Exception e) {
            Log.e("Game:addCheat()", e.getMessage());
            return false;
        }
    }

    public int getCheatsCount() {
        return cheatsCount;
    }

    public List<Cheat> getCheatList() {
        return cheatList;
    }

    public int countCheats() {
        return cheatList.size();
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
        cheatList = new ArrayList<>();
        cheatList.add(cheat);
    }

    public void setCheatList(List<Cheat> cheatList) {
        this.cheatList = cheatList;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

}
