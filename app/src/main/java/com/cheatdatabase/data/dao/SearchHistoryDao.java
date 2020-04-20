package com.cheatdatabase.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cheatdatabase.data.model.SearchHistoryModel;

import java.util.List;

@Dao
public interface SearchHistoryDao {

    @Query("SELECT * FROM searchhistory")
    List<SearchHistoryModel> getAll();

    @Query("SELECT * FROM searchhistory where _id = :searchId")
    SearchHistoryModel getBySearchId(int searchId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SearchHistoryModel systemModel);

    @Delete
    void delete(SearchHistoryModel systemModel);

    @Query("DELETE FROM searchhistory where _id = :searchId")
    void deleteBySearchId(int searchId);


}
