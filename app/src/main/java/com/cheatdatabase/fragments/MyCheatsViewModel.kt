package com.cheatdatabase.fragments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.data.model.MyCheatsCount
import com.cheatdatabase.data.repository.MyCheatsRepository
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

class MyCheatsViewModel @Inject constructor(app: Application) : AndroidViewModel(app) {

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
        member?.let {

            if (it.passwordMd5.isNullOrBlank() || it.mid == 0) {
                _myCheats.postValue(MyCheatsCount(0, 0, 0))
                return
            }

            viewModelScope.launch {
                try {
                    val response = repository.countMyCheats(member.mid, member.passwordMd5)
                    if (response.isSuccessful) {
                        _myCheats.postValue(response.body())
                    } else {
                        _myCheats.postValue(MyCheatsCount(0, 0, 0))
                    }

                } catch (networkError: IOException) {
                    _myCheats.postValue(MyCheatsCount(0, 0, 0))
                }
            }
        }
    }
}