package com.cheatdatabase.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.cheatdatabase.data.model.SystemModel;

import java.util.List;

@Dao
public interface SystemDao {

    @Query("SELECT * FROM systems")
    List<SystemModel> getAll();

    @Query("SELECT * FROM systems where _id = :systemId")
    SystemModel getSystemById(int systemId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SystemModel systemModel);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<SystemModel> systemModels);

    @Delete
    void delete(SystemModel systemModel);

    @Query("DELETE FROM systems")
    void deleteAll();

    @Update
    void update(SystemModel systemModel);

    @Query("DELETE FROM systems where _id = :systemId")
    void deleteBySystemId(int systemId);


}
