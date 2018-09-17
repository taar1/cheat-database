package com.cheatdatabase.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.Screenshot;
import com.cheatdatabase.businessobjects.SystemPlatform;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Database Adapter More Info:
 * http://www.anddev.org/png_image_--und-gt_sqlite_db_
 * --und-gt_imageview-t7188.html Copyright (c) 2010, 2011<br>
 *
 * @version 1.0
 */
@EBean
public class CheatDatabaseAdapter {

    // Member
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_ROWID = "_id";
    public static final String KEY_ONLINEMEMBERID = "mid";

    // Favorites
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

    // Search History
    public static final String KEY_SEARCHHISTORY_QUERY = "searchquery";
    public static final String KEY_SEARCHHISTORY_SEARCHTIME = "searchtime";

    // Systems
    private static final String DATABASE_TABLE_SYSTEMS = "systems";
    private static final String SYS_SYSTEM_ID = "_id";
    private static final String SYS_SYSTEM_NAME = "name";
    private static final String SYS_SYSTEM_GAMECOUNT = "gamecount";
    private static final String SYS_SYSTEM_CHEATCOUNT = "cheatcount";
    private static final String SYS_SYSTEM_LASTMOD = "lastmod";

    private static final String DATABASE_NAME = "data";
    // private static final String DATABASE_TABLE_MEMBERS = "members";
    private static final String DATABASE_TABLE_FAVORITES = "favorites";
    private static final String DATABASE_TABLE_SEARCHHISTORY = "searchhistory";

    private static final int DATABASE_VERSION = 3; // From 30.06.2015

    private static final String TAG = "CheatDbAdapter";
    /**
     * Database creation sql statement
     */
    // private static final String DATABASE_CREATE_MEMBER = "create table " +
    // DATABASE_TABLE_MEMBERS + " (" + KEY_ROWID +
    // " integer primary key autoincrement, " + KEY_USERNAME +
    // " text not null, " + KEY_PASSWORD + " text not null, " + KEY_EMAIL +
    // " text not null, " + KEY_ONLINEMEMBERID + " integer);";

