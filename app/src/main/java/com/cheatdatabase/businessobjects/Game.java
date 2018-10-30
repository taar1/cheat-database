package com.cheatdatabase.businessobjects;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Game implements Parcelable {

    private int systemId;
    private String systemName;
    private List<Cheat> cheatList;
    private int gameId;
    private int cheatsCount;
    private String gameName;

    public Game() {
        cheatList = new ArrayList<>();
    }

    public Game(int gameId, String gameName, int systemId, String systemName) {
//        super(systemId, systemName);
        this.gameId = gameId;
        this.gameName = gameName;
        this.systemId = systemId;
        this.systemName = systemName;
    }

    protected Game(Parcel in) {
        systemId = in.readInt();
        systemName = in.readString();
        cheatList = in.createTypedArrayList(Cheat.CREATOR);
        gameId = in.readInt();
        cheatsCount = in.readInt();
        gameName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(systemId);
        dest.writeString(systemName);
        dest.writeTypedList(cheatList);
        dest.writeInt(gameId);
        dest.writeInt(cheatsCount);
        dest.writeString(gameName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Game> CREATOR = new Creator<Game>() {
        @Override
        public Game createFromParcel(Parcel in) {
            return new Game(in);
        }

        @Override
        public Game[] newArray(int size) {
            return new Game[size];
        }
    };

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

    public int getSystemId() {
        return systemId;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemId(int systemId) {
        this.systemId = systemId;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }
}
