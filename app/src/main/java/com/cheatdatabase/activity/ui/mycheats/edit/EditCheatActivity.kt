package com.cheatdatabase.activity.ui.mycheats.edit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.cheatdatabase.R

class EditCheatActivity : AppCompatActivity() {
    private val TAG = "EditCheatActivity"

    private lateinit var viewModel: EditCheatViewModel

    lateinit var toolbar: Toolbar
//    lateinit var centeredText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_cheat_activity)

        // TODO get the intent
//        var cheat = intent.getParcelableExtra("cheat") as Cheat
//        Log.d(TAG, "XXXXX onCreate: " + cheat.cheatTitle)
//
//        toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//
//        supportActionBar?.title = cheat.gameName
//        supportActionBar?.subtitle = cheat.cheatTitle


//        centeredText = findViewById(R.id.message)
//        centeredText.text = "HALLO"


        viewModel = ViewModelProvider(this).get(EditCheatViewModel::class.java)
    }
}