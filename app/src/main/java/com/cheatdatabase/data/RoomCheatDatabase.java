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
//    public static final int DATABASE_VERSION = 4; // From 20.04.2020
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
            // Nothing to do but we are already at database version 5, so.... leave this here.
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Nothing to do but we are already at database version 5, so.... leave this here.
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Nothing to do but we are already at database version 5, so.... leave this here.
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Nothing to do but we are already at database version 5, so.... leave this here.
        }
    };


}
