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

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MyUnpublishedCheatsListFragment.newInstance(this))
                .commitNow()
        }
    }
}