package com.cheatdatabase.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.cheatdatabase.model.SystemPlatform;

import java.util.Date;

@Entity(tableName = "systems")
public class SystemModel {

    @PrimaryKey
    @ColumnInfo(name = "_id")
    public int id;
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "gamecount")
    public int gamesCount;
    @ColumnInfo(name = "cheatcount")
    public int cheatCount;
    @ColumnInfo(name = "lastmod")
    public String lastmod;


    public SystemModel() {
    }

    public SystemModel(int id, String name, int gamesCount, int cheatCount, String lastmod) {
        this.id = id;
        this.name = name;
        this.gamesCount = gamesCount;
        this.cheatCount = cheatCount;
        this.lastmod = lastmod;
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

    public SystemPlatform toSystemPlatform() {
        return new SystemPlatform(getId(), getName(), new Date(getLastmod()));
    }
}



