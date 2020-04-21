package com.cheatdatabase.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.cheatdatabase.data.dao.FavoriteCheatDao;
import com.cheatdatabase.data.dao.SearchHistoryDao;
import com.cheatdatabase.data.dao.SystemDao;
import com.cheatdatabase.data.model.FavoriteCheatModel;
import com.cheatdatabase.data.model.SearchHistoryModel;
import com.cheatdatabase.data.model.SystemModel;

@Database(entities = {FavoriteCheatModel.class, SearchHistoryModel.class, SystemModel.class}, version = RoomCheatDatabase.DATABASE_VERSION, exportSchema = false)
public abstract class RoomCheatDatabase extends androidx.room.RoomDatabase {

    private static RoomCheatDatabase INSTANCE;
    private static final String DATABASE_NAME = "data";
    //    public static final int DATABASE_VERSION = 3; // From 30.06.2015
    public static final int DATABASE_VERSION = 4; // From 20.04.2020

    public static RoomCheatDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (RoomCheatDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), RoomCheatDatabase.class, DATABASE_NAME)
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
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
            // Nothing to do but we are already at database version 3, so.... leave this here.
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Nothing to do but we are already at database version 3, so.... leave this here.
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            //database.execSQL("ALTER TABLE favorites ADD COLUMN id INTEGER primary KEY AUTOINCREMENT");

            // TODO FIX ME migration to room doesnt work yet...
            // TODO FIX ME migration to room doesnt work yet...
            // TODO FIX ME migration to room doesnt work yet...

//            database.execSQL("CREATE TABLE new_favorites (" +
//                    "cheat_id INTEGER PRIMARY KEY NOT NULL," +
//                    "cheat_title TEXT," +
//                    "cheat_text TEXT," +
//                    "game_id INTEGER," +
//                    "game_name TEXT," +
//                    "system_id INTEGER," +
//                    "system_name TEXT," +
//                    "language_id INTEGER," +
//                    "game_count INTEGER," +
//                    "walkthrough_format INTEGER," +
//                    "member_id INTEGER)");
//            database.execSQL("INSERT INTO new_favorites (cheat_id, cheat_title, cheat_text, game_id, game_name, system_id, system_name, language_id, game_count, walkthrough_format, member_id) " +
//                    "SELECT cheat_id, cheat_title, cheat_text, game_id, game_name, system_id, system_name, language_id, game_count, walkthrough_format, member_id FROM favorites");
//            database.execSQL("DROP TABLE favorites");
//            database.execSQL("ALTER TABLE new_favorites RENAME TO favorites");
        }
    };


}
