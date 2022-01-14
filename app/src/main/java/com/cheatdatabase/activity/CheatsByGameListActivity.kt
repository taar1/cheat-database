package com.cheatdatabase.activity

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.applovin.adview.AppLovinAdView
import com.cheatdatabase.CheatDatabaseApplication
import com.cheatdatabase.R
import com.cheatdatabase.activity.SubmitCheatFormActivity
import com.cheatdatabase.adapters.CheatsByGameRecycleListViewAdapter
import com.cheatdatabase.cheatdetailview.CheatViewPageIndicatorActivity
import com.cheatdatabase.data.RoomCheatDatabase
import com.cheatdatabase.data.model.Cheat
import com.cheatdatabase.data.model.Game
import com.cheatdatabase.databinding.ActivityCheatListBinding
import com.cheatdatabase.helpers.Konstanten
import com.cheatdatabase.helpers.Reachability
import com.cheatdatabase.helpers.Tools
import com.cheatdatabase.listeners.OnCheatListItemSelectedListener
import com.cheatdatabase.rest.RestApi
import com.cheatdatabase.widgets.DividerDecoration
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import dagger.hilt.android.AndroidEntryPoint
import needle.Needle
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CheatsByGameListActivity : AppCompatActivity(), OnCheatListItemSelectedListener {

    @Inject
    lateinit var tools: Tools

    @Inject
    lateinit var restApi: RestApi

    lateinit var outerLayout: ConstraintLayout
    lateinit var recyclerView: FastScrollRecyclerView
    lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    lateinit var mToolbar: Toolbar
    lateinit var mEmptyView: TextView
    lateinit var appLovinAdView: AppLovinAdView

    private var cheatList: ArrayList<Cheat>? = null
    private var isAchievementsEnabled = false
    private var cheatDatabaseApplication: CheatDatabaseApplication? = null
    private var cheatsByGameRecycleListViewAdapter: CheatsByGameRecycleListViewAdapter? = null
    private var gameObj: Game? = null

    private lateinit var binding: ActivityCheatListBinding

    private val resultContract = registerForActivityResult(
        StartActivityForResult(),
        activityResultRegistry
    ) { activityResult: ActivityResult ->
        val intentReturnCode = activityResult.resultCode
        when {
            intentReturnCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE -> {
                tools.showSnackbar(outerLayout, getString(R.string.register_thanks))
            }
            intentReturnCode == Konstanten.LOGIN_SUCCESS_RETURN_CODE -> {
                tools.showSnackbar(outerLayout, getString(R.string.login_ok))
            }
            activityResult.resultCode == Konstanten.RECOVER_PASSWORD_ATTEMPT -> {
                tools.showSnackbar(outerLayout, getString(R.string.recover_login_success))
            }
        }
        invalidateOptionsMenu()
    }

    override fun onStart() {
        super.onStart()
        if (Reachability.reachability.isReachable) {
            loadCheats(false)
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCheatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gameObj = intent.getParcelableExtra("gameObj")
        if (gameObj == null) {
            Toast.makeText(this, R.string.err_somethings_wrong, Toast.LENGTH_LONG).show()
            finish()
        } else {
            title = if (gameObj!!.gameName != null) gameObj!!.gameName else ""

            bindViews()
            init()

            mSwipeRefreshLayout.isRefreshing = true
            mSwipeRefreshLayout.setOnRefreshListener { loadCheats(true) }
            cheatsByGameRecycleListViewAdapter =
                CheatsByGameRecycleListViewAdapter(this, tools, this)
            supportActionBar!!.title = if (gameObj!!.gameName != null) gameObj!!.gameName else ""
            supportActionBar!!.subtitle =
                if (gameObj!!.systemName != null) gameObj!!.systemName else ""

            with(recyclerView) {
                adapter = cheatsByGameRecycleListViewAdapter
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                addItemDecoration(DividerDecoration(context))
                itemAnimator!!.removeDuration = 50
                setHasFixedSize(true)
                showScrollbar()
            }
        }
    }

    private fun bindViews() {
        outerLayout = binding.outerLayout
        recyclerView = binding.includeRecyclerview.myRecyclerView
        mSwipeRefreshLayout = binding.swipeRefreshLayout
        mToolbar = binding.includeToolbar.toolbar
        mEmptyView = binding.itemListEmptyView
        appLovinAdView = binding.includeApplovin.adContainer

        binding.addNewCheatButton.setOnClickListener {
            addNewCheat()
        }
    }

    private fun init() {
        cheatDatabaseApplication = CheatDatabaseApplication.getCurrentAppInstance()
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        appLovinAdView.loadNextAd()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        menuInflater.inflate(R.menu.cheats_by_game_menu, menu)
        if (tools.member != null) {
            menuInflater.inflate(R.menu.signout_menu, menu)
        } else {
            menuInflater.inflate(R.menu.signin_menu, menu)
        }

        // Search
        menuInflater.inflate(R.menu.search_menu, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        if (tools.member == null) {
            menuInflater.inflate(R.menu.signin_menu, menu)
        } else {
            menuInflater.inflate(R.menu.signout_menu, menu)
        }
        menuInflater.inflate(R.menu.cheats_by_game_menu, menu)
        menuInflater.inflate(R.menu.search_menu, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                tools.removeValue(Konstanten.PREFERENCES_TEMP_GAME_OBJECT_VIEW)
                finish()
                true
            }
            R.id.action_add_to_favorites -> {
                tools.showSnackbar(outerLayout, getString(R.string.favorite_adding))
                addCheatsToFavoritesTask()
                true
            }
            R.id.action_submit_cheat -> {
                val explicitIntent =
                    Intent(this@CheatsByGameListActivity, SubmitCheatFormActivity::class.java)
                explicitIntent.putExtra("gameObj", gameObj)
                startActivity(explicitIntent)
                true
            }
            R.id.action_login -> {
                resultContract.launch(
                    Intent(
                        this@CheatsByGameListActivity,
                        LoginActivity::class.java
                    )
                )
                true
            }
            R.id.action_logout -> {
                tools.logout()
                tools.showSnackbar(outerLayout, getString(R.string.logout_ok))
                invalidateOptionsMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadCheats(forceLoadOnline: Boolean) {
        cheatList = ArrayList()
        val cachedCheatsCollection: ArrayList<Cheat>?
        val cheatsFound: ArrayList<Cheat>
        var isCached = false
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        isAchievementsEnabled = prefs.getBoolean("enable_achievements", true)
        val achievementsEnabled: String = if (isAchievementsEnabled) {
            Konstanten.ACHIEVEMENTS
        } else {
            Konstanten.NO_ACHIEVEMENTS
        }
        val cheatsByGameInCache = cheatDatabaseApplication!!.cheatsByGameCached
        var cheatListTree: TreeMap<*, *>? = null
        if (cheatsByGameInCache.containsKey(gameObj!!.gameId.toString())) {
            cheatListTree = cheatsByGameInCache[gameObj!!.gameId.toString()]
            if (cheatListTree != null && cheatListTree.containsKey(achievementsEnabled)) {
                cachedCheatsCollection = cheatListTree[achievementsEnabled] as ArrayList<Cheat>?
                if (cachedCheatsCollection != null && cachedCheatsCollection.size > 0) {
                    cheatsFound = cachedCheatsCollection
                    gameObj!!.setCheatList(cheatsFound)
                    cheatList = cheatsFound
                    isCached = true
                }
            }
        }
        if (!isCached || forceLoadOnline) {
            var memberId = 0
            if (tools.member != null) {
                memberId = tools.member.mid
            }
            val finalCheatListTree = cheatListTree
            val call = restApi.getCheatsAndRatings(
                gameObj!!.gameId, memberId, if (isAchievementsEnabled) 1 else 0
            )
            call.enqueue(object : Callback<List<Cheat>> {
                override fun onResponse(
                    cheats: Call<List<Cheat>>,
                    response: Response<List<Cheat>>
                ) {
                    cheatList = response.body() as ArrayList<Cheat>
                    gameObj!!.setCheatList(cheatList)
                    val updatedCheatListForCache = TreeMap<String, List<Cheat>?>()
                    updatedCheatListForCache[achievementsEnabled] = cheatList
                    val checkWhichSubKey: String = if (achievementsEnabled.equals(
                            Konstanten.ACHIEVEMENTS,
                            ignoreCase = true
                        )
                    ) {
                        Konstanten.NO_ACHIEVEMENTS
                    } else {
                        Konstanten.ACHIEVEMENTS
                    }
                    if (finalCheatListTree != null && finalCheatListTree.containsKey(
                            checkWhichSubKey
                        )
                    ) {
                        val existingGamesInCache =
                            finalCheatListTree[checkWhichSubKey] as List<Cheat>?
                        updatedCheatListForCache[checkWhichSubKey] = existingGamesInCache
                    }
                    cheatsByGameInCache[gameObj!!.gameId.toString()] = updatedCheatListForCache
                    cheatDatabaseApplication!!.cheatsByGameCached = cheatsByGameInCache
                    updateUI()
                }

                override fun onFailure(call: Call<List<Cheat>>, e: Throwable) {
                    Log.e(TAG, "getCheatList onFailure: " + e.localizedMessage)
                    Needle.onMainThread().execute {
                        Toast.makeText(
                            this@CheatsByGameListActivity,
                            R.string.err_somethings_wrong,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })
        } else {
            updateUI()
        }
    }

    private fun updateUI() {
        if (cheatList != null && cheatList!!.size > 0) {
            cheatsByGameRecycleListViewAdapter!!.setCheatList(cheatList)
            cheatsByGameRecycleListViewAdapter!!.filterList("")
        } else {
            error()
        }
        Needle.onMainThread().execute { mSwipeRefreshLayout.isRefreshing = false }
    }

    private fun error() {
        Log.e(TAG, "Caught error: $packageName/$title")
        Needle.onMainThread().execute {
            tools.showSnackbar(
                outerLayout,
                getString(R.string.err_data_not_accessible)
            )
        }
    }

    public override fun onResume() {
        super.onResume()
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(this)
        }
    }

    override fun onStop() {
        Reachability.unregister(this)
        super.onStop()
    }

    fun addNewCheat() {
        val explicitIntent =
            Intent(this@CheatsByGameListActivity, SubmitCheatFormActivity::class.java)
        explicitIntent.putExtra("gameObj", gameObj)
        startActivity(explicitIntent)
    }

    private fun addCheatsToFavoritesTask() {
        val dao = RoomCheatDatabase.getDatabase(this).favoriteDao()
        val call = restApi.getCheatsByGameId(
            gameObj!!.gameId, if (isAchievementsEnabled) 1 else 0
        )
        call.enqueue(object : Callback<List<Cheat>> {
            override fun onResponse(cheats: Call<List<Cheat>>, response: Response<List<Cheat>>) {
                val cheatList = response.body()!!
                for (cheat in cheatList) {
                    if (cheat.hasScreenshots()) {
                        // TODO FIXME: currently it ignores success/fail of saving screenshots to SD card...
                        // TODO FIXME: currently it ignores success/fail of saving screenshots to SD card...
                        tools.saveScreenshotsToSdCard(cheat, null)
                    }

                    Needle.onBackgroundThread().execute {
                        var memberId = 0
                        if (tools.member != null) {
                            memberId = tools.member.mid
                        }
                        dao.insert(cheat.toFavoriteCheatModel(memberId))
                    }
                }
                tools.showSnackbar(outerLayout, getString(R.string.add_favorites_ok))
            }

            override fun onFailure(call: Call<List<Cheat>>, e: Throwable) {
                Log.e(TAG, "insertFavoriteCheats onFailure: " + e.localizedMessage)
                tools.showSnackbar(outerLayout, getString(R.string.error_adding_favorites))
            }
        })
    }


    override fun onCheatListItemSelected(cheat: Cheat, position: Int) {
        if (Reachability.reachability.isReachable) {
            tools.putInt(Konstanten.PREFERENCES_PAGE_SELECTED, position)

            // Using local Preferences to pass data for large game objects
            // (instead of intent) such as Pokemon
            val explicitIntent =
                Intent(this@CheatsByGameListActivity, CheatViewPageIndicatorActivity::class.java)
            explicitIntent.putExtra("gameObj", gameObj)
            explicitIntent.putExtra("selectedPage", position)
            explicitIntent.putExtra("layoutResourceId", R.layout.activity_cheatview_pager)
            startActivity(explicitIntent)
        } else {
            tools.showSnackbar(outerLayout, getString(R.string.no_internet))
        }
    }

    companion object {
        private const val TAG = "CheatsByGameListActivit"
    }
}