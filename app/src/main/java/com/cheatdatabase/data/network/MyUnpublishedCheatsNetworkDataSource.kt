package com.cheatdatabase.data.network

import androidx.lifecycle.LiveData
import com.cheatdatabase.data.model.Cheat

interface MyUnpublishedCheatsNetworkDataSource {

    val downloadCheatList: LiveData<List<Cheat>>

    suspend fun fetchCheatList(
            memberId: Int
    )

}