package com.cheatdatabase.data.repository

import com.cheatdatabase.data.RoomCheatDatabase
import com.cheatdatabase.data.model.SystemModel
import com.cheatdatabase.rest.KotlinRestApi
import com.cheatdatabase.rest.SafeApiRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SystemRepository(private val roomCheatDatabase: RoomCheatDatabase) : SafeApiRequest() {
    private val TAG = "SystemRepository"

    var systemsList: List<SystemModel> = listOf()

    var newSystemModels: MutableList<SystemModel> = mutableListOf()

    suspend fun getSystemsListFromRoom() {
        withContext(Dispatchers.IO) {
            systemsList = roomCheatDatabase.systemDao().allAsList() as List<SystemModel>
        }
    }

    suspend fun getSystemsListFromNetwork() {
        withContext(Dispatchers.IO) {
            val systemModelListFromApi: List<SystemModel> = apiRequest { KotlinRestApi().getSystems() }

            for (sp in systemModelListFromApi) {
                newSystemModels.add(sp.toSystemModel())
            }

            roomCheatDatabase.systemDao().deleteAll()
            roomCheatDatabase.systemDao().insertAll(newSystemModels)
        }
    }
}