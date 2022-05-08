package com.cheatdatabase.activity.ui.mycheats

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.cheatdatabase.R
import com.cheatdatabase.databinding.UnpublishedCheatsActivityBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyUnpublishedCheatsListActivity : AppCompatActivity() {

    lateinit var toolbar: Toolbar

    lateinit var binding: UnpublishedCheatsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = UnpublishedCheatsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar = binding.includeToolbar.toolbar

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MyUnpublishedCheatsListFragment.newInstance(this))
                .commitNow()
        }
    }

    fun setToolbarTitle(title: String) {
        toolbar.title = title
    }

    fun setToolbarSubtitle(subtitle: String) {
        toolbar.subtitle = subtitle
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}