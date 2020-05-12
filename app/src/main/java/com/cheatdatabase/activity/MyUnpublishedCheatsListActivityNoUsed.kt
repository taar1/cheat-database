package com.cheatdatabase.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cheatdatabase.R
import com.cheatdatabase.activity.ui.myunpublishedcheatslist.MyUnpublishedCheatsListFragmentNotUsed

class MyUnpublishedCheatsListActivityNoUsed : AppCompatActivity() {

    //    @Inject
//    var retrofit: Retrofit? = null
//
//    private var apiService: RestApi? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_unpublished_cheats_list_activity)

//        // Dagger start
//        (application as CheatDatabaseApplication).networkComponent.inject(this)
//        apiService = retrofit!!.create(RestApi::class.java)
//        // Dagger end

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MyUnpublishedCheatsListFragmentNotUsed.newInstance())
                    .commitNow()
        }
    }
}
