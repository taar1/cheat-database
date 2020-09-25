package com.cheatdatabase.activity

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.cheatdatabase.R
import com.cheatdatabase.fragments.SubmitCheatFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubmitCheatSelectGameActivity : AppCompatActivity() {

    lateinit var toolbarMenu: Menu
    lateinit var searchView: SearchView
    lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.submit_cheat_activity)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.container, SubmitCheatFragment.newInstance(this)
                )
                .commitNow()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        toolbarMenu = menu
        menu.clear()
        menuInflater.inflate(R.menu.search_menu, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        return super.onCreateOptionsMenu(menu)
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}