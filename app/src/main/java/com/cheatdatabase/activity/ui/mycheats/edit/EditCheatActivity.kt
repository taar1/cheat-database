package com.cheatdatabase.activity.ui.mycheats.edit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cheatdatabase.R

class EditCheatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_cheat_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, EditCheatFragment.newInstance())
                .commitNow()
        }
    }
}