package com.cheatdatabase.helpers;

public class GameSystemTable {


    // Systems
    private static final String DATABASE_TABLE_SYSTEMS = "systems";
    private static final String SYS_SYSTEM_ID = "_id";
    private static final String SYS_SYSTEM_NAME = "name";
    private static final String SYS_SYSTEM_GAMECOUNT = "gamecount";
    private static final String SYS_SYSTEM_CHEATCOUNT = "cheatcount";
    private static final String SYS_SYSTEM_LASTMOD = "lastmod";


    private static final String DATABASE_CREATE_SYSTEMS = "create table if not exists " + DATABASE_TABLE_SYSTEMS + " (" + SYS_SYSTEM_ID + " integer primary key, " + SYS_SYSTEM_NAME + " text not null, " + SYS_SYSTEM_GAMECOUNT + " integer not null, " + SYS_SYSTEM_CHEATCOUNT + " integer not null, " + SYS_SYSTEM_LASTMOD + " text not null);";



}
