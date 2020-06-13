package com.cheatdatabase.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.cheatdatabase.R
import com.cheatdatabase.fragments.SubmitCheatFragment

class SubmitCheatSelectGameActivity : AppCompatActivity() {

    lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.submit_cheat_activity)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // TODO newInstance(MyUnpublishedCheatsListActivity) machen (vorbild MyUnpublishedCheatsListActivity)
        // TODO newInstance(MyUnpublishedCheatsListActivity) machen (vorbild MyUnpublishedCheatsListActivity)
        // TODO newInstance(MyUnpublishedCheatsListActivity) machen (vorbild MyUnpublishedCheatsListActivity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.container,
                    SubmitCheatFragment.newInstance()
                )
                .commitNow()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}