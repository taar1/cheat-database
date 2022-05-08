package com.cheatdatabase.activity.ui.mycheats

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.data.model.UnpublishedCheat
import com.cheatdatabase.data.repository.MyCheatsRepository
import com.cheatdatabase.helpers.Konstanten
import com.google.gson.Gson
import kotlinx.coroutines.launch

class MyUnpublishedCheatsViewModel(application: Application) :
    AndroidViewModel(application) {

    companion object {
        private const val TAG = "MyUnpublishedCheatsView"
    }

    private val member: Member

    var fetchListener: MyUnpublishedCheatsListener? = null

    private val settings: SharedPreferences =
        application.getSharedPreferences(Konstanten.PREFERENCES_FILE, 0)

    fun getMyUnpublishedCheatsByCoroutines() {
        viewModelScope.launch {
            if (member.passwordMd5.isNullOrBlank() || member.mid == 0) {
                fetchListener?.fetchUnpublishedCheatsFail()
            } else {
                val response =
                    MyCheatsRepository().getMyUnpublishedCheats(
                        member.mid,
                        member.passwordMd5
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
                unpublishedCheat, member
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

    init {
        member = Gson().fromJson(
            settings.getString(
                Konstanten.MEMBER_OBJECT,
                null
            ), Member::class.java
        )
    }

}