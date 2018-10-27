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

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "data";
    private static final int DATABASE_VERSION = 3; // From 30.06.2015

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Favorite.CREATE_TABLE);
        db.execSQL(DATABASE_CREATE_SEARCHHISTORY);
        db.execSQL(DATABASE_CREATE_SYSTEMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + Favorite.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_SEARCHHISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_SYSTEMS);
        onCreate(db);
    }

    public void saveScreenshotsToSdCard(Cheat cheat) {
        for (Screenshot s : cheat.getScreenshotList()) {
            if (Tools.isSdWriteable()) {
                s.saveToSd();
            }
        }
    }

    public long insertFavoriteCheatNew(Cheat cheat) {
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

    public long insertFavoriteCheats(Cheat[] cheats) {
        long id = 0;

        SQLiteDatabase db = this.getWritableDatabase();

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

    public long insertFavoriteCheats(Game game) {
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
        return id;
    }

    public boolean deleteCheat(Cheat cheat) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Favorite.TABLE_NAME, Favorite.FAV_CHEAT_ID + " = ?",
                new String[]{String.valueOf(cheat.getCheatId())});
        db.close();
    }

    public boolean deleteCheats(Game game) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Favorite.TABLE_NAME, Favorite.FAV_GAME_ID + " = ?",
                new String[]{String.valueOf(game.getGameId())});
        db.close();
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
}
