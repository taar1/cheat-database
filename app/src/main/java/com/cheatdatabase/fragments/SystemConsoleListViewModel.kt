package com.cheatdatabase.fragments

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.cheatdatabase.data.RoomCheatDatabase
import com.cheatdatabase.data.model.SystemModel
import com.cheatdatabase.data.repository.SystemRepository

class SystemConsoleListViewModel(app: Application) : AndroidViewModel(app) {
    private val TAG = "SystemConsoleListViewMo"

    private val repository: SystemRepository

    var allSystems: LiveData<List<SystemModel>>

    init {
        val systemDao = RoomCheatDatabase.getDatabase(app).systemDao()
        repository = SystemRepository(systemDao)

        allSystems = repository.allSystems
    }

    fun getAllSystemsObserver(): LiveData<List<SystemModel>> {
        Log.d(TAG, "XXXXX getAllSystemsObserver: ")
        allSystems = repository.allSystems
        return allSystems
    }

//    fun getAllSystems() {
//        Log.d(TAG, "XXXXX getAllSystems: ")
//
//        val dao = RoomCheatDatabase.getDatabase(getApplication()).systemDao()
//        val systemList = dao?.allAsList
//
//        allSystems.postValue(systemList)
//    }

}