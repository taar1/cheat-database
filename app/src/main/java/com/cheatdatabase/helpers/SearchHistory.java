package com.cheatdatabase.helpers;

public class SearchHistory {

    public static final String TABLE_NAME = "searchhistory";

    public static final String KEY_ROWID = "_id";
    public static final String KEY_SEARCHHISTORY_QUERY = "searchquery";
    public static final String KEY_SEARCHHISTORY_SEARCHTIME = "searchtime";


    private int id;
    private String searchQuery;
    private int searchTime;

    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + KEY_ROWID + " INTEGER primary key autoincrement,"
                    + KEY_SEARCHHISTORY_QUERY + " TEXT,"
                    + KEY_SEARCHHISTORY_SEARCHTIME + " TEXT"
                    + ")";


    public SearchHistory() {
    }

    public SearchHistory(int id, String searchQuery, int searchTime) {
        this.id = id;
        this.searchQuery = searchQuery;
        this.searchTime = searchTime;
    }
}
