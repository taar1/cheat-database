package com.cheatdatabase.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import javax.inject.Inject;

public class SystemPlatform implements Parcelable {

    private int systemId;
    private int gameCount;
    private int cheatCount;
    private String systemName;
    private String dateLocallyAdded;
    private Game[] games;
    private long lastModTimeStamp;

    @Inject
    public SystemPlatform(int systemId, String systemName, String dateLocallyAdded) {
        super();
        this.systemId = systemId;
        this.systemName = systemName;
        this.dateLocallyAdded = dateLocallyAdded;
    }

    @Inject
    public SystemPlatform(int systemId, String systemName) {
        super();
        this.systemId = systemId;
        this.systemName = systemName;
    }

    @Inject
    public SystemPlatform() {

    }

    @Inject
    protected SystemPlatform(Parcel in) {
        systemId = in.readInt();
        gameCount = in.readInt();
        cheatCount = in.readInt();
        systemName = in.readString();
        dateLocallyAdded = in.readString();
        games = in.createTypedArray(Game.CREATOR);
        lastModTimeStamp = in.readLong();
    }

    public static final Creator<SystemPlatform> CREATOR = new Creator<SystemPlatform>() {
        @Override
        public SystemPlatform createFromParcel(Parcel in) {
            return new SystemPlatform(in);
        }

        @Override
        public SystemPlatform[] newArray(int size) {
            return new SystemPlatform[size];
        }
    };

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
            Log.e("SystemPlatform", "SystemPlatform:addGame(): " + e.getMessage());
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(systemId);
        dest.writeInt(gameCount);
        dest.writeInt(cheatCount);
        dest.writeString(systemName);
        dest.writeString(dateLocallyAdded);
        dest.writeTypedArray(games, flags);
        dest.writeLong(lastModTimeStamp);
    }
}
