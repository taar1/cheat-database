package com.cheatdatabase.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class Game implements Parcelable {

    @SerializedName("systemId")
    private int systemId;
    @SerializedName("systemName")
    private String systemName;
    @SerializedName("cheats")
    private ArrayList<Cheat> cheatList;
    @SerializedName("gameId")
    private int gameId;
    @SerializedName("cheatCount")
    private int cheatsCount;
    @SerializedName("gameName")
    private String gameName;

    @Inject
    public Game() {
        cheatList = new ArrayList<>();
    }

    // TODO FIXME remove systemId and systemName and replace it with SystemPlatform object....
    // TODO FIXME remove systemId and systemName and replace it with SystemPlatform object....
    // TODO FIXME remove systemId and systemName and replace it with SystemPlatform object....
    // TODO FIXME remove systemId and systemName and replace it with SystemPlatform object....

    public Game(int gameId, String gameName, int systemId, String systemName) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.systemId = systemId;
        this.systemName = systemName;
    }

    protected Game(Parcel in) {
        // Attention: The order of writing and reading the parcel MUST match.
        cheatList = in.createTypedArrayList(Cheat.CREATOR);
        systemId = in.readInt();
        systemName = in.readString();
        gameId = in.readInt();
        cheatsCount = in.readInt();
        gameName = in.readString();

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Attention: The order of writing and reading the parcel MUST match.
        dest.writeTypedList(cheatList);
        dest.writeInt(systemId);
        dest.writeString(systemName);
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
        return gameName.replaceAll("\\\\", "");
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

    public void setCheatList(ArrayList<Cheat> cheatList) {
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
