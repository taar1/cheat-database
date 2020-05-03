package com.cheatdatabase.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

@Entity(tableName = "systems")
public class SystemModel implements Parcelable {

    @PrimaryKey
    @ColumnInfo(name = "_id")
    public int id;
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "gamecount")
    @SerializedName("gameCount")
    public int gamesCount;
    @ColumnInfo(name = "cheatcount")
    @SerializedName("cheatCount")
    public int cheatCount;
    @ColumnInfo(name = "lastmod")
    public String lastmod;


    @SerializedName("systemId")
    @Ignore
    private int systemId;
    @SerializedName("systemName")
    @Ignore
    private String systemName;
    @SerializedName("dateAdded")
    @Ignore
    private Date dateLocallyAdded;
    @SerializedName("games")
    @Ignore
    private List<Game> games;
    @SerializedName("lastMod")
    @Ignore
    private Date lastModTimeStamp;


    @Inject
    @Ignore
    public SystemModel() {
    }

    public SystemModel(int systemId, String systemName, Date dateLocallyAdded) {
        this.systemId = systemId;
        this.systemName = systemName;
        this.dateLocallyAdded = dateLocallyAdded;
    }

    public SystemModel(int systemId, String systemName) {
        this.systemId = systemId;
        this.systemName = systemName;
    }

    public SystemModel(int id, String name, int gamesCount, int cheatCount, String lastmod) {
        this.id = id;
        this.name = name;
        this.gamesCount = gamesCount;
        this.cheatCount = cheatCount;
        this.lastmod = lastmod;
    }

    protected SystemModel(Parcel in) {
        systemId = in.readInt();
        gamesCount = in.readInt();
        cheatCount = in.readInt();
        systemName = in.readString();
        dateLocallyAdded = (java.util.Date) in.readSerializable();
        games = in.createTypedArrayList(Game.CREATOR);
        lastModTimeStamp = (java.util.Date) in.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(systemId);
        dest.writeInt(gamesCount);
        dest.writeInt(cheatCount);
        dest.writeString(systemName);
        dest.writeSerializable(dateLocallyAdded);
        dest.writeTypedList(games);
        dest.writeSerializable(lastModTimeStamp);
    }

    public static final Creator<SystemModel> CREATOR = new Creator<SystemModel>() {
        @Override
        public SystemModel createFromParcel(Parcel in) {
            return new SystemModel(in);
        }

        @Override
        public SystemModel[] newArray(int size) {
            return new SystemModel[size];
        }
    };

    public void setCheatCount(int cheatCount) {
        this.cheatCount = cheatCount;
    }

    public boolean addGame(Game game) {
        try {
            games.add(game);
            return true;
        } catch (Exception e) {
            Log.e("SystemModel", "SystemModel:addGame(): " + e.getMessage());
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


    public long getLastModTimeStamp() {
        return lastModTimeStamp.getTime();
    }

    public void setLastModTimeStamp(Date lastModTimeStamp) {
        this.lastModTimeStamp = lastModTimeStamp;
    }

    public SystemModel toSystemModel() {
        return new SystemModel(getSystemId(), getSystemName(), getGamesCount(), getCheatCount(), String.valueOf(getLastModTimeStamp()));
    }


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getGamesCount() {
        return gamesCount;
    }

    public int getCheatCount() {
        return cheatCount;
    }

    public String getLastmod() {
        return lastmod;
    }

    public SystemModel toSystemPlatform() {
        return new SystemModel(getId(), getName(), new Date(getLastmod()));
    }
}



