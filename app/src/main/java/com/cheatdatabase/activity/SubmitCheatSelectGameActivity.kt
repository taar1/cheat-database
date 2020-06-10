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

        // TODO eigenes activity-layout machen für cheat-submissions
        // TODO eigenes activity-layout machen für cheat-submissions
        // TODO eigenes activity-layout machen für cheat-submissions
        // TODO eigenes activity-layout machen für cheat-submissions
        setContentView(R.layout.unpublished_cheats_activity)

        // TODO "cheat-score" hinzufügen bei den "my cheats" oben im layout (coming soon feature)
        // TODO "cheat-score" hinzufügen bei den "my cheats" oben im layout (coming soon feature)
        // TODO "cheat-score" hinzufügen bei den "my cheats" oben im layout (coming soon feature)
        // TODO "cheat-score" hinzufügen bei den "my cheats" oben im layout (coming soon feature)

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