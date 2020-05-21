package com.cheatdatabase.activity.ui.mycheats

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cheatdatabase.R
import com.cheatdatabase.rest.KotlinRestApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyUnpublishedCheatsListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.unpublished_cheats_activity)

        val repository = UnpublishedCheatsRepository(KotlinRestApi())

        GlobalScope.launch(Dispatchers.Main) {
            val cheats = repository.getMyUnpublishedCheats(1, "7695b843af98811d4c95c8f6a08541dd")
            Toast.makeText(
                this@MyUnpublishedCheatsListActivity,
                cheats.toString(),
                Toast.LENGTH_LONG
            ).show()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MyUnpublishedCheatsFragment.newInstance())
                .commitNow()
        }
    }
}