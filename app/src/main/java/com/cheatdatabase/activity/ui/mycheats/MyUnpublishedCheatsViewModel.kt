package com.cheatdatabase.activity.ui.mycheats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cheatdatabase.data.model.UnpublishedCheat
import com.cheatdatabase.data.repository.MyCheatsRepository
import com.cheatdatabase.helpers.Tools
import kotlinx.coroutines.launch
import javax.inject.Inject

class MyUnpublishedCheatsViewModel(application: Application) :
    AndroidViewModel(application) {

    companion object {
        private const val TAG = "MyUnpublishedCheatsView"
    }

    @Inject
    lateinit var tools: Tools

    var fetchListener: MyUnpublishedCheatsListener? = null

    fun getMyUnpublishedCheatsByCoroutines() {
        viewModelScope.launch {
            if (tools.member.passwordMd5.isNullOrBlank() || tools.member.mid == 0) {
                fetchListener?.fetchUnpublishedCheatsFail()
            } else {
                val response =
                    MyCheatsRepository().getMyUnpublishedCheats(
                        tools.member.mid,
                        tools.member.passwordMd5
                    )

                if (response.isSuccessful) {
                    fetchListener?.fetchUnpublishedCheatsSuccess(response.body()!!)
                } else {
                    fetchListener?.fetchUnpublishedCheatsFail()
                }
            }
        }
    }

    fun deleteUnpublishedCheat(unpublishedCheat: UnpublishedCheat) {
        viewModelScope.launch {
            val response = MyCheatsRepository().deleteUnpublishedCheat(
                unpublishedCheat, tools.member
            )

            if (response.isSuccessful) {
                val responseJsonObject = response.body()!!
                //Log.d(TAG, "XXXXX SUCCESS UNPUBLISHED: $responseJsonObject.returnValue")
                fetchListener?.deleteUnpublishedCheatSuccess(responseJsonObject.returnValue)
            } else {
                fetchListener?.deleteUnpublishedCheatFailed()
            }
        }
    }

}