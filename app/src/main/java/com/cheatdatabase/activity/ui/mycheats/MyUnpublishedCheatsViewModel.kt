package com.cheatdatabase.activity.ui.mycheats

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.data.model.UnpublishedCheat
import com.cheatdatabase.data.repository.MyCheatsRepository
import com.cheatdatabase.helpers.AeSimpleMD5
import com.cheatdatabase.helpers.Coroutines
import com.cheatdatabase.helpers.Konstanten
import com.google.gson.Gson

class MyUnpublishedCheatsViewModel(application: Application) :
    AndroidViewModel(application) {

    val TAG = "MyUnpublishedCheatsView"

    private val member: Member

    var fetchListener: MyUnpublishedCheatsListener? = null

    private val settings: SharedPreferences =
        application.getSharedPreferences(Konstanten.PREFERENCES_FILE, 0)

    fun getMyUnpublishedCheatsByCoroutines() {
        Coroutines.main {
            val response = MyCheatsRepository().getMyUnpublishedCheats(
                member.mid,
                AeSimpleMD5.MD5(member.password)
            )

            if (response.isSuccessful) {
                fetchListener?.fetchUnpublishedCheatsSuccess(response.body()!!)
            } else {
                fetchListener?.fetchUnpublishedCheatsFail()
            }
        }
    }

    fun deleteUnpublishedCheat(unpublishedCheat: UnpublishedCheat) {
        Coroutines.main {
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
        // TODO use tools.getMember()
        member = Gson().fromJson(
            settings.getString(
                Konstanten.MEMBER_OBJECT,
                null
            ), Member::class.java
        )
    }

}