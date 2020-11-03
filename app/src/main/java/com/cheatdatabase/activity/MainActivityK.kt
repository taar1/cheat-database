package com.cheatdatabase.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.appbrain.AppBrain
import com.cheatdatabase.R
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.data.model.MyCheatsCount
import com.cheatdatabase.data.repository.MyCheatsRepository
import com.cheatdatabase.databinding.ActivityMainKBinding
import com.cheatdatabase.dialogs.RateAppDialog
import com.cheatdatabase.helpers.*
import com.cheatdatabase.rest.RestApi
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.inmobi.ads.InMobiBanner
import com.inmobi.sdk.InMobiSdk
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main_k.view.*
import kotlinx.android.synthetic.main.adview_mixed_banner_container.view.*
import kotlinx.android.synthetic.main.toolbar.view.*
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class MainActivityK : AppCompatActivity() {

    private val TAG = "MainActivityK"

    @Inject
    lateinit var rateAppDialog: RateAppDialog

    @Inject
    lateinit var tools: Tools

    @Inject
    lateinit var restApi: RestApi

    //    private lateinit var mToolbar: Toolbar
//    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var mixedBannerContainer: LinearLayout
    private lateinit var bannerContainerFacebook: LinearLayout
    private lateinit var bannerContainerInmobi: LinearLayout
    private lateinit var inMobiBanner: InMobiBanner

    private lateinit var adView: AdView
    private lateinit var member: Member
    private var myCheatsCount: MyCheatsCount? = null
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

        prepareAdBanner()

        // Navigation Component
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_k) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawerLayout)

        navigationView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun init() {
        TrackingUtils.getInstance().init(this)
        AppBrain.init(this)
    }

    private fun bindViews() {
        navigationView = viewBinding.nav_view
        inMobiBanner = viewBinding.inmobi_banner
        bannerContainerFacebook = viewBinding.banner_container_facebook
        bannerContainerInmobi = viewBinding.banner_container_inmobi
        mixedBannerContainer = viewBinding.mixed_banner_container
        //floatingActionButton = viewBinding.add_new_cheat_button
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        member = tools.member

        if (resultCode == Konstanten.LOGIN_SUCCESS_RETURN_CODE) {
            Toast.makeText(this, R.string.login_ok, Toast.LENGTH_LONG).show()
        } else if (resultCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
            Toast.makeText(this, R.string.register_thanks, Toast.LENGTH_LONG).show()
        }

        countMyCheats()
    }


    /**
     * Display either InMobi or Facebook Audience Network banner (randomly)
     */
    private fun prepareAdBanner() {
        if (Random.nextInt(0, 1) == 0) {
            Log.d(TAG, "Banner: Using InMobi Version: " + InMobiSdk.getVersion())
            inMobiBanner.setEnableAutoRefresh(true)
            inMobiBanner.load(this)
            bannerContainerFacebook.visibility = View.GONE
            bannerContainerInmobi.visibility = View.VISIBLE
        } else {
            Log.d(TAG, "Banner: Using Facebook Audience Network")
            bannerContainerInmobi.visibility = View.GONE
            bannerContainerFacebook.visibility = View.VISIBLE
            adView = AdView(this, Konstanten.FACEBOOK_AUDIENCE_NETWORK_NATIVE_BANNER_ID, AdSize.BANNER_HEIGHT_50)
            bannerContainerFacebook.addView(adView)
            adView.loadAd()
        }
    }

    private fun updateMyCheatsDrawerNavigationItemCount() {
        val menu = navigationView.menu
        val navMyCheats = menu.findItem(R.id.nav_my_cheats)
        val myCheatsNavDrawerCounter = navMyCheats.actionView.findViewById<TextView>(R.id.nav_drawer_item_counter)

        myCheatsNavDrawerCounter.text = ""
        val allUnpublishedCheats = myCheatsCount!!.uncheckedCheats + myCheatsCount!!.rejectedCheats
        if (allUnpublishedCheats > 0) {
            myCheatsNavDrawerCounter.text = getString(R.string.braces_with_text_in_the_middle, allUnpublishedCheats)
        }

//        refreshMyCheatsFragment()
    }

//    private fun refreshMyCheatsFragment() {
//        // If you log out we are updating the text in "MyCheatsFragment" so we have to inform the fragment that the login-state has changed.
//        val myCheatsFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
//        if (myCheatsFragment != null) {
//            if (myCheatsFragment is MyCheatsFragment) {
//                val mmyCheatsFragment = myCheatsFragment
//                mmyCheatsFragment.myCheatsCount = myCheatsCount
//                mmyCheatsFragment.updateText()
//            }
//        }
//    }

    //
    //    public void showContactFormFragment() {
    //        mToolbar.setTitle(R.string.contactform_title);
    //        fragmentTransaction.addToBackStack(ContactFormFragment.class.getSimpleName());
    //
    //        fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, contactFormFragment, ContactFormFragment.class.getSimpleName()).commit();
    //
    //        mixedBannerContainer.setVisibility(View.GONE);
    //
    //        floatingActionButton.hide();
    //
    //        // Contact Form Item: #6
    //        navigationView.getMenu().getItem(6).setChecked(true);
    //        drawerLayout.closeDrawers();
    //    }
    //
    //    public void closeNagivationDrawer() {
    //        drawerLayout.closeDrawers();
    //    }

    /**
     * Loads the member's unpublished, rejected and published cheats count.
     *
     * @return UnpublishedCheatsRepositoryKotlin.MyCheatsCount
     */
    private fun countMyCheats() {
        if (member.mid != 0) {
            Coroutines.main {
                val response = MyCheatsRepository().countMyCheats(
                    member.mid,
                    AeSimpleMD5.MD5(member.password)
                )

                if (response.isSuccessful) {
                    myCheatsCount = response.body()!!
                } else {
                    myCheatsCount = null
                }

                updateMyCheatsDrawerNavigationItemCount()
            }
        }
    }

    private fun testCrash() {
        throw RuntimeException("This is a crash")
    }

    override fun onResume() {
        super.onResume()
        member = tools.member
        countMyCheats()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        AppBrain.getAds().showOfferWall(this)
        finish()
    }
}