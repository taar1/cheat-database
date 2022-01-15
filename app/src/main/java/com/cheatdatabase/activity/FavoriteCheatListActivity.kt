package com.cheatdatabase.activity

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.applovin.adview.AppLovinAdView
import com.cheatdatabase.R
import com.cheatdatabase.activity.SubmitCheatFormActivity
import com.cheatdatabase.adapters.CheatsByGameRecycleListViewAdapter
import com.cheatdatabase.cheatdetailview.FavoritesCheatViewPageIndicator
import com.cheatdatabase.data.RoomCheatDatabase
import com.cheatdatabase.data.dao.FavoriteCheatDao
import com.cheatdatabase.data.model.Cheat
import com.cheatdatabase.data.model.FavoriteCheatModel
import com.cheatdatabase.data.model.Game
import com.cheatdatabase.databinding.ActivityCheatListBinding
import com.cheatdatabase.helpers.Konstanten
import com.cheatdatabase.helpers.Reachability
import com.cheatdatabase.helpers.Tools
import com.cheatdatabase.listeners.OnCheatListItemSelectedListener
import com.cheatdatabase.widgets.DividerDecoration
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import dagger.hilt.android.AndroidEntryPoint
import needle.Needle
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class FavoriteCheatListActivity : AppCompatActivity(), OnCheatListItemSelectedListener {
    @Inject
    lateinit var tools: Tools

    lateinit var outerLayout: ConstraintLayout
    lateinit var recyclerView: FastScrollRecyclerView
    lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    lateinit var mToolbar: Toolbar
    lateinit var mEmptyView: TextView
    lateinit var appLovinAdView: AppLovinAdView

    private var gameObj: Game? = null
    private var cheatsByGameRecycleListViewAdapter: CheatsByGameRecycleListViewAdapter? = null
    private var lastGameObj: Game? = null
    private var visibleCheat: Cheat? = null
    private var cheatsArrayList: ArrayList<Cheat>? = null

    private var dao: FavoriteCheatDao? = null

    private lateinit var binding: ActivityCheatListBinding

    override fun onStart() {
        super.onStart()
        dao = RoomCheatDatabase.getDatabase(this).favoriteDao()
        loadCheats()
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
            mSwipeRefreshLayout.setOnRefreshListener { loadCheats() }
            cheatsByGameRecycleListViewAdapter =
                CheatsByGameRecycleListViewAdapter(this, tools, this)
            supportActionBar!!.title = gameObj!!.gameName
            supportActionBar!!.subtitle = gameObj!!.systemName

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
        appLovinAdView.loadNextAd()
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        menuInflater.inflate(R.menu.cheats_by_game_menu, menu)
        if (tools.member != null) {
            menuInflater.inflate(R.menu.signout_menu, menu)
        } else {
            menuInflater.inflate(R.menu.signin_menu, menu)
        }
        menuInflater.inflate(R.menu.handset_cheatview_rating_off_menu, menu)

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
        menuInflater.inflate(R.menu.search_menu, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadCheats() {
        cheatsArrayList = ArrayList()
        if (gameObj != null) {
            if (gameObj!!.cheatList == null || gameObj!!.cheatList.size == 0) {
                dao!!.getCheatsByGameId(gameObj!!.gameId)
                    .observe(this) { favcheats: List<FavoriteCheatModel> ->
                        for (fc in favcheats) {
                            val cheat = fc.toCheat()
                            cheat.setHasScreenshots(hasScreenshotsOnSdCard(cheat.cheatId))
                            cheatsArrayList!!.add(cheat)
                        }
                        gameObj!!.setCheatList(cheatsArrayList)
                        updateUI()
                    }
            } else {
                cheatsArrayList!!.addAll(gameObj!!.cheatList)
                updateUI()
            }
        } else {
            error()
        }
        mSwipeRefreshLayout.isRefreshing = false
    }

    private fun updateUI() {
        try {
            if (cheatsArrayList != null && cheatsArrayList!!.size > 0) {
                cheatsByGameRecycleListViewAdapter!!.setCheatList(cheatsArrayList)
                cheatsByGameRecycleListViewAdapter!!.updateCheatListWithoutAds()
            } else {
                error()
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateUI Exception:" + e.localizedMessage)
            error()
        }
        Needle.onMainThread().execute { mSwipeRefreshLayout.isRefreshing = false }
    }

    private fun hasScreenshotsOnSdCard(cheatId: Int): Boolean {
        val sdCard = applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        sdCard?.let {
            val dir = File(it.absolutePath + Konstanten.APP_PATH_SD_CARD + cheatId)
            val files = dir.listFiles()
            return !files.isNullOrEmpty()
        }
        return false
    }

    // Save the position of the last element
    override fun onSaveInstanceState(outState: Bundle) {
        //outState.putInt("position", lastPosition);
        outState.putParcelable("gameObj", lastGameObj)
        super.onSaveInstanceState(outState)
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
            Intent(this@FavoriteCheatListActivity, SubmitCheatFormActivity::class.java)
        explicitIntent.putExtra("gameObj", gameObj)
        startActivity(explicitIntent)
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

    override fun onCheatListItemSelected(cheat: Cheat, position: Int) {
        visibleCheat = cheat
        lastGameObj = cheat.game
        if (Reachability.reachability.isReachable) {
            tools.putInt(Konstanten.PREFERENCES_PAGE_SELECTED, position)

            val explicitIntent = Intent(this, FavoritesCheatViewPageIndicator::class.java)
            explicitIntent.putExtra("gameObj", gameObj)
            explicitIntent.putExtra("position", position)
            explicitIntent.putExtra("layoutResourceId", R.layout.activity_cheatview_pager)
            startActivity(explicitIntent)
        } else {
            tools.showSnackbar(outerLayout, getString(R.string.no_internet))
        }
    }

    companion object {
        private const val TAG = "FavoriteCheatListActivi"
    }
}