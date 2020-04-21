package com.cheatdatabase.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cheatdatabase.callbacks.GenericCallback;
import com.cheatdatabase.model.Cheat;
import com.cheatdatabase.model.Game;
import com.cheatdatabase.model.Screenshot;
import com.cheatdatabase.model.SystemPlatform;
import com.cheatdatabase.rest.RestApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import needle.Needle;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "data";
    private static final int DATABASE_VERSION = 3; // From 30.06.2015
//    private static final int DATABASE_VERSION = 4; // From 20.04.2020

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Favorite.CREATE_TABLE);
        db.execSQL(GameSystemTable.CREATE_TABLE);
        db.execSQL(SearchHistory.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Loop through each version when an upgrade occurs.
        for (int version = oldVersion + 1; version <= newVersion; version++) {
            switch (version) {
                case 2:
                    // Apply changes made in version 2
                    Log.d(TAG, "onUpgrade to Version 2: ");
                    break;
                case 3:
                    // Apply changes made in version 3
                    Log.d(TAG, "onUpgrade to Version 3: ");
                    break;
                case 4:
                    // Apply changes made in version 4
                    Log.d(TAG, "onUpgrade to Version 4: ");
                    databaseChangesForVersion4(db);
                    break;

            }
        }
    }

    private void databaseChangesForVersion4(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE new_favorites (" +
                "cheat_id INTEGER PRIMARY KEY NOT NULL, " +
                "cheat_title TEXT, " +
                "cheat_text TEXT, " +
                "game_id INTEGER NOT NULL, " +
                "game_name TEXT, " +
                "system_id INTEGER NOT NULL, " +
                "system_name TEXT, " +
                "language_id INTEGER NOT NULL, " +
                "walkthrough_format INTEGER, " +
                "member_id INTEGER NOT NULL)");
        database.execSQL("INSERT INTO new_favorites (cheat_id, cheat_title, cheat_text, game_id, game_name, system_id, system_name, language_id, walkthrough_format, member_id) " +
                "SELECT cheat_id, cheat_title, cheat_text, game_id, game_name, system_id, system_name, language_id, walkthrough_format, member_id FROM favorites");
        database.execSQL("DROP TABLE favorites");
        database.execSQL("ALTER TABLE new_favorites RENAME TO favorites");
    }

    private void saveScreenshotsToSdCard(Cheat cheat, GenericCallback callback) {
        Needle.onBackgroundThread().execute(() -> {
            try {
                for (Screenshot s : cheat.getScreenshotList()) {
                    if (Tools.isSdWriteable()) {
                        s.saveToSd();
                    }
                }

                callback.success();
            } catch (IOException e) {
                callback.fail(e);
            }
        });
    }

    public long insertFavoriteCheat(Cheat cheat, GenericCallback callback) {
        if (cheat == null) {
            return 0;
        } else {
            if (cheat.isScreenshots()) {
                saveScreenshotsToSdCard(cheat, callback);
            }

            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues initialValues = new ContentValues();
            initialValues.put(Favorite.FAV_GAME_ID, cheat.getGameId());
            initialValues.put(Favorite.FAV_GAMENAME, cheat.getGameName());
            initialValues.put(Favorite.FAV_CHEAT_ID, cheat.getCheatId());
            initialValues.put(Favorite.FAV_CHEAT_TITLE, cheat.getCheatTitle());
            initialValues.put(Favorite.FAV_CHEAT_TEXT, cheat.getCheatText());
            initialValues.put(Favorite.FAV_LANGUAGE_ID, cheat.getLanguageId());
            initialValues.put(Favorite.FAV_SYSTEM_ID, cheat.getSystemId());
            initialValues.put(Favorite.FAV_SYSTEM_NAME, cheat.getSystemName());
            int walkthroughFormat = 0;
            if (cheat.isWalkthroughFormat()) {
                walkthroughFormat = 1;
            }
            initialValues.put(Favorite.FAV_WALKTHROUGH_FORMAT, walkthroughFormat);

            // insert row
            long id = db.insert(Favorite.TABLE_NAME, null, initialValues);

            // close db connection
            db.close();

            // return newly inserted row id
            return id;
        }
    }

    public void insertFavoriteCheats(Game gameObj, boolean isAchievementsEnabled, RestApi restApi, GenericCallback callback) {
        SQLiteDatabase db = this.getWritableDatabase();

        Call<List<Cheat>> call = restApi.getCheatsByGameId(gameObj.getGameId(), isAchievementsEnabled);
        call.enqueue(new Callback<List<Cheat>>() {
            @Override
            public void onResponse(Call<List<Cheat>> cheats, Response<List<Cheat>> response) {
                List<Cheat> cheatList = response.body();

                for (Cheat cheat : cheatList) {
                    if (cheat.isScreenshots()) {
                        // TODO FIXME: currently it ignores success/fail of saving screenshots to SD card...
                        // TODO FIXME: currently it ignores success/fail of saving screenshots to SD card...
                        saveScreenshotsToSdCard(cheat, null);
                    }

                    ContentValues initialValues = new ContentValues();
                    initialValues.put(Favorite.FAV_GAME_ID, cheat.getGame().getGameId());
                    initialValues.put(Favorite.FAV_GAMENAME, cheat.getGame().getGameName());
                    initialValues.put(Favorite.FAV_CHEAT_ID, cheat.getCheatId());
                    initialValues.put(Favorite.FAV_CHEAT_TITLE, cheat.getCheatTitle());
                    initialValues.put(Favorite.FAV_CHEAT_TEXT, cheat.getCheatText());
                    initialValues.put(Favorite.FAV_LANGUAGE_ID, cheat.getLanguageId());
                    initialValues.put(Favorite.FAV_SYSTEM_ID, cheat.getSystem().getSystemId());
                    initialValues.put(Favorite.FAV_SYSTEM_NAME, cheat.getSystem().getSystemName());
                    int walkthroughFormat = 0;
                    if (cheat.isWalkthroughFormat()) {
                        walkthroughFormat = 1;
                    }
                    initialValues.put(Favorite.FAV_WALKTHROUGH_FORMAT, walkthroughFormat);

                    // insert row
                    long returnValue = db.insert(Favorite.TABLE_NAME, null, initialValues);
                    callback.success();
                }

                // close db connection
                db.close();
            }

            @Override
            public void onFailure(Call<List<Cheat>> call, Throwable e) {
                Log.e(TAG, "insertFavoriteCheats onFailure: " + e.getLocalizedMessage());
                callback.fail((Exception) e);
            }
        });


    }

    public boolean deleteFavoritedCheat(Cheat cheat) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean ret = db.delete(Favorite.TABLE_NAME, Favorite.FAV_CHEAT_ID + " = ?",
                new String[]{String.valueOf(cheat.getCheatId())}) > 0;
        db.close();
        return ret;
    }

    public boolean deleteFavoritedCheats(Game game) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean ret = db.delete(Favorite.TABLE_NAME, Favorite.FAV_GAME_ID + " = ?",
                new String[]{String.valueOf(game.getGameId())}) > 0;
        db.close();
        return ret;
    }

    public Cheat getFavoriteCheat(int cheatId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cur = db.query(Favorite.TABLE_NAME,
                new String[]{Favorite.FAV_GAME_ID, Favorite.FAV_GAMENAME, Favorite.FAV_CHEAT_ID, Favorite.FAV_CHEAT_TITLE, Favorite.FAV_CHEAT_TEXT, Favorite.FAV_LANGUAGE_ID, Favorite.FAV_SYSTEM_ID, Favorite.FAV_SYSTEM_NAME, Favorite.FAV_WALKTHROUGH_FORMAT},
                Favorite.FAV_CHEAT_ID + "=?",
                new String[]{String.valueOf(cheatId)}, null, null, null, null);

        if (cur.moveToFirst()) {
            int gameId = cur.getInt(cur.getColumnIndex(Favorite.FAV_GAME_ID));
            String gameName = cur.getString(cur.getColumnIndex(Favorite.FAV_GAMENAME));
            String cheatTitle = cur.getString(cur.getColumnIndex(Favorite.FAV_CHEAT_TITLE));
            String cheatText = cur.getString(cur.getColumnIndex(Favorite.FAV_CHEAT_TEXT));
            int languageId = cur.getInt(cur.getColumnIndex(Favorite.FAV_LANGUAGE_ID));
            int systemId = cur.getInt(cur.getColumnIndex(Favorite.FAV_SYSTEM_ID));
            String systemName = cur.getString(cur.getColumnIndex(Favorite.FAV_SYSTEM_NAME));
            int walkthrough = cur.getInt(cur.getColumnIndex(Favorite.FAV_WALKTHROUGH_FORMAT));
            boolean walkthroughFormat = false;
            if (walkthrough == 1) {
                walkthroughFormat = true;
            }

            Cheat cheat = new Cheat(gameId, gameName, cheatId, cheatTitle, cheatText, languageId, systemId, systemName, walkthroughFormat);
            cur.close();
            return cheat;
        }
        cur.close();
        return null;
    }

    public List<Cheat> getAllFavoritedCheatsByGame(int gameId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int countRows = countFavoritedCheats(gameId);
        if (countRows == 0) {
            return null;
        } else {
            List<Cheat> cheatList = new ArrayList<>();

            Cursor cur = db.query(Favorite.TABLE_NAME, new String[]{
                            Favorite.FAV_GAME_ID,
                            Favorite.FAV_GAMENAME,
                            Favorite.FAV_CHEAT_ID,
                            Favorite.FAV_CHEAT_TITLE,
                            Favorite.FAV_CHEAT_TEXT,
                            Favorite.FAV_LANGUAGE_ID,
                            Favorite.FAV_SYSTEM_ID,
                            Favorite.FAV_SYSTEM_NAME,
                            Favorite.FAV_WALKTHROUGH_FORMAT},
                    Favorite.FAV_GAME_ID + "=" + gameId, null, Favorite.FAV_CHEAT_ID, null, Favorite.FAV_CHEAT_TITLE);

            if (cur.moveToFirst()) {
                if (cur.isFirst()) {
                    do {
                        String gameName = cur.getString(cur.getColumnIndex(Favorite.FAV_GAMENAME));
                        int cheatId = cur.getInt(cur.getColumnIndex(Favorite.FAV_CHEAT_ID));
                        String cheatTitle = cur.getString(cur.getColumnIndex(Favorite.FAV_CHEAT_TITLE));
                        String cheatText = cur.getString(cur.getColumnIndex(Favorite.FAV_CHEAT_TEXT));
                        int languageId = cur.getInt(cur.getColumnIndex(Favorite.FAV_LANGUAGE_ID));
                        int systemId = cur.getInt(cur.getColumnIndex(Favorite.FAV_SYSTEM_ID));
                        String systemName = cur.getString(cur.getColumnIndex(Favorite.FAV_SYSTEM_NAME));
                        int walkthrough = cur.getInt(cur.getColumnIndex(Favorite.FAV_WALKTHROUGH_FORMAT));
                        boolean walkthroughFormat = false;
                        if (walkthrough == 1) {
                            walkthroughFormat = true;
                        }

                        Game game = new Game(gameId, gameName, systemId, systemName);
                        SystemPlatform system = new SystemPlatform(systemId, systemName);

                        Cheat cheat = new Cheat(gameId, gameName, cheatId, cheatTitle, cheatText, languageId, systemId, systemName, walkthroughFormat);

                        cheat.setGame(game);
                        cheat.setSystem(system);

                        cheatList.add(cheat);
                    } while (cur.moveToNext());
                }

                cur.close();
                return cheatList;
            }
        }
        return null;
    }


    public List<Game> getAllFavoritedGames() {
        SQLiteDatabase db = this.getReadableDatabase();
        int countRows = countFavoritedGames();
        if (countRows == 0) {
            return null;
        } else {
            Cursor cur = db.query(Favorite.TABLE_NAME, new String[]{Favorite.FAV_GAME_ID, Favorite.FAV_GAMENAME, Favorite.FAV_CHEAT_ID, Favorite.FAV_CHEAT_TITLE, Favorite.FAV_CHEAT_TEXT, Favorite.FAV_SYSTEM_ID, Favorite.FAV_SYSTEM_NAME, Favorite.FAV_LANGUAGE_ID, Favorite.FAV_WALKTHROUGH_FORMAT}, null, null, Favorite.FAV_GAME_ID, null, Favorite.FAV_GAMENAME);

            List<Game> favGames = new ArrayList<>();

            if (cur.moveToFirst()) {
                if (cur.isFirst()) {
                    do {
                        int gameId = cur.getInt(cur.getColumnIndex(Favorite.FAV_GAME_ID));
                        String gameName = cur.getString(cur.getColumnIndex(Favorite.FAV_GAMENAME));
                        int systemId = cur.getInt(cur.getColumnIndex(Favorite.FAV_SYSTEM_ID));
                        String systemName = cur.getString(cur.getColumnIndex(Favorite.FAV_SYSTEM_NAME));
                        int numberOfFavoritedCheats = countFavoritedCheats(gameId);

                        Game tempGame = new Game(gameId, gameName, systemId, systemName);
                        tempGame.setCheatsCount(numberOfFavoritedCheats);

                        favGames.add(tempGame);
                    } while (cur.moveToNext());
                }

                cur.close();
                return favGames;
            }
        }

        return null;
    }

    public int countFavoritedGames() {
        return countRows(Favorite.TABLE_NAME, Favorite.FAV_GAME_ID, null, Favorite.FAV_GAME_ID);
    }

    public int countFavoritedCheats(int gameId) {
        return countRows(Favorite.TABLE_NAME, Favorite.FAV_GAME_ID, String.valueOf(gameId), Favorite.FAV_CHEAT_ID);
    }

    public int countRows(String sqlTable, String columnToCount, String columnValue, String countBy) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCursor;
        if (columnValue == null) {
            mCursor = db.query(sqlTable, new String[]{columnToCount}, null, null, countBy, null, null);
        } else {
            mCursor = db.query(sqlTable, new String[]{columnToCount}, columnToCount + "=" + columnValue, null, countBy, null, null);
        }
        int count = mCursor.getCount();
        mCursor.close();
        return count;
    }

    public List<Cheat> getFavoriteCheats(int gameId) {
        SQLiteDatabase db = this.getReadableDatabase();

        int countRows = countFavoritedCheats(gameId);
        if (countRows == 0) {
            return null;
        } else {
            List<Cheat> favCheats = new ArrayList<>();

            Cursor cur = db.query(Favorite.TABLE_NAME, new String[]{Favorite.FAV_GAME_ID, Favorite.FAV_GAMENAME, Favorite.FAV_CHEAT_ID, Favorite.FAV_CHEAT_TITLE, Favorite.FAV_CHEAT_TEXT, Favorite.FAV_LANGUAGE_ID, Favorite.FAV_SYSTEM_ID, Favorite.FAV_SYSTEM_NAME, Favorite.FAV_WALKTHROUGH_FORMAT}, Favorite.FAV_GAME_ID + "=" + gameId, null, Favorite.FAV_CHEAT_ID, null, Favorite.FAV_CHEAT_TITLE);

            if (cur.moveToFirst()) {
                if (cur.isFirst()) {
                    do {
                        String gameName = cur.getString(cur.getColumnIndex(Favorite.FAV_GAMENAME));
                        int cheatId = cur.getInt(cur.getColumnIndex(Favorite.FAV_CHEAT_ID));
                        String cheatTitle = cur.getString(cur.getColumnIndex(Favorite.FAV_CHEAT_TITLE));
                        String cheatText = cur.getString(cur.getColumnIndex(Favorite.FAV_CHEAT_TEXT));
                        int languageId = cur.getInt(cur.getColumnIndex(Favorite.FAV_LANGUAGE_ID));
                        int systemId = cur.getInt(cur.getColumnIndex(Favorite.FAV_SYSTEM_ID));
                        String systemName = cur.getString(cur.getColumnIndex(Favorite.FAV_SYSTEM_NAME));
                        int walkthrough = cur.getInt(cur.getColumnIndex(Favorite.FAV_WALKTHROUGH_FORMAT));
                        boolean walkthroughFormat = false;
                        if (walkthrough == 1) {
                            walkthroughFormat = true;
                        }

                        favCheats.add(new Cheat(gameId, gameName, cheatId, cheatTitle, cheatText, languageId, systemId, systemName, walkthroughFormat));
                    } while (cur.moveToNext());
                }

                cur.close();
                db.close();
                return favCheats;
            }
        }
        return null;
    }

    public List<SystemPlatform> getAllSystemsAndCount() {
        try {
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cur = db.query(GameSystemTable.TABLE_NAME, new String[]{GameSystemTable.SYS_SYSTEM_ID, GameSystemTable.SYS_SYSTEM_NAME, GameSystemTable.SYS_SYSTEM_GAMECOUNT, GameSystemTable.SYS_SYSTEM_CHEATCOUNT, GameSystemTable.SYS_SYSTEM_LASTMOD}, null, null, null, null, GameSystemTable.SYS_SYSTEM_NAME + " COLLATE NOCASE ASC;");

            ArrayList<SystemPlatform> systems = null;

            if (cur.moveToFirst()) {
                if (cur.isFirst()) {
                    systems = new ArrayList<>();
                    do {
                        int systemId = cur.getInt(cur.getColumnIndex(GameSystemTable.SYS_SYSTEM_ID));
                        String systemName = cur.getString(cur.getColumnIndex(GameSystemTable.SYS_SYSTEM_NAME));
                        int gameCount = cur.getInt(cur.getColumnIndex(GameSystemTable.SYS_SYSTEM_GAMECOUNT));
                        int cheatCount = cur.getInt(cur.getColumnIndex(GameSystemTable.SYS_SYSTEM_CHEATCOUNT));
                        String lastMod = cur.getString(cur.getColumnIndex(GameSystemTable.SYS_SYSTEM_LASTMOD));

                        SystemPlatform sysPla = new SystemPlatform();
                        sysPla.setSystemId(systemId);
                        sysPla.setSystemName(systemName);
                        sysPla.setGameCount(gameCount);
                        sysPla.setCheatCount(cheatCount);

                        // TODO TESTEN OB DAS SO KORREKT FUNKTIONIERT MIT DEM KONVERTIEREN
                        sysPla.setLastModTimeStamp(new Date(Long.parseLong(lastMod) * 1000));

                        systems.add(sysPla);

                    } while (cur.moveToNext());
                }

                cur.close();
                return systems;
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "getAllSystemsAndCount: Error: ", e);
        }

        return null;
    }

    public int updateSystemsAndCount(List<SystemPlatform> systemsAndCount) {
        SQLiteDatabase db = this.getReadableDatabase();

        int insertCount = 0;
        deleteSystemsAndCount();

        try {
            for (SystemPlatform sysPla : systemsAndCount) {
                ContentValues initialValues = new ContentValues();
                initialValues.put(GameSystemTable.SYS_SYSTEM_ID, sysPla.getSystemId());
                initialValues.put(GameSystemTable.SYS_SYSTEM_NAME, sysPla.getSystemName());
                initialValues.put(GameSystemTable.SYS_SYSTEM_GAMECOUNT, sysPla.getGameCount());
                initialValues.put(GameSystemTable.SYS_SYSTEM_CHEATCOUNT, sysPla.getCheatCount());
                initialValues.put(GameSystemTable.SYS_SYSTEM_LASTMOD, sysPla.getLastModTimeStamp());

                Long l_result = db.insert(GameSystemTable.TABLE_NAME, null, initialValues);
                if (l_result > -1) {
                    insertCount++;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "updateSystemsAndCount: ", e);
        }

//        db.close();
        return insertCount;
    }

    public void deleteSystemsAndCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(GameSystemTable.TABLE_NAME, null, null);
//        db.close();
    }

    /**
     * Inserts the last search query to the search history.
     *
     * @param query
     * @return
     */
    public int insertSearchQuery(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());

        ContentValues initialValues = new ContentValues();
        initialValues.put(SearchHistory.KEY_SEARCHHISTORY_QUERY, query.trim());
        initialValues.put(SearchHistory.KEY_SEARCHHISTORY_SEARCHTIME, currentTimestamp.toString());

        long retVal = db.insert(SearchHistory.TABLE_NAME, null, initialValues);
        return Integer.parseInt(String.valueOf(retVal));
    }

    /**
     * Gets the latest search queries done by the user.
     *
     * @param historyLength
     * @return
     */
    public List<String> getSearchHistory(int historyLength) {
        SQLiteDatabase db = this.getReadableDatabase();
        List searchHistory = new ArrayList<>();

        Cursor cur = db.query(SearchHistory.TABLE_NAME, new String[]{SearchHistory.KEY_ROWID, SearchHistory.KEY_SEARCHHISTORY_QUERY, SearchHistory.KEY_SEARCHHISTORY_SEARCHTIME}, null, null, null, null, SearchHistory.KEY_ROWID + " DESC", historyLength + "");

        if (cur.moveToFirst()) {
            if (cur.isFirst()) {
                int i = 0;
                do {
                    String searchQuery = cur.getString(cur.getColumnIndex(SearchHistory.KEY_SEARCHHISTORY_QUERY));
                    // String searchTime =
                    // cur.getString(cur.getColumnIndex(KEY_SEARCHHISTORY_SEARCHTIME));
                    // String rowId =
                    // cur.getString(cur.getColumnIndex(KEY_ROWID));

                    searchHistory.add(searchQuery);
                } while (cur.moveToNext());
            }

            cur.close();
            db.close();
        }
        return searchHistory;

    }


}
