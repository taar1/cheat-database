package com.cheatdatabase.helpers;

public class GameSystemTable {

    // Systems
    public static final String TABLE_NAME = "systems";

    public static final String SYS_SYSTEM_ID = "_id";
    public static final String SYS_SYSTEM_NAME = "name";
    public static final String SYS_SYSTEM_GAMECOUNT = "gamecount";
    public static final String SYS_SYSTEM_CHEATCOUNT = "cheatcount";
    public static final String SYS_SYSTEM_LASTMOD = "lastmod";

    public int id;
    public String name;
    public int gamesCount;
    public String cheatCount;
    public String lastmod;

    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + SYS_SYSTEM_ID + " INTEGER primary key,"
                    + SYS_SYSTEM_NAME + " TEXT,"
                    + SYS_SYSTEM_GAMECOUNT + " INTEGER,"
                    + SYS_SYSTEM_CHEATCOUNT + " INTEGER,"
                    + SYS_SYSTEM_LASTMOD + " TEXT"
                    + ")";


    public GameSystemTable() {
    }

    public GameSystemTable(int id, String name, int gamesCount, String cheatCount, String lastmod) {
        this.id = id;
        this.name = name;
        this.gamesCount = gamesCount;
        this.cheatCount = cheatCount;
        this.lastmod = lastmod;
    }
}
