package com.cheatdatabase.activity.ui.mycheats

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.data.model.UnpublishedCheat
import com.cheatdatabase.data.repository.RestRepository
import com.cheatdatabase.helpers.AeSimpleMD5
import com.cheatdatabase.helpers.Coroutines
import com.cheatdatabase.helpers.Konstanten
import com.cheatdatabase.rest.RestApi
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Retrofit

class MyUnpublishedCheatsViewModel(application: Application) :
    AndroidViewModel(application) {

    val TAG = "MyUnpublishedCheatsView"

    private val retrofit: Retrofit? = null
    private val restApi: RestApi? = null
    private val allMembers: Call<List<Member>>? =
        null
    private val member: Member
    private var restRepository: RestRepository? = null
    var unpublishedCheatsList: MutableLiveData<List<UnpublishedCheat>>? = null
    private val settings: SharedPreferences

    var fetchListener: MyUnpublishedCheatsListener? = null

//    fun init() {
//        if (unpublishedCheatsList != null) {
//            return
//        }
//        restRepository = RestRepository(getApplication())
//    }

//    val myUnpublishedCheats: LiveData<List<UnpublishedCheat>>?
//        get() {
//            unpublishedCheatsList = restRepository!!.getMyUnpublishedCheats(member)
//            return unpublishedCheatsList
//        }


    fun getMyUnpublishedCheatsByCoroutines() {

        // TODO wir sind hier: https://www.youtube.com/watch?v=lAKYRXSFvQQ&list=PLk7v1Z2rk4hjVaZ8DZKe8iT9RIM9OUrwp&index=7
        // TODO es scheint zu funktionieren mit SafeApiRequest und couroutines etc.
        // TODO jetzt noch alles aufräumen und "getMyUnpublishedCheats()" austesten....

        Log.d(TAG, "XXXXX getMyUnpublishedCheatsByCoroutines 1")
//        Coroutines.main {
//            try {
//                val response = UnpublishedCheatsRepositoryKotlin().getTopMembersUsingSafeApiRequest()
//                Log.d(TAG, "XXXXX SUCCESS TOP MEMBERS X: " + response)
//            } catch (e: ApiException) {
//                Log.e(TAG, "XXXXX ERROR TOP MEMBERS X: " + e.message)
//            }
//        }

        Log.d(TAG, "XXXXX getMyUnpublishedCheatsByCoroutines 2")
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

    fun deleteUnpublishedCheat(unpublishedCheat: UnpublishedCheat?): Call<JsonObject> {
        return restRepository!!.deleteUnpublishedCheat(unpublishedCheat, member)
    }

    //    public void deleteUnpublishedCheatFromRepository(UnpublishedCheat unpublishedCheat) {
    //        restRepository.deleteUnpublishedCheat(unpublishedCheat, member);
    //    }

    init {
        settings = application.getSharedPreferences(Konstanten.PREFERENCES_FILE, 0)
        member = Gson().fromJson(
            settings.getString(
                Konstanten.MEMBER_OBJECT,
                null
            ), Member::class.java
        )
        restRepository = RestRepository(getApplication())
    }

}