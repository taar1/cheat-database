package com.cheatdatabase.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "searchhistory")
public class SearchHistoryModel {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    public int id;
    @ColumnInfo(name = "searchquery")
    public String searchQuery;
    @ColumnInfo(name = "searchtime")
    public String searchTime;

    public SearchHistoryModel(int id, String searchQuery, String searchTime) {
        this.id = id;
        this.searchQuery = searchQuery;
        this.searchTime = searchTime;
    }


    public int getId() {
        return id;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public String getSearchTime() {
        return searchTime;
    }
}