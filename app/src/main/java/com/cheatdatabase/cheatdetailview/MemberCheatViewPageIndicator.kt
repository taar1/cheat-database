package com.cheatdatabase.cheatdetailview

import android.app.SearchManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import com.applovin.adview.AppLovinAdView
import com.cheatdatabase.R
import com.cheatdatabase.activity.CheatForumActivity
import com.cheatdatabase.activity.LoginActivity
import com.cheatdatabase.activity.SubmitCheatFormActivity
import com.cheatdatabase.callbacks.GenericCallback
import com.cheatdatabase.callbacks.OnCheatRated
import com.cheatdatabase.data.model.Cheat
import com.cheatdatabase.data.model.Game
import com.cheatdatabase.databinding.ActivityCheatviewPagerBinding
import com.cheatdatabase.dialogs.CheatMetaDialog
import com.cheatdatabase.dialogs.RateCheatMaterialDialog
import com.cheatdatabase.dialogs.ReportCheatMaterialDialog
import com.cheatdatabase.events.CheatRatingFinishedEvent
import com.cheatdatabase.helpers.Konstanten
import com.cheatdatabase.helpers.Reachability
import com.cheatdatabase.helpers.Tools
import com.cheatdatabase.rest.RestApi
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Swipe through cheats horizontally submitted by members.
 *
 * @author Dominik Erbsland
 */
@AndroidEntryPoint
class MemberCheatViewPageIndicator : AppCompatActivity(), GenericCallback, OnCheatRated {

    @Inject
    lateinit var tools: Tools

    @Inject
    lateinit var restApi: RestApi

    lateinit var outerLayout: ConstraintLayout
    lateinit var mToolbar: Toolbar
    lateinit var appLovinAdView: AppLovinAdView
    lateinit var viewPager: ViewPager2

    private var pageSelected = 0
    private var cheatList: List<Cheat>? = null
    private lateinit var visibleCheat: Cheat
    private var gameObj: Game = Game()
    private var activePage = 0

    private lateinit var settings: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var binding: ActivityCheatviewPagerBinding

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
            activityResult.resultCode == FORUM_POST_ADDED_REQUEST -> {
                val newForumCount = activityResult.data!!
                    .getIntExtra("newForumCount", visibleCheat.forumCount)
                visibleCheat.forumCount = newForumCount
            }
        }
        invalidateOptionsMenu()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCheatviewPagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindViews()
        init()

        val type = object : TypeToken<List<Cheat?>?>() {}.type
        cheatList = Gson().fromJson(
            tools.sharedPreferences.getString(
                Konstanten.PREFERENCES_TEMP_CHEAT_ARRAY_OBJECT_VIEW,
                null
            ), type
        )
        if (cheatList.isNullOrEmpty()) {
            cheatList = intent.getParcelableArrayListExtra("cheatList")
        }
        if (cheatList.isNullOrEmpty()) {
            handleNullPointerException()
        } else {
            pageSelected = intent.getIntExtra("selectedPage", 0)
            activePage = pageSelected
            try {
                visibleCheat = cheatList!![pageSelected]
                gameObj = visibleCheat.game
                gameObj.systemId = visibleCheat.system.systemId
                gameObj.systemName = visibleCheat.system.systemName
                supportActionBar!!.title = visibleCheat.game.gameName
                supportActionBar!!.subtitle = visibleCheat.system.systemName
                initialisePaging()
            } catch (e: NullPointerException) {
                handleNullPointerException()
            }
        }

        TabLayoutMediator(binding.tabLayout, viewPager) { tab, position ->
            tab.text = cheatList!![position].cheatTitle
        }.attach()
    }

    private fun handleNullPointerException() {
        Toast.makeText(this, R.string.err_somethings_wrong, Toast.LENGTH_LONG).show()
        finish()
    }

    private fun bindViews() {
        outerLayout = binding.outerLayout
        mToolbar = binding.toolbar
        appLovinAdView = binding.adContainer
        viewPager = binding.pager
    }

    private fun init() {
        settings = tools.sharedPreferences
        editor = tools.preferencesEditor

        mToolbar = tools.initToolbarBase(this, mToolbar)
        appLovinAdView.loadNextAd()
    }

    private fun initialisePaging() {
        viewPager.adapter = MemberCheatViewFragmentAdapter(this, cheatList!!)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // Save selected page
                tools.putInt(Konstanten.PREFERENCES_PAGE_SELECTED, position)
                activePage = position
                try {
                    visibleCheat = cheatList!![position]
                    invalidateOptionsMenu()
                    gameObj = visibleCheat.game
                    gameObj.systemId = visibleCheat.system.systemId
                    gameObj.systemName = visibleCheat.system.systemName
                    supportActionBar!!.title = visibleCheat.game.gameName
                    supportActionBar!!.subtitle = visibleCheat.system.systemName
                } catch (e: Exception) {
                    Log.e(TAG, e.message!!)
                    Toast.makeText(
                        this@MemberCheatViewPageIndicator,
                        R.string.err_somethings_wrong,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

        viewPager.currentItem = pageSelected

        binding.addNewCheatButton.setOnClickListener {
            val intent =
                Intent(this@MemberCheatViewPageIndicator, SubmitCheatFormActivity::class.java)
            intent.putExtra("gameObj", gameObj)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (visibleCheat != null && visibleCheat.memberRating > 0) {
            menuInflater.inflate(R.menu.handset_cheatview_rating_on_menu, menu)
        } else {
            menuInflater.inflate(R.menu.handset_cheatview_rating_off_menu, menu)
        }
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val explicitIntent: Intent
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_submit_cheat -> {
                explicitIntent =
                    Intent(this@MemberCheatViewPageIndicator, SubmitCheatFormActivity::class.java)
                explicitIntent.putExtra("gameObj", gameObj)
                startActivity(explicitIntent)
                true
            }
            R.id.action_rate -> {
                showRatingDialog()
                true
            }
            R.id.action_forum -> {
                explicitIntent =
                    Intent(this@MemberCheatViewPageIndicator, CheatForumActivity::class.java)
                explicitIntent.putExtra("cheatId", visibleCheat.cheatId)

                startActivity(explicitIntent)
                true
            }
            R.id.action_add_to_favorites -> {
                tools.showSnackbar(outerLayout, getString(R.string.favorite_adding))
                var memberId = 0
                if (tools.member != null) {
                    memberId = tools.member.mid
                }
                tools.addFavorite(visibleCheat, memberId, this)
                true
            }
            R.id.action_report -> {
                showReportDialog()
                true
            }
            R.id.action_metainfo -> {
                CheatMetaDialog(
                    this@MemberCheatViewPageIndicator,
                    visibleCheat,
                    outerLayout,
                    tools
                ).show()
                true
            }
            R.id.action_login -> {
                resultContract.launch(
                    Intent(
                        this@MemberCheatViewPageIndicator,
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
            R.id.action_share -> {
                tools.shareCheat(visibleCheat)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showReportDialog() {
        if (tools.member == null || tools.member.mid == 0) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_SHORT).show()
        } else {
            ReportCheatMaterialDialog(this, visibleCheat, tools.member, outerLayout, tools)
        }
    }

    private fun showRatingDialog() {
        if (tools.member == null || tools.member.mid == 0) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_LONG).show()
        } else {
            RateCheatMaterialDialog(this, visibleCheat, outerLayout, tools, restApi, this)
        }
    }

    override fun onCheatRated(cheatRatingFinishedEvent: CheatRatingFinishedEvent) {
        visibleCheat.memberRating = cheatRatingFinishedEvent.rating.toFloat()
        cheatList!![activePage].memberRating = cheatRatingFinishedEvent.rating.toFloat()
        invalidateOptionsMenu()
    }

    fun setRating(position: Int, rating: Float) {
        cheatList!![position].memberRating = rating
        invalidateOptionsMenu()
    }

    public override fun onResume() {
        super.onResume()
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(this)
        }
        invalidateOptionsMenu()
    }

    override fun onStop() {
        Reachability.unregister(this)
        super.onStop()
    }

    override fun success() {
        tools.showSnackbar(outerLayout, getString(R.string.add_favorite_ok))
    }

    override fun fail(e: Exception) {
        tools.showSnackbar(outerLayout, getString(R.string.error_adding_favorite))
    }

    companion object {
        private const val TAG = "MemberCheatViewPageIndi"
        const val FORUM_POST_ADDED_REQUEST = 176
    }
}