package com.cheatdatabase.fragments

import android.app.Application
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cheatdatabase.data.RoomCheatDatabase
import com.cheatdatabase.data.model.SystemModel
import com.cheatdatabase.data.repository.SystemRepository
import kotlinx.coroutines.launch
import java.io.IOException

class SystemConsoleListViewModel @ViewModelInject constructor(app: Application) : AndroidViewModel(app) {
    private val TAG = "SystemConsoleListViewMo"

    private val repository: SystemRepository = SystemRepository(RoomCheatDatabase.getDatabase(app))

    /**
     * A list of systems that can be shown on the screen. This is private to avoid exposing a
     * way to set this value to observers.
     */
    private val _systems = MutableLiveData<List<SystemModel>>()

    /**
     * A playlist of videos that can be shown on the screen. Views should use this to get access
     * to the data.
     */
    val systems: LiveData<List<SystemModel>>
        get() = _systems


    /**
     * Event triggered for network error. This is private to avoid exposing a
     * way to set this value to observers.
     */
    private var _eventNetworkError = MutableLiveData(false)

    /**
     * Event triggered for network error. Views should use this to get access
     * to the data.
     */
    val eventNetworkError: LiveData<Boolean>
        get() = _eventNetworkError


    /**
     * Flag to display the error message. This is private to avoid exposing a
     * way to set this value to observers.
     */
    private var _isNetworkErrorShown = MutableLiveData(false)

    /**
     * Flag to display the error message. Views should use this to get access
     * to the data.
     */
    val isNetworkErrorShown: LiveData<Boolean>
        get() = _isNetworkErrorShown

    init {
        refreshDataFromNetwork()
    }

    fun refreshDataFromNetwork() {
        viewModelScope.launch {
            try {
                _eventNetworkError.value = false
                _isNetworkErrorShown.value = false

                repository.getSystemsListFromNetwork()
                repository.getSystemsListFromRoom()
                _systems.postValue(repository.systemsList)

                Log.d(TAG, "XXXXX refreshDataFromRepository: NETWORK OK")
            } catch (networkError: IOException) {
                Log.d(TAG, "XXXXX refreshDataFromRepository: IOException NO NETWORK")

                repository.getSystemsListFromRoom()
                _systems.postValue(repository.systemsList)
            }

            // If data is empty show a error message
            if (repository.systemsList.isNullOrEmpty()) {
                Log.d(TAG, "XXXXX refreshDataFromRepository: isNullOrEmpty")
                _eventNetworkError.value = true
            }
        }
    }

    /**
     * Resets the network error flag.
     */
    fun onNetworkErrorShown() {
        _isNetworkErrorShown.value = true
    }

}