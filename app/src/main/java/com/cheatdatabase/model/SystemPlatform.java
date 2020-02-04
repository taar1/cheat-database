package com.cheatdatabase.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

public class SystemPlatform implements Parcelable {

    @SerializedName("systemId")
    private int systemId;
    @SerializedName("gameCount")
    private int gameCount;
    @SerializedName("cheatCount")
    private int cheatCount;
    @SerializedName("systemName")
    private String systemName;
    @SerializedName("dateAdded")
    private Date dateLocallyAdded;
    @SerializedName("games")
    private List<Game> games;
    @SerializedName("lastMod")
    private Date lastModTimeStamp;

    public SystemPlatform(int systemId, String systemName, Date dateLocallyAdded) {
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

    @Inject
    public SystemPlatform() {
    }

    protected SystemPlatform(Parcel in) {
        systemId = in.readInt();
        gameCount = in.readInt();
        cheatCount = in.readInt();
        systemName = in.readString();
        dateLocallyAdded = (java.util.Date) in.readSerializable();
        games = in.createTypedArrayList(Game.CREATOR);
        lastModTimeStamp = (java.util.Date) in.readSerializable();
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

    public boolean addGame(Game game) {
        try {
            games.add(game);
            return true;
        } catch (Exception e) {
            Log.e("SystemPlatform", "SystemPlatform:addGame(): " + e.getMessage());
            return false;
        }
    }

    public Date getDateLocallyAdded() {
        return dateLocallyAdded;
    }

    public void setDateLocallyAdded(Date dateLocallyAdded) {
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
        return lastModTimeStamp.getTime();
    }

    public void setLastModTimeStamp(Date lastModTimeStamp) {
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
        dest.writeSerializable(dateLocallyAdded);
        dest.writeTypedList(games);
        dest.writeSerializable(lastModTimeStamp);
    }
}
