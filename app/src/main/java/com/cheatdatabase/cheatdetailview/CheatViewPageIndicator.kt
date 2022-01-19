package com.cheatdatabase.cheatdetailview

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
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
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
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
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import net.lucode.hackware.magicindicator.MagicIndicator
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView
import javax.inject.Inject

/**
 * Swipe through cheats horizontally with this CheatViewPageIndicatorActivity.
 *
 * @author Dominik Erbsland
 */
@AndroidEntryPoint
class CheatViewPageIndicator : AppCompatActivity(), GenericCallback,
    OnCheatRated {

    @Inject
    lateinit var tools: Tools

    @Inject
    lateinit var restApi: RestApi

    lateinit var outerLayout: ConstraintLayout
    lateinit var mToolbar: Toolbar
    lateinit var appLovinAdView: AppLovinAdView
    lateinit var mPager: ViewPager
    lateinit var magicIndicator: MagicIndicator

    private var pageSelected = 0
    private var gameObj: Game? = null
    private var cheatList: ArrayList<Cheat>? = null
    private var visibleCheat: Cheat? = null
    private var mAdapter: CheatViewFragmentAdapter? = null
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
                    .getIntExtra("newForumCount", visibleCheat!!.forumCount)
                visibleCheat!!.forumCount = newForumCount
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

        // Bei grossen Game Objekten (wie Pokemon Fire Red) muss das Objekt
        // aus dem SharedPreferences geholt werden (ansonsten Absturz)
        gameObj = intent.getParcelableExtra("gameObj")
        if (gameObj == null) {
            gameObj = Gson().fromJson(
                settings.getString(
                    Konstanten.PREFERENCES_TEMP_GAME_OBJECT_VIEW,
                    null
                ), Game::class.java
            )
        }
        editor.putString(Konstanten.PREFERENCES_TEMP_GAME_OBJECT_VIEW, Gson().toJson(gameObj))
            .apply()

        pageSelected = intent.getIntExtra("selectedPage", 0)
        activePage = pageSelected

        if (gameObj == null) {
            finish()
        } else {
            for (cheat in gameObj!!.cheatList) {
                if (cheatList == null) {
                    cheatList = ArrayList()
                }
                cheatList!!.add(cheat)
            }
            if (cheatList == null || cheatList!!.size < 1) {
                onBackPressed()
            }
            visibleCheat = cheatList!![pageSelected]
            supportActionBar!!.title = if (gameObj!!.gameName != null) gameObj!!.gameName else ""
            supportActionBar!!.subtitle =
                if (gameObj!!.systemName != null) gameObj!!.systemName else ""
            initialisePaging()
        }
    }

    private fun bindViews() {
        outerLayout = binding.outerLayout
        mToolbar = binding.toolbar
        appLovinAdView = binding.adContainer
        mPager = binding.pager
        magicIndicator = binding.magicIndicator
    }

    private fun init() {
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(this)
        }

        settings = tools.sharedPreferences
        editor = tools.preferencesEditor

        cheatList = ArrayList()
        appLovinAdView.loadNextAd()
        mToolbar = tools.initToolbarBase(this, mToolbar)
    }

    private fun initialisePaging() {
        try {
            mAdapter = CheatViewFragmentAdapter(supportFragmentManager, gameObj!!, cheatList!!)
            mPager.adapter = mAdapter
            mPager.addOnPageChangeListener(object : OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    // Save the last selected page
                    editor.putInt(Konstanten.PREFERENCES_PAGE_SELECTED, position).apply()
                    activePage = position
                    try {
                        visibleCheat = cheatList!![position]
                        invalidateOptionsMenu()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@CheatViewPageIndicator,
                            R.string.err_somethings_wrong,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })
            val commonNavigator = CommonNavigator(this)
            commonNavigator.isSkimOver = true
            commonNavigator.adapter = object : CommonNavigatorAdapter() {
                override fun getCount(): Int {
                    return if (cheatList == null) 0 else cheatList!!.size
                }

                override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                    val clipPagerTitleView: SimplePagerTitleView =
                        ColorTransitionPagerTitleView(context)
                    clipPagerTitleView.text = cheatList!![index].cheatTitle
                    clipPagerTitleView.normalColor =
                        Color.parseColor("#88ffffff") // White transparent
                    clipPagerTitleView.selectedColor = Color.WHITE
                    clipPagerTitleView.setOnClickListener { mPager.currentItem = index }
                    return clipPagerTitleView
                }

                override fun getIndicator(context: Context): IPagerIndicator {
                    val indicator = LinePagerIndicator(context)
                    indicator.mode = LinePagerIndicator.MODE_EXACTLY
                    indicator.yOffset = UIUtil.dip2px(context, 3.0).toFloat()
                    indicator.setColors(Color.WHITE)
                    return indicator
                }
            }
            magicIndicator.navigator = commonNavigator
            ViewPagerHelper.bind(magicIndicator, mPager)
            mPager.currentItem = pageSelected

            binding.addNewCheatButton.setOnClickListener {
                val intent =
                    Intent(this@CheatViewPageIndicator, SubmitCheatFormActivity::class.java)
                intent.putExtra("gameObj", gameObj)
                startActivity(intent)
            }
        } catch (e2: Exception) {
            Log.e(TAG, "ERROR: " + packageName + "/" + title + "... " + e2.message)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (visibleCheat != null && visibleCheat!!.memberRating > 0) {
            menuInflater.inflate(R.menu.handset_cheatview_rating_on_menu, menu)
        } else {
            menuInflater.inflate(R.menu.handset_cheatview_rating_off_menu, menu)
        }
        if (tools.member != null) {
            menuInflater.inflate(R.menu.signout_menu, menu)
        } else {
            menuInflater.inflate(R.menu.signin_menu, menu)
        }
        var postOrPosts = getString(R.string.forum_many_posts)
        if (visibleCheat!!.forumCount == 1) {
            postOrPosts = getString(R.string.forum_single_post)
        }
        val forumMenuItem = menu.findItem(R.id.action_forum)
        forumMenuItem.title =
            getString(R.string.forum_amount_posts, visibleCheat!!.forumCount, postOrPosts)

        // Search
        menuInflater.inflate(R.menu.search_menu, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        var postOrPosts = getString(R.string.forum_many_posts)
        if (visibleCheat!!.forumCount == 1) {
            postOrPosts = getString(R.string.forum_single_post)
        }
        val forumMenuItem = menu.findItem(R.id.action_forum)
        forumMenuItem.title =
            getString(R.string.forum_amount_posts, visibleCheat!!.forumCount, postOrPosts)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val explicitIntent: Intent
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_submit_cheat -> {
                explicitIntent =
                    Intent(this@CheatViewPageIndicator, SubmitCheatFormActivity::class.java)
                explicitIntent.putExtra("gameObj", gameObj)
                startActivity(explicitIntent)
                true
            }
            R.id.action_rate -> {
                showRatingDialog()
                true
            }
            R.id.action_forum -> {
                if (Reachability.reachability.isReachable) {
                    explicitIntent =
                        Intent(this@CheatViewPageIndicator, CheatForumActivity::class.java)
                    explicitIntent.putExtra("gameObj", gameObj)
                    explicitIntent.putExtra("cheatObj", visibleCheat)
                    resultContract.launch(explicitIntent)
                } else {
                    Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show()
                }
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
                if (Reachability.reachability.isReachable) {
                    CheatMetaDialog(
                        this@CheatViewPageIndicator,
                        visibleCheat!!,
                        outerLayout,
                        tools
                    ).show()
                } else {
                    Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.action_login -> {
                resultContract.launch(
                    Intent(
                        this@CheatViewPageIndicator,
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

    public override fun onResume() {
        super.onResume()
        invalidateOptionsMenu()
    }

    override fun onStop() {
        Reachability.unregister(this)
        super.onStop()
    }

    private fun showReportDialog() {
        if (tools.member == null || tools.member.mid == 0) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_SHORT).show()
        } else {
            ReportCheatMaterialDialog(this, visibleCheat!!, tools.member, outerLayout, tools)
        }
    }

    private fun showRatingDialog() {
        if (tools.member == null || tools.member.mid == 0) {
            Toast.makeText(this, R.string.error_login_to_rate, Toast.LENGTH_LONG).show()
        } else {
            RateCheatMaterialDialog(this, visibleCheat!!, outerLayout, tools, restApi, this)
        }
    }

    override fun success() {
        tools.showSnackbar(outerLayout, getString(R.string.add_favorite_ok))
    }

    override fun fail(e: Exception) {
        tools.showSnackbar(outerLayout, getString(R.string.error_adding_favorite))
    }

    override fun onCheatRated(cheatRatingFinishedEvent: CheatRatingFinishedEvent) {
        visibleCheat!!.memberRating = cheatRatingFinishedEvent.rating.toFloat()
        cheatList!![activePage].memberRating = cheatRatingFinishedEvent.rating.toFloat()
        invalidateOptionsMenu()
    }

    companion object {
        private const val TAG = "CheatViewPageIndicatorA"
        const val FORUM_POST_ADDED_REQUEST = 176
    }

}