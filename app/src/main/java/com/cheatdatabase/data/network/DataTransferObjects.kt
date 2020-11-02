package com.cheatdatabase.data.network

import com.cheatdatabase.data.model.SystemModel


data class SystemContainer(val systems: List<SystemModel>)

/**
 * @Unused
 * @Unused
 * Convert Network results to database objects
 */
fun SystemContainer.asDatabaseModel(): List<SystemModel> {
    return systems.map {
        SystemModel(it.id, it.systemName, it.gamesCount, it.cheatCount, it.lastmod)
    }
}