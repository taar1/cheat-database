package com.cheatdatabase.fragments

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.data.model.MyCheatsCount
import com.cheatdatabase.data.repository.MyCheatsRepository
import com.cheatdatabase.helpers.AeSimpleMD5
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

class MyCheatsViewModel @Inject constructor(app: Application) : AndroidViewModel(app) {
    private val TAG = "MyCheatsViewModel"

    private val repository: MyCheatsRepository = MyCheatsRepository()

    /**
     * This is private to avoid exposing a way to set this value to observers.
     */
    private val _myCheats = MutableLiveData<MyCheatsCount>()

    /**
     * Views should use this to get access to the data.
     */
    val myCheats: LiveData<MyCheatsCount>
        get() = _myCheats

    fun getMyCheatsCount(member: Member?) {

        if (member != null) {
            val pwMd5 = AeSimpleMD5.MD5(member.password)

            viewModelScope.launch {
                try {
                    val response = repository.countMyCheats(member.mid, pwMd5)
                    if (response.isSuccessful) {
                        _myCheats.postValue(response.body())
                    } else {
                        _myCheats.postValue(null)
                    }

                    Log.d(TAG, "XXXXX getMyCheatsCount: NETWORK OK")
                } catch (networkError: IOException) {
                    Log.d(TAG, "XXXXX getMyCheatsCount: IOException NO_NETWORK")
                    _myCheats.postValue(null)
                }
            }
        } else {
            _myCheats.postValue(null)
        }
    }

}