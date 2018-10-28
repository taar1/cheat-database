package com.cheatdatabase.helpers;

public class Favorite {

    public static final String TABLE_NAME = "favorites";

    public static final String FAV_GAME_ID = "game_id";
    public static final String FAV_GAMENAME = "game_name";
    public static final String FAV_CHEAT_ID = "cheat_id";
    public static final String FAV_CHEAT_TITLE = "cheat_title";
    public static final String FAV_CHEAT_TEXT = "cheat_text";
    public static final String FAV_SYSTEM_ID = "system_id";
    public static final String FAV_SYSTEM_NAME = "system_name";
    public static final String FAV_LANGUAGE_ID = "language_id";
    public static final String FAV_GAME_COUNT = "game_count";
    public static final String FAV_WALKTHROUGH_FORMAT = "walkthrough_format";
    // Local member who saved the favorite (for syncing online)
    public static final String FAV_MEMBER_ID = "member_id";

    private int id;
    private String gameName;
    private int cheatId;
    private String cheatTitle;
    private String cheatText;
    private int systemId;
    private String systemName;
    private int languageId;
    private int gameCount;
    private int walkthroughFormat;

    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + FAV_GAME_ID + " INTEGER,"
                    + FAV_GAMENAME + " TEXT,"
                    + FAV_CHEAT_ID + " INTEGER not null unique,"
                    + FAV_CHEAT_TITLE + " TEXT,"
                    + FAV_CHEAT_TEXT + " TEXT,"
                    + FAV_SYSTEM_ID + " INTEGER,"
                    + FAV_SYSTEM_NAME + " TEXT,"
                    + FAV_LANGUAGE_ID + " INTEGER,"
                    + FAV_GAME_COUNT + " INTEGER,"
                    + FAV_WALKTHROUGH_FORMAT + " INTEGER,"
                    + FAV_MEMBER_ID + " INTEGER"
                    + ")";


    public Favorite() {
    }

    public Favorite(int id, String gameName, int cheatId, String cheatTitle, String cheatText, int systemId, String systemName, int languageId, int gameCount, int walkthroughFormat) {
        this.id = id;
        this.gameName = gameName;
        this.cheatId = cheatId;
        this.cheatTitle = cheatTitle;
        this.cheatText = cheatText;
        this.systemId = systemId;
        this.systemName = systemName;
        this.languageId = languageId;
        this.gameCount = gameCount;
        this.walkthroughFormat = walkthroughFormat;
    }
}
