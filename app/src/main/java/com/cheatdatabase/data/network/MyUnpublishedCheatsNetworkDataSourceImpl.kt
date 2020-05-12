package com.cheatdatabase.data.network

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cheatdatabase.data.model.Cheat
import com.cheatdatabase.rest.KotlinRestApi

class MyUnpublishedCheatsNetworkDataSourceImpl(private val kotlinRestApi: KotlinRestApi) : MyUnpublishedCheatsNetworkDataSource {

    private val _downloadCheatList = MutableLiveData<List<Cheat>>()


    // TODO dieses MVVM tutorial anschauen: https://www.youtube.com/watch?v=JLwW5HivZg4
    // TODO dieses MVVM tutorial anschauen: https://www.youtube.com/watch?v=JLwW5HivZg4
    // TODO dieses MVVM tutorial anschauen: https://www.youtube.com/watch?v=JLwW5HivZg4
    // TODO dieses MVVM tutorial anschauen: https://www.youtube.com/watch?v=JLwW5HivZg4
    // TODO dieses MVVM tutorial anschauen: https://www.youtube.com/watch?v=JLwW5HivZg4


    override val downloadCheatList: LiveData<List<Cheat>>
        get() = _downloadCheatList

    override suspend fun fetchCheatList(memberId: Int) {
        try {
            val fetchedCheatList = kotlinRestApi
                    .getCheatsByMemberId(memberId)
                    .await()
            _downloadCheatList.postValue(fetchedCheatList)
        } catch (e: Exception) {
            Log.e("Connectivity", "No internet connection...", e)
        }
    }
}