package com.cheatdatabase.activity

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.applovin.sdk.AppLovinSdk
import com.cheatdatabase.R
import com.cheatdatabase.data.model.MyCheatsCount
import com.cheatdatabase.databinding.ActivityMainBinding
import com.cheatdatabase.dialogs.RateAppDialog
import com.cheatdatabase.fragments.MyCheatsViewModel
import com.cheatdatabase.helpers.Konstanten
import com.cheatdatabase.helpers.Tools
import com.cheatdatabase.helpers.TrackingUtils
import com.cheatdatabase.search.SearchSuggestionProvider
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.navigation.NavigationView
import com.inmobi.sdk.InMobiSdk
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    @Inject
    lateinit var rateAppDialog: RateAppDialog

    @Inject
    lateinit var tools: Tools

    private val viewModel: MyCheatsViewModel by viewModels()

    private lateinit var navController: NavController
    private lateinit var searchManager: SearchManager
    private lateinit var searchView: SearchView

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    val resultContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
            if (result?.resultCode == Konstanten.LOGIN_SUCCESS_RETURN_CODE) {
                tools.showSnackbar(drawerLayout, getString(R.string.login_ok))
            } else if (result?.resultCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
                tools.showSnackbar(drawerLayout, getString(R.string.register_thanks))
            }

            viewModel.getMyCheatsCount(tools.member)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init()

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbarLayout.toolbar)

        drawerLayout = binding.drawerLayout
        navigationView = binding.navigationView

        viewModel.myCheats.observe(this) { myCheats ->
            updateMyCheatsDrawerNavigationItemCount(myCheats)
        }

        // Navigation Component
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)

        navigationView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val rateAppItem = navigationView.menu.findItem(R.id.nav_rate)
        rateAppItem.setOnMenuItemClickListener {
            rateAppDialog.show(
                navController.createDeepLink().setDestination(R.id.nav_contact)
                    .createPendingIntent()
            )
            true
        }
    }

    private fun init() {
        // Please make sure to set the mediation provider value to "max" to ensure proper functionality
        AppLovinSdk.getInstance(this).mediationProvider = "max"
        AppLovinSdk.getInstance(this).initializeSdk {
            // AppLovin SDK is initialized, start loading ads
        }

        // 0 = OK
        val isGooglePlayServicesAvailable =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        TrackingUtils.getInstance().init(this)

        val consentObject = JSONObject()
        try {
            // Provide correct consent value to sdk which is obtained by User
            consentObject.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE, true)
            // Provide 0 if GDPR is not applicable and 1 if applicable
            consentObject.put("gdpr", "0")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        InMobiSdk.init(this, Konstanten.INMOBI_APP_ID, consentObject) {
            if (null != it) {
                Log.e(TAG, "InMobi Init failed - ${it.message}")
            } else {
                Log.d(TAG, "InMobi Init Successful")
            }
        }
        InMobiSdk.setAgeGroup(InMobiSdk.AgeGroup.BETWEEN_18_AND_24)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navigateUp(navController, drawerLayout)
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
        return when (item.itemId) {
            R.id.action_clear_search_history -> {
                val suggestions = SearchRecentSuggestions(
                    this,
                    SearchSuggestionProvider.AUTHORITY,
                    SearchSuggestionProvider.MODE
                )
                suggestions.clearHistory()
                Toast.makeText(this, R.string.search_history_cleared, Toast.LENGTH_LONG).show()
                true
            }
            R.id.action_login -> {
                resultContract.launch(Intent(this, LoginActivity::class.java))
                true
            }
            R.id.action_logout -> {
                tools.logout()
                tools.showSnackbar(drawerLayout, getString(R.string.logout_ok))
                invalidateOptionsMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateMyCheatsDrawerNavigationItemCount(myCheatsCount: MyCheatsCount?) {
        val menu = navigationView.menu
        val navMyCheats = menu.findItem(R.id.nav_my_cheats)
        val myCheatsNavDrawerCounter =
            navMyCheats.actionView.findViewById<TextView>(R.id.nav_drawer_item_counter)

        if (myCheatsCount != null) {
            val allUnpublishedCheats = myCheatsCount.uncheckedCheats + myCheatsCount.rejectedCheats
            if (allUnpublishedCheats > 0) {
                myCheatsNavDrawerCounter.text =
                    getString(R.string.braces_with_text_in_the_middle, allUnpublishedCheats)
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