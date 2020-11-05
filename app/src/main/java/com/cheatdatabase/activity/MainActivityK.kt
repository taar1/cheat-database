package com.cheatdatabase.activity

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.cheatdatabase.R
import com.cheatdatabase.data.model.MyCheatsCount
import com.cheatdatabase.databinding.ActivityMainKBinding
import com.cheatdatabase.dialogs.RateAppDialog
import com.cheatdatabase.fragments.MyCheatsViewModel
import com.cheatdatabase.helpers.Konstanten
import com.cheatdatabase.helpers.Tools
import com.cheatdatabase.helpers.TrackingUtils
import com.cheatdatabase.search.SearchSuggestionProvider
import com.facebook.ads.AdView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.inmobi.ads.InMobiBanner
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main_k.view.*
import kotlinx.android.synthetic.main.toolbar.view.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivityK : AppCompatActivity() {
    private val TAG = "MainActivityK"

    @Inject
    lateinit var rateAppDialog: RateAppDialog

    @Inject
    lateinit var tools: Tools

    val viewModel: MyCheatsViewModel by viewModels()

    private lateinit var navigationView: NavigationView
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var mixedBannerContainer: LinearLayout
    private lateinit var bannerContainerFacebook: LinearLayout
    private lateinit var bannerContainerInmobi: LinearLayout
    private lateinit var inMobiBanner: InMobiBanner
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var searchManager: SearchManager
    private lateinit var searchView: SearchView

    private lateinit var adView: AdView

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var viewBinding: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainKBinding.inflate(layoutInflater)
        viewBinding = binding.root

        setContentView(viewBinding)
        setSupportActionBar(viewBinding.toolbar)

        init()
        bindViews()

        viewModel.myCheats.observe(this, { myCheats ->
            updateMyCheatsDrawerNavigationItemCount(myCheats)
        })

        // Navigation Component
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_k) as NavHostFragment
        navController = navHostFragment.navController
        drawerLayout = binding.drawerLayout

        appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawerLayout)

        navigationView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val rateAppItem = navigationView.menu.findItem(R.id.nav_rate)
        rateAppItem.setOnMenuItemClickListener {
            rateAppDialog.show(navController.createDeepLink().setDestination(R.id.nav_contact).createPendingIntent())
            true
        }
    }

    private fun init() {
        TrackingUtils.getInstance().init(this)
    }

    private fun bindViews() {
        navigationView = viewBinding.nav_view
//        inMobiBanner = viewBinding.inmobi_banner
//        bannerContainerFacebook = viewBinding.banner_container_facebook
//        bannerContainerInmobi = viewBinding.banner_container_inmobi
//        mixedBannerContainer = viewBinding.mixed_banner_container
        //floatingActionButton = viewBinding.add_new_cheat_button
    }

    override fun onSupportNavigateUp(): Boolean {
        return navigateUp(navController, drawerLayout)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Konstanten.LOGIN_SUCCESS_RETURN_CODE) {
            Toast.makeText(this, R.string.login_ok, Toast.LENGTH_LONG).show()
        } else if (resultCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
            Toast.makeText(this, R.string.register_thanks, Toast.LENGTH_LONG).show()
        }

        viewModel.getMyCheatsCount(tools.member)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        populateOptionsMenu(menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        populateOptionsMenu(menu)
        return super.onPrepareOptionsMenu(menu)
    }

    private fun populateOptionsMenu(menu: Menu) {
        menu.clear()

        if (tools.member != null) {
            menuInflater.inflate(R.menu.signout_menu, menu)
        } else {
            menuInflater.inflate(R.menu.signin_menu, menu)
        }
        menuInflater.inflate(R.menu.clear_search_history_menu, menu)

        // Search
        // Associate searchable configuration with the SearchView
        menuInflater.inflate(R.menu.search_menu, menu)
        searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action buttons
        return when (item.itemId) {
            R.id.action_clear_search_history -> {
                val suggestions = SearchRecentSuggestions(this, SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE)
                suggestions.clearHistory()
                Toast.makeText(this, R.string.search_history_cleared, Toast.LENGTH_LONG).show()
                true
            }
            R.id.action_login -> {
                val loginIntent = Intent(this, LoginActivity::class.java)
                startActivityForResult(loginIntent, Konstanten.LOGIN_REGISTER_OK_RETURN_CODE)
                true
            }
            R.id.action_logout -> {
                tools.logout()
                invalidateOptionsMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    /**
     * Display either InMobi or Facebook Audience Network banner (randomly)
     */
//    private fun prepareAdBanner() {
//        if (Random.nextInt(0, 1) == 0) {
//            Log.d(TAG, "Banner: Using InMobi Version: " + InMobiSdk.getVersion())
//            inMobiBanner.setEnableAutoRefresh(true)
//            inMobiBanner.load(this)
//            bannerContainerFacebook.visibility = View.GONE
//            bannerContainerInmobi.visibility = View.VISIBLE
//        } else {
//            Log.d(TAG, "Banner: Using Facebook Audience Network")
//            bannerContainerInmobi.visibility = View.GONE
//            bannerContainerFacebook.visibility = View.VISIBLE
//            adView = AdView(this, Konstanten.FACEBOOK_AUDIENCE_NETWORK_NATIVE_BANNER_ID, AdSize.BANNER_HEIGHT_50)
//            bannerContainerFacebook.addView(adView)
//            adView.loadAd()
//        }
//    }

    private fun updateMyCheatsDrawerNavigationItemCount(myCheatsCount: MyCheatsCount?) {
        val menu = navigationView.menu
        val navMyCheats = menu.findItem(R.id.nav_my_cheats)
        val myCheatsNavDrawerCounter = navMyCheats.actionView.findViewById<TextView>(R.id.nav_drawer_item_counter)

        if (myCheatsCount != null) {
            val allUnpublishedCheats = myCheatsCount.uncheckedCheats + myCheatsCount.rejectedCheats
            if (allUnpublishedCheats > 0) {
                myCheatsNavDrawerCounter.text = getString(R.string.braces_with_text_in_the_middle, allUnpublishedCheats)
            }
        } else {
            myCheatsNavDrawerCounter.text = ""
        }
    }

    private fun testCrash() {
        throw RuntimeException("This is a crash")
    }

    override fun onResume() {
        super.onResume()
        viewModel.getMyCheatsCount(tools.member)
    }
}