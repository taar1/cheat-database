package com.cheatdatabase.activity.ui.mycheats

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cheatdatabase.R

class MyUnpublishedCheatsListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.unpublished_cheats_activity)

        // TODO FIXME hier noch "home as up" in toolbar irgendwie einbauen....
        // TODO FIXME hier noch "home as up" in toolbar irgendwie einbauen....
        // TODO FIXME hier noch "home as up" in toolbar irgendwie einbauen....

//        val repository = UnpublishedCheatsRepositoryUnused(KotlinRestApi())

//        GlobalScope.launch(Dispatchers.Main) {
//            //val cheats = repository.getMyUnpublishedCheats(1, "7695b843af98811d4c95c8f6a08541dd")
//            val cheats = repository.getTopMembers()
//
////            Toast.makeText(
////                this@MyUnpublishedCheatsListActivity,
////                cheats.toString(),
////                Toast.LENGTH_LONG
////            ).show()
//        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MyUnpublishedCheatsListFragment.newInstance())
                .commitNow()
        }
    }


}