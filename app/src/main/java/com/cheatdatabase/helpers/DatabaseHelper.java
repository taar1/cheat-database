package com.cheatdatabase.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.Screenshot;
import com.cheatdatabase.businessobjects.SystemPlatform;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "data";
    private static final int DATABASE_VERSION = 3; // From 30.06.2015

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Favorite.CREATE_TABLE);
        db.execSQL(GameSystemTable.CREATE_TABLE);
        db.execSQL(SearchHistory.CREATE_TABLE);
    }

    // TODO FAVORITEN HINZUFÜGEN GEHT, MAN KANN DEN FAVORITEN DANACH ABER NICHT ANSCHAUEN KOMMT EINE FEHLERMELDUNG
    // TODO FAVORITEN HINZUFÜGEN GEHT, MAN KANN DEN FAVORITEN DANACH ABER NICHT ANSCHAUEN KOMMT EINE FEHLERMELDUNG
    // TODO FAVORITEN HINZUFÜGEN GEHT, MAN KANN DEN FAVORITEN DANACH ABER NICHT ANSCHAUEN KOMMT EINE FEHLERMELDUNG
    // TODO FAVORITEN HINZUFÜGEN GEHT, MAN KANN DEN FAVORITEN DANACH ABER NICHT ANSCHAUEN KOMMT EINE FEHLERMELDUNG
    // TODO FAVORITEN HINZUFÜGEN GEHT, MAN KANN DEN FAVORITEN DANACH ABER NICHT ANSCHAUEN KOMMT EINE FEHLERMELDUNG
    // TODO FAVORITEN HINZUFÜGEN GEHT, MAN KANN DEN FAVORITEN DANACH ABER NICHT ANSCHAUEN KOMMT EINE FEHLERMELDUNG
    // TODO FAVORITEN HINZUFÜGEN GEHT, MAN KANN DEN FAVORITEN DANACH ABER NICHT ANSCHAUEN KOMMT EINE FEHLERMELDUNG

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + Favorite.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + GameSystemTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SearchHistory.TABLE_NAME);
        onCreate(db);
    }

    public void saveScreenshotsToSdCard(Cheat cheat) {
        for (Screenshot s : cheat.getScreenshotList()) {
            if (Tools.isSdWriteable()) {
                s.saveToSd();
            }
        }
    }

    public long insertFavoriteCheat(Cheat cheat) {
        if (cheat == null) {
            return 0;
        } else {
            if (cheat.isScreenshots()) {
                saveScreenshotsToSdCard(cheat);
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

    public long insertFavoriteCheats(List<Cheat> cheats) {
        long id = 0;

        SQLiteDatabase db = this.getWritableDatabase();

        for (Cheat cheat : cheats) {
            if (cheat.isScreenshots()) {
                saveScreenshotsToSdCard(cheat);
            }

            ContentValues initialValues = new ContentValues();
            initialValues.put(Favorite.FAV_GAME_ID, cheat.getGameId());
            initialValues.put(Favorite.FAV_GAMENAME, cheat.getGameName());
            initialValues.put(Favorite.FAV_CHEAT_ID, cheat.getCheatId());
            initialValues.put(Favorite.FAV_CHEAT_TITLE, cheat.getCheatTitle());
            initialValues.put(Favorite.FAV_CHEAT_TEXT, cheat.getCheatText());
            initialValues.put(Favorite.FAV_LANGUAGE_ID, cheat.getLanguageId());
            initialValues.put(Favorite.FAV_SYSTEM_ID, cheat.getSystemId());
            initialValues.put(Favorite.FAV_SYSTEM_NAME, cheat.getSystemName());
            initialValues.put(Favorite.FAV_MEMBER_ID, cheat.getSubmittingMember().getMid());
            int walkthroughFormat = 0;
            if (cheat.isWalkthroughFormat()) {
                walkthroughFormat = 1;
            }
            initialValues.put(Favorite.FAV_WALKTHROUGH_FORMAT, walkthroughFormat);

            // insert row
            id = db.insert(Favorite.TABLE_NAME, null, initialValues);
        }

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public int insertFavoriteCheats(Game game) {
        long id = 0;

        SQLiteDatabase db = this.getWritableDatabase();

        Cheat[] cheats = Webservice.getCheatsByGameId(game.getGameId(), game.getGameName());

        for (int i = 0; i < cheats.length; i++) {
            Cheat cheat = cheats[i];

            if (cheat.isScreenshots()) {
                saveScreenshotsToSdCard(cheat);
            }

            ContentValues initialValues = new ContentValues();
            initialValues.put(Favorite.FAV_GAME_ID, cheat.getGameId());
            initialValues.put(Favorite.FAV_GAMENAME, cheat.getGameName());
            initialValues.put(Favorite.FAV_CHEAT_ID, cheat.getCheatId());
            initialValues.put(Favorite.FAV_CHEAT_TITLE, cheat.getCheatTitle());
            initialValues.put(Favorite.FAV_CHEAT_TEXT, cheat.getCheatText());
            initialValues.put(Favorite.FAV_LANGUAGE_ID, cheat.getLanguageId());
            initialValues.put(Favorite.FAV_SYSTEM_ID, game.getSystemId());
            initialValues.put(Favorite.FAV_SYSTEM_NAME, game.getSystemName());
            int walkthroughFormat = 0;
            if (cheat.isWalkthroughFormat()) {
                walkthroughFormat = 1;
            }
            initialValues.put(Favorite.FAV_WALKTHROUGH_FORMAT, walkthroughFormat);

            // insert row
            id = db.insert(Favorite.TABLE_NAME, null, initialValues);
        }

        // close db connection
        db.close();

        // return newly inserted row id
        return Integer.parseInt(String.valueOf(id));
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
            List<Cheat> favCheats = new ArrayList<>();

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

                        favCheats.add(new Cheat(gameId, gameName, cheatId, cheatTitle, cheatText, languageId, systemId, systemName, walkthroughFormat));
                    } while (cur.moveToNext());
                }

                cur.close();
                return favCheats;
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
                    sysPla.setLastModTimeStamp(Long.parseLong(lastMod));

                    systems.add(sysPla);

                } while (cur.moveToNext());
            }

            cur.close();
            return systems;
        }

        return null;
    }

    public int updateSystemsAndCount(List<SystemPlatform> systemsAndCount) {
        SQLiteDatabase db = this.getReadableDatabase();

        int insertCount = 0;
        deleteSystemsAndCount();

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

        db.close();
        return insertCount;
    }

    public void deleteSystemsAndCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(GameSystemTable.TABLE_NAME, null, null);
        db.close();
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
