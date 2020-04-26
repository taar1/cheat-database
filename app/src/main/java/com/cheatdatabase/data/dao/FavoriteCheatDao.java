package com.cheatdatabase.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.cheatdatabase.data.model.FavoriteCheatModel;

import java.util.List;

@Dao
public interface FavoriteCheatDao {

    @Query("SELECT * FROM favorites")
    LiveData<List<FavoriteCheatModel>> getAll();

    @Query("SELECT * FROM favorites where cheat_id = :cheatId")
    LiveData<FavoriteCheatModel> getCheatByCheatId(int cheatId);

    @Query("SELECT * FROM favorites where game_id = :gameId")
    LiveData<List<FavoriteCheatModel>> getCheatsByGameId(int gameId);

    @Query("SELECT COUNT(game_id) FROM favorites where game_id = :gameId")
    int countCheatsByGame(int gameId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(FavoriteCheatModel favoriteModel);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<FavoriteCheatModel> favoriteModels);

    @Delete
    void delete(FavoriteCheatModel favoriteModel);

    @Update
    void update(FavoriteCheatModel favoriteModel);

    @Query("DELETE FROM favorites where game_id = :gameId")
    void deleteAllByGameId(int gameId);

}
