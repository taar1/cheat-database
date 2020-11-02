package com.cheatdatabase.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.cheatdatabase.data.model.SystemModel

@Dao
interface SystemDao {

    @Query("SELECT * FROM systems")
    fun loadAllSystems(): Array<SystemModel>

    @Query("SELECT * FROM systems")
    fun all(): LiveData<List<SystemModel>>

    @Query("SELECT * FROM systems ORDER BY name")
    suspend fun allAsList(): List<SystemModel?>?

    @Query("SELECT * FROM systems where _id = :systemId")
    fun getSystemById(systemId: Int): LiveData<SystemModel?>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(systemModel: SystemModel): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(systemModels: List<SystemModel>): List<Long>

    @Delete
    fun delete(systemModel: SystemModel)

    @Query("DELETE FROM systems")
    fun deleteAll()

    @Update
    fun update(systemModel: SystemModel)

    @Query("DELETE FROM systems where _id = :systemId")
    fun deleteBySystemId(systemId: Int)
}