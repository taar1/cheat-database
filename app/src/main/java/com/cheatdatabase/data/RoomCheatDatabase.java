package com.cheatdatabase.data;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.cheatdatabase.data.dao.FavoriteCheatDao;
import com.cheatdatabase.data.dao.SearchHistoryDao;
import com.cheatdatabase.data.dao.SystemDao;
import com.cheatdatabase.data.model.FavoriteCheatModel;
import com.cheatdatabase.data.model.SearchHistoryModel;
import com.cheatdatabase.data.model.SystemModel;

@Database(entities = {FavoriteCheatModel.class, SearchHistoryModel.class, SystemModel.class}, version = RoomCheatDatabase.DATABASE_VERSION, exportSchema = false)
public abstract class RoomCheatDatabase extends RoomDatabase {

    private static final String TAG = "RoomCheatDatabase";

    // 3 - From 30.06.2015
    // 4 - From 20.04.2020
    // 5 - From 24.04.2020
    // 5 - From 24.04.2020

    private static RoomCheatDatabase INSTANCE;
    private static final String DATABASE_NAME = "data";
    public static final int DATABASE_VERSION = 5; // From 24.04.2020

    public static RoomCheatDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (RoomCheatDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), RoomCheatDatabase.class, DATABASE_NAME)
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract FavoriteCheatDao favoriteDao();

    public abstract SystemDao systemDao();

    public abstract SearchHistoryDao searchHistoryDao();


    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            Log.d(TAG, "XXXXX migrate: MIGRATION_1_2");
            // Nothing to do but we are already at database version 5, so.... leave this here.
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            Log.d(TAG, "XXXXX migrate: MIGRATION_2_3");
            // Nothing to do but we are already at database version 5, so.... leave this here.
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            Log.d(TAG, "XXXXX migrate: MIGRATION_3_4");
            // Nothing to do but we are already at database version 5, so.... leave this here.
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            Log.d(TAG, "XXXXX migrate: MIGRATION_4_5");

            databaseChangesForVersion4And5(database);
        }
    };

    static void databaseChangesForVersion4And5(SupportSQLiteDatabase database) {
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


        database.execSQL("CREATE TABLE new_systems (" +
                "_id INTEGER PRIMARY KEY NOT NULL, " +
                "name TEXT, " +
                "gamecount INTEGER NOT NULL, " +
                "cheatcount INTEGER NOT NULL, " +
                "lastmod TEXT)");
        database.execSQL("INSERT INTO new_systems (_id, name, gamecount, cheatcount, lastmod) " +
                "SELECT _id, name, gamecount, cheatcount, lastmod FROM systems");
        database.execSQL("DROP TABLE systems");
        database.execSQL("ALTER TABLE new_systems RENAME TO systems");

        database.execSQL("DROP TABLE searchhistory");
        database.execSQL("CREATE TABLE searchhistory (" +
                "_id INTEGER PRIMARY KEY NOT NULL, " +
                "searchquery TEXT, " +
                "searchtime TEXT)");
    }

}