    private static final String DATABASE_CREATE_FAVORITES = "create table if not exists " + DATABASE_TABLE_FAVORITES + " (" + FAV_GAME_ID + " integer not null, " + FAV_GAMENAME + " text not null, " + FAV_CHEAT_ID + " integer not null unique, " + FAV_CHEAT_TITLE + " text not null, " + FAV_CHEAT_TEXT
            + " integer not null, " + FAV_SYSTEM_ID + " integer not null, " + FAV_SYSTEM_NAME + " text not null, " + FAV_LANGUAGE_ID + " integer, " + FAV_WALKTHROUGH_FORMAT + " integer, " + FAV_MEMBER_ID + " integer);";
    private static final String DATABASE_CREATE_SEARCHHISTORY = "create table " + DATABASE_TABLE_SEARCHHISTORY + " (" + KEY_ROWID + " integer primary key autoincrement, " + KEY_SEARCHHISTORY_QUERY + " text not null, " + KEY_SEARCHHISTORY_SEARCHTIME + " text not null);";
    // New Table from 30.06.2015
    private static final String DATABASE_CREATE_SYSTEMS = "create table if not exists " + DATABASE_TABLE_SYSTEMS + " (" + SYS_SYSTEM_ID + " integer primary key, " + SYS_SYSTEM_NAME + " text not null, " + SYS_SYSTEM_GAMECOUNT + " integer not null, " + SYS_SYSTEM_CHEATCOUNT + " integer not null, " + SYS_SYSTEM_LASTMOD + " text not null);";
    private final Context mCtx;
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public CheatDatabaseAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     * initialization call)
     * @throws android.database.SQLException if the database could be neither opened or created
     */
    public CheatDatabaseAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    /**
     * Insert one cheat to the favorites.
     *
     * @param cheat
     * @return
     */
    public int insertCheat(Cheat cheat) {
        if (cheat == null) {
            return 0;
        } else {
            if (cheat.isScreenshots()) {
                saveScreenshotsToSdCard(cheat);
            }

            ContentValues initialValues = new ContentValues();
            initialValues.put(FAV_GAME_ID, cheat.getGameId());
            initialValues.put(FAV_GAMENAME, cheat.getGameName());
            initialValues.put(FAV_CHEAT_ID, cheat.getCheatId());
            initialValues.put(FAV_CHEAT_TITLE, cheat.getCheatTitle());
            initialValues.put(FAV_CHEAT_TEXT, cheat.getCheatText());
            initialValues.put(FAV_LANGUAGE_ID, cheat.getLanguageId());
            initialValues.put(FAV_SYSTEM_ID, cheat.getSystemId());
            initialValues.put(FAV_SYSTEM_NAME, cheat.getSystemName());
            int walkthroughFormat = 0;
            if (cheat.isWalkthroughFormat()) {
                walkthroughFormat = 1;
            }
            initialValues.put(FAV_WALKTHROUGH_FORMAT, walkthroughFormat);

            long retVal = mDb.insert(DATABASE_TABLE_FAVORITES, null, initialValues);
            return Integer.parseInt(String.valueOf(retVal));
        }
    }

    /**
     * Insert a list of cheats to the favorites.
     *
     * @param cheats []
     * @return
     */
    public int insertCheats(Cheat[] cheats) {
        int retValInt = 0;
        long retVal = 0;

        for (int i = 0; i < cheats.length; i++) {
            Cheat cheat = cheats[i];

            if (cheat.isScreenshots()) {
                saveScreenshotsToSdCard(cheat);
            }

            ContentValues initialValues = new ContentValues();
            initialValues.put(FAV_GAME_ID, cheat.getGameId());
            initialValues.put(FAV_GAMENAME, cheat.getGameName());
            initialValues.put(FAV_CHEAT_ID, cheat.getCheatId());
            initialValues.put(FAV_CHEAT_TITLE, cheat.getCheatTitle());
            initialValues.put(FAV_CHEAT_TEXT, cheat.getCheatText());
            initialValues.put(FAV_LANGUAGE_ID, cheat.getLanguageId());
            initialValues.put(FAV_SYSTEM_ID, cheat.getSystemId());
            initialValues.put(FAV_SYSTEM_NAME, cheat.getSystemName());
            initialValues.put(FAV_MEMBER_ID, cheat.getSubmittingMember().getMid());
            int walkthroughFormat = 0;
            if (cheat.isWalkthroughFormat()) {
                walkthroughFormat = 1;
            }
            initialValues.put(FAV_WALKTHROUGH_FORMAT, walkthroughFormat);

            retVal = mDb.insert(DATABASE_TABLE_FAVORITES, null, initialValues);
            retValInt = Integer.parseInt(String.valueOf(retVal));
        }

        return retValInt;
    }

    public void saveScreenshotsToSdCard(Cheat cheat) {
        Screenshot[] screens = cheat.getScreens();
        for (int j = 0; j < screens.length; j++) {
            Screenshot tmpScr = screens[j];

            if (Tools.isSdWriteable()) {
                tmpScr.saveToSd();
            }
        }
    }

    /**
     * Add all cheats of a game to the favorites.
     *
     * @param game
     * @return
     */
    public int insertCheats(Game game) {

        int int_anzInserts = 0;

        Cheat[] cheats = Webservice.getCheatsByGameId(game.getGameId(), game.getGameName());

        for (int i = 0; i < cheats.length; i++) {
            Cheat cheat = cheats[i];

            if (cheat.isScreenshots()) {
                saveScreenshotsToSdCard(cheat);
            }

            ContentValues initialValues = new ContentValues();
            initialValues.put(FAV_GAME_ID, cheat.getGameId());
            initialValues.put(FAV_GAMENAME, cheat.getGameName());
            initialValues.put(FAV_CHEAT_ID, cheat.getCheatId());
            initialValues.put(FAV_CHEAT_TITLE, cheat.getCheatTitle());
            initialValues.put(FAV_CHEAT_TEXT, cheat.getCheatText());
            initialValues.put(FAV_LANGUAGE_ID, cheat.getLanguageId());
            initialValues.put(FAV_SYSTEM_ID, game.getSystemId());
            initialValues.put(FAV_SYSTEM_NAME, game.getSystemName());
            int walkthroughFormat = 0;
            if (cheat.isWalkthroughFormat()) {
                walkthroughFormat = 1;
            }
            initialValues.put(FAV_WALKTHROUGH_FORMAT, walkthroughFormat);

            Long l_result = mDb.insert(DATABASE_TABLE_FAVORITES, null, initialValues);
            if (l_result > -1) {
                int_anzInserts++;
            }
        }

        return int_anzInserts;
    }

    public boolean deleteCheat(Cheat cheat) {
        return mDb.delete(DATABASE_TABLE_FAVORITES, FAV_CHEAT_ID + "=" + cheat.getCheatId(), null) > 0;
    }

    public boolean deleteCheats(Game game) {
        return mDb.delete(DATABASE_TABLE_FAVORITES, FAV_GAME_ID + "=" + game.getGameId(), null) > 0;
    }

    /**
     * Get all favorites from the database.
     *
     * @return Game[]
     */
    public Game[] getAllFavoritedGames() {

        int countRows = countFavoritedGames();
        if (countRows == 0) {
            return null;
        } else {
            Cursor cur = mDb.query(DATABASE_TABLE_FAVORITES, new String[]{FAV_GAME_ID, FAV_GAMENAME, FAV_CHEAT_ID, FAV_CHEAT_TITLE, FAV_CHEAT_TEXT, FAV_SYSTEM_ID, FAV_SYSTEM_NAME, FAV_LANGUAGE_ID, FAV_WALKTHROUGH_FORMAT}, null, null, FAV_GAME_ID, null, FAV_GAMENAME);

            Game[] favGames = new Game[countRows];

            if (cur.moveToFirst()) {
                if (cur.isFirst()) {
                    int i = 0;
                    do {
                        int gameId = cur.getInt(cur.getColumnIndex(FAV_GAME_ID));
                        String gameName = cur.getString(cur.getColumnIndex(FAV_GAMENAME));
                        int systemId = cur.getInt(cur.getColumnIndex(FAV_SYSTEM_ID));
                        String systemName = cur.getString(cur.getColumnIndex(FAV_SYSTEM_NAME));
                        int numberOfFavoritedCheats = countFavoritedCheats(gameId);

                        Game tempGame = new Game(gameId, gameName, systemId, systemName);
                        tempGame.setCheatsCount(numberOfFavoritedCheats);

                        favGames[i] = tempGame;
                        i++;
                    } while (cur.moveToNext());
                }

                cur.close();
                return favGames;
            }
        }

        return null;
    }

    public ArrayList<SystemPlatform> getAllSystemsAndCount() {

        Cursor cur = mDb.query(DATABASE_TABLE_SYSTEMS, new String[]{SYS_SYSTEM_ID, SYS_SYSTEM_NAME, SYS_SYSTEM_GAMECOUNT, SYS_SYSTEM_CHEATCOUNT, SYS_SYSTEM_LASTMOD}, null, null, null, null, SYS_SYSTEM_NAME + " COLLATE NOCASE ASC;");

        ArrayList<SystemPlatform> systems = null;

        if (cur.moveToFirst()) {
            if (cur.isFirst()) {
                systems = new ArrayList<>();
                do {
                    int systemId = cur.getInt(cur.getColumnIndex(SYS_SYSTEM_ID));
                    String systemName = cur.getString(cur.getColumnIndex(SYS_SYSTEM_NAME));
                    int gameCount = cur.getInt(cur.getColumnIndex(SYS_SYSTEM_GAMECOUNT));
                    int cheatCount = cur.getInt(cur.getColumnIndex(SYS_SYSTEM_CHEATCOUNT));
                    String lastMod = cur.getString(cur.getColumnIndex(SYS_SYSTEM_LASTMOD));

                    SystemPlatform sysPla = new SystemPlatform();
                    sysPla.setSystemId(systemId);
                    sysPla.setSystemName(systemName);
                    sysPla.setGameCount(gameCount);
                    sysPla.setCheatCount(cheatCount);
                    sysPla.setLastModTimeStamp(Long.parseLong(lastMod));

                    systems.add(sysPla);

                } while (cur.moveToNext());
            }


            cur.close();
            return systems;
        }

        return null;
    }

    public int updateSystemsAndCount(ArrayList<SystemPlatform> systemsAndCount) {

        int insertCount = 0;
        deleteSystemsAndCount();

        for (SystemPlatform sysPla : systemsAndCount) {

            ContentValues initialValues = new ContentValues();
            initialValues.put(SYS_SYSTEM_ID, sysPla.getSystemId());
            initialValues.put(SYS_SYSTEM_NAME, sysPla.getSystemName());
            initialValues.put(SYS_SYSTEM_GAMECOUNT, sysPla.getGameCount());
            initialValues.put(SYS_SYSTEM_CHEATCOUNT, sysPla.getCheatCount());
            initialValues.put(SYS_SYSTEM_LASTMOD, sysPla.getLastModTimeStamp());

            Long l_result = mDb.insert(DATABASE_TABLE_SYSTEMS, null, initialValues);
            if (l_result > -1) {
                insertCount++;
            }
        }

        return insertCount;
    }

    public void deleteSystemsAndCount() {
        mDb.delete(DATABASE_TABLE_SYSTEMS, null, null);
    }

    /**
     * Get one cheat from the favorites.
     *
     * @param cheatId
     * @return Cheat
     */
    public Cheat getFavoriteCheat(int cheatId) {
        Cursor cur = mDb.query(DATABASE_TABLE_FAVORITES, new String[]{FAV_GAME_ID, FAV_GAMENAME, FAV_CHEAT_ID, FAV_CHEAT_TITLE, FAV_CHEAT_TEXT, FAV_LANGUAGE_ID, FAV_SYSTEM_ID, FAV_SYSTEM_NAME, FAV_WALKTHROUGH_FORMAT}, FAV_CHEAT_ID + "=" + cheatId, null, null, null, null);

        Cheat cheat = null;

        if (cur.moveToFirst()) {
            int gameId = cur.getInt(cur.getColumnIndex(FAV_GAME_ID));
            String gameName = cur.getString(cur.getColumnIndex(FAV_GAMENAME));
            String cheatTitle = cur.getString(cur.getColumnIndex(FAV_CHEAT_TITLE));
            String cheatText = cur.getString(cur.getColumnIndex(FAV_CHEAT_TEXT));
            int languageId = cur.getInt(cur.getColumnIndex(FAV_LANGUAGE_ID));
            int systemId = cur.getInt(cur.getColumnIndex(FAV_SYSTEM_ID));
            String systemName = cur.getString(cur.getColumnIndex(FAV_SYSTEM_NAME));
            int walkthrough = cur.getInt(cur.getColumnIndex(FAV_WALKTHROUGH_FORMAT));
            boolean walkthroughFormat = false;
            if (walkthrough == 1) {
                walkthroughFormat = true;
            }

            cheat = new Cheat(gameId, gameName, cheatId, cheatTitle, cheatText, languageId, systemId, systemName, walkthroughFormat);
            cur.close();
            return cheat;
        }
        cur.close();
        return null;
    }

    public int countFavoritedGames() {
        return countRows(DATABASE_TABLE_FAVORITES, FAV_GAME_ID, null, FAV_GAME_ID);
    }

    public int countFavoritedCheats(int gameId) {
        return countRows(DATABASE_TABLE_FAVORITES, FAV_GAME_ID, String.valueOf(gameId), FAV_CHEAT_ID);
    }

    /**
     * Count rows in one table.
     *
     * @param sqlTable
     * @param columnToCount
     * @param columnValue
     * @param countBy
     * @return
     */
    public int countRows(String sqlTable, String columnToCount, String columnValue, String countBy) {
        Cursor mCursor;
        if (columnValue == null) {
            mCursor = mDb.query(sqlTable, new String[]{columnToCount}, null, null, countBy, null, null);
        } else {
            mCursor = mDb.query(sqlTable, new String[]{columnToCount}, columnToCount + "=" + columnValue, null, countBy, null, null);
        }
        int count = mCursor.getCount();
        mCursor.close();
        return count;
    }

    /**
     * Get all favotired cheats from one game.
     *
     * @param gameId
     * @return Cheat[]
     */
    public Cheat[] getAllFavoritedCheatsByGame(int gameId) {

        int countRows = countFavoritedCheats(gameId);
        if (countRows == 0) {
            return null;
        } else {
            Cheat[] favCheats = new Cheat[countRows];

            Cursor cur = mDb.query(DATABASE_TABLE_FAVORITES, new String[]{FAV_GAME_ID, FAV_GAMENAME, FAV_CHEAT_ID, FAV_CHEAT_TITLE, FAV_CHEAT_TEXT, FAV_LANGUAGE_ID, FAV_SYSTEM_ID, FAV_SYSTEM_NAME, FAV_WALKTHROUGH_FORMAT}, FAV_GAME_ID + "=" + gameId, null, FAV_CHEAT_ID, null, FAV_CHEAT_TITLE);

            if (cur.moveToFirst()) {
                if (cur.isFirst()) {
                    int i = 0;
                    do {
                        String gameName = cur.getString(cur.getColumnIndex(FAV_GAMENAME));
                        int cheatId = cur.getInt(cur.getColumnIndex(FAV_CHEAT_ID));
                        String cheatTitle = cur.getString(cur.getColumnIndex(FAV_CHEAT_TITLE));
                        String cheatText = cur.getString(cur.getColumnIndex(FAV_CHEAT_TEXT));
                        int languageId = cur.getInt(cur.getColumnIndex(FAV_LANGUAGE_ID));
                        int systemId = cur.getInt(cur.getColumnIndex(FAV_SYSTEM_ID));
                        String systemName = cur.getString(cur.getColumnIndex(FAV_SYSTEM_NAME));
                        int walkthrough = cur.getInt(cur.getColumnIndex(FAV_WALKTHROUGH_FORMAT));
                        boolean walkthroughFormat = false;
                        if (walkthrough == 1) {
                            walkthroughFormat = true;
                        }

                        favCheats[i] = new Cheat(gameId, gameName, cheatId, cheatTitle, cheatText, languageId, systemId, systemName, walkthroughFormat);
                        i++;
                    } while (cur.moveToNext());
                }

                cur.close();
                return favCheats;
            }
        }
        return null;
    }

    /**
     * Get one cheat from the favorites.
     *
     * @param cheatId
     * @return
     * @throws android.database.SQLException
     */
    public Cursor fetchFavorite(long cheatId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE_FAVORITES, new String[]{FAV_GAME_ID, FAV_GAMENAME, FAV_CHEAT_ID, FAV_CHEAT_TITLE, FAV_CHEAT_TEXT, FAV_SYSTEM_ID, FAV_SYSTEM_NAME}, FAV_CHEAT_ID + "=" + cheatId, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    /**
     * Inserts the last search query to the search history.
     *
     * @param query
     * @return
     */
    public int insertSearchQuery(String query) {

        // http://alvinalexander.com/java/java-timestamp-example-current-time-now
        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_SEARCHHISTORY_QUERY, query.trim());
        initialValues.put(KEY_SEARCHHISTORY_SEARCHTIME, currentTimestamp.toString());

        long retVal = mDb.insert(DATABASE_TABLE_SEARCHHISTORY, null, initialValues);
        return Integer.parseInt(String.valueOf(retVal));
    }

    /**
     * Gets the latest search queries done by the user.
     *
     * @param historyLength
     * @return
     */
    public String[] getSearchHistory(int historyLength) {

        String[] searchHistory = new String[historyLength];

        Cursor cur = mDb.query(DATABASE_CREATE_SEARCHHISTORY, new String[]{KEY_ROWID, KEY_SEARCHHISTORY_QUERY, KEY_SEARCHHISTORY_SEARCHTIME}, null, null, null, null, KEY_ROWID + " DESC", historyLength + "");

        if (cur.moveToFirst()) {
            if (cur.isFirst()) {
                int i = 0;
                do {
                    String searchQuery = cur.getString(cur.getColumnIndex(KEY_SEARCHHISTORY_QUERY));
                    // String searchTime =
                    // cur.getString(cur.getColumnIndex(KEY_SEARCHHISTORY_SEARCHTIME));
                    // String rowId =
                    // cur.getString(cur.getColumnIndex(KEY_ROWID));

                    searchHistory[i] = searchQuery;
                    i++;
                } while (cur.moveToNext());
            }

            cur.close();
        }
        return searchHistory;

    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // db.execSQL(DATABASE_CREATE_MEMBER);
            db.execSQL(DATABASE_CREATE_FAVORITES);
            db.execSQL(DATABASE_CREATE_SEARCHHISTORY);
            db.execSQL(DATABASE_CREATE_SYSTEMS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_SEARCHHISTORY);
            db.execSQL(DATABASE_CREATE_SEARCHHISTORY);

//            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_FAVORITES);
//            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_SYSTEMS);
            db.execSQL(DATABASE_CREATE_FAVORITES);
            db.execSQL(DATABASE_CREATE_SYSTEMS);
            // onCreate(db);
        }
    }

}
