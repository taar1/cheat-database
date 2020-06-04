package com.cheatdatabase.activity.ui.mycheats

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.data.model.UnpublishedCheat
import com.cheatdatabase.data.repository.RestRepository
import com.cheatdatabase.helpers.AeSimpleMD5
import com.cheatdatabase.helpers.Coroutines
import com.cheatdatabase.helpers.Konstanten
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call

class MyUnpublishedCheatsViewModel(application: Application) :
    AndroidViewModel(application) {

    val TAG = "MyUnpublishedCheatsView"

//    private val retrofit: Retrofit? = null
//    private val restApi: RestApi? = null
//    private val allMembers: Call<List<Member>>? =  null
//    var unpublishedCheatsList: MutableLiveData<List<UnpublishedCheat>>? = null

    private val member: Member
    private var restRepository: RestRepository? = null
    var fetchListener: MyUnpublishedCheatsListener? = null
    private val settings: SharedPreferences =
        application.getSharedPreferences(Konstanten.PREFERENCES_FILE, 0)

    fun getMyUnpublishedCheatsByCoroutines() {
        Coroutines.main {
            val response = UnpublishedCheatsRepositoryKotlin().getMyUnpublishedCheats(
                member.mid,
                AeSimpleMD5.MD5(member.password)
            )
            if (response.isSuccessful) {
                Log.d(TAG, "XXXXX SUCCESS UNPUBLISHED: " + response.body()!!)

                fetchListener?.fetchUnpublishedCheatsSuccess(response.body()!!)
            } else {
                Log.d(TAG, "XXXXX ERROR UNPUBLISHED")
                fetchListener?.fetchUnpublishedCheatsFail("TODO XXXXXX")
            }
        }
    }

    // TODO delete unpublished cheat through coroutines (UnpublishedCheatsRepositoryKotlin)
    // TODO delete unpublished cheat through coroutines (UnpublishedCheatsRepositoryKotlin)
    // TODO delete unpublished cheat through coroutines (UnpublishedCheatsRepositoryKotlin)
    fun deleteUnpublishedCheat(unpublishedCheat: UnpublishedCheat?): Call<JsonObject> {
        return restRepository!!.deleteUnpublishedCheat(unpublishedCheat, member)
    }

    init {
        member = Gson().fromJson(
            settings.getString(
                Konstanten.MEMBER_OBJECT,
                null
            ), Member::class.java
        )
        restRepository = RestRepository(getApplication())
    }

}