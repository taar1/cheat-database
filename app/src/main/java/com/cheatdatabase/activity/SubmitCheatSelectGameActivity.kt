package com.cheatdatabase.activity

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.cheatdatabase.R
import com.cheatdatabase.databinding.SubmitCheatActivityBinding
import com.cheatdatabase.fragments.SubmitCheatFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SubmitCheatSelectGameActivity : AppCompatActivity() {

    lateinit var toolbarMenu: Menu
    lateinit var searchView: SearchView

    lateinit var binder: SubmitCheatActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binder = SubmitCheatActivityBinding.inflate(layoutInflater)

        setContentView(binder.root)
        setSupportActionBar(binder.includeToolbar.toolbar)

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