package com.cheatdatabase.cheatdetailview;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.viewpager.widget.ViewPager;

import com.cheatdatabase.R;
import com.cheatdatabase.activity.CheatForumActivity;
import com.cheatdatabase.activity.LoginActivity;
import com.cheatdatabase.activity.SubmitCheatFormActivity;
import com.cheatdatabase.callbacks.GenericCallback;
import com.cheatdatabase.data.model.Cheat;
import com.cheatdatabase.data.model.Game;
import com.cheatdatabase.dialogs.CheatMetaDialog;
import com.cheatdatabase.dialogs.RateCheatMaterialDialog;
import com.cheatdatabase.dialogs.ReportCheatMaterialDialog;
import com.cheatdatabase.events.CheatRatingFinishedEvent;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.rest.RestApi;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.hilt.android.AndroidEntryPoint;

/**
 * Swipe through cheats horizontally with this CheatViewPageIndicatorActivity.
 *
 * @author Dominik Erbsland
 * @version 1.0
 */
@AndroidEntryPoint
public class CheatViewPageIndicatorActivity extends AppCompatActivity implements GenericCallback {

    private static final String TAG = CheatViewPageIndicatorActivity.class.getSimpleName();

    @Inject
    Tools tools;

    @Inject
    RestApi restApi;

    @BindView(R.id.outer_layout)
    LinearLayout outerLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.banner_container)
    LinearLayout facebookBanner;

    public static final int FORUM_POST_ADDED_REQUEST = 176;

    private Intent intent;

    private View viewLayout;
    private int pageSelected;
    private Game gameObj;
    private ArrayList<Cheat> cheatArray;
    private Cheat visibleCheat;
    private SharedPreferences settings;
    private Editor editor;
    public AlertDialog.Builder builder;
    private CheatViewFragmentAdapter mAdapter;
    private ViewPager mPager;
    private int activePage;
    private ShareActionProvider mShare;
    private AdView adView;

    private final ActivityResultLauncher<Intent> resultContract =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), getActivityResultRegistry(), activityResult -> {
                int intentReturnCode = activityResult.getResultCode();
                if (intentReturnCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
                    tools.showSnackbar(outerLayout, getString(R.string.register_thanks));
                } else if (intentReturnCode == Konstanten.LOGIN_SUCCESS_RETURN_CODE) {
                    tools.showSnackbar(outerLayout, getString(R.string.login_ok));
                } else if (activityResult.getResultCode() == Konstanten.RECOVER_PASSWORD_ATTEMPT) {
                    tools.showSnackbar(outerLayout, getString(R.string.recover_login_success));
                } else if (activityResult.getResultCode() == FORUM_POST_ADDED_REQUEST) {
                    int newForumCount = activityResult.getData().getIntExtra("newForumCount", visibleCheat.getForumCount());
                    visibleCheat.setForumCount(newForumCount);
                }
                invalidateOptionsMenu();
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();

        LayoutInflater inflater = LayoutInflater.from(this);
        viewLayout = inflater.inflate(intent.getIntExtra("layoutResourceId", R.layout.activity_cheatview_pager), null);
        setContentView(viewLayout);
        ButterKnife.bind(this);

        init();

        // Bei grossen Game Objekten (wie Pokemon Fire Red) muss das Objekt
        // aus dem SharedPreferences geholt werden (ansonsten Absturz)
        gameObj = intent.getParcelableExtra("gameObj");
        if (gameObj == null) {
            gameObj = new Gson().fromJson(settings.getString(Konstanten.PREFERENCES_TEMP_GAME_OBJECT_VIEW, null), Game.class);
        }

        editor.putString(Konstanten.PREFERENCES_TEMP_GAME_OBJECT_VIEW, new Gson().toJson(gameObj));
        editor.apply();

        pageSelected = intent.getIntExtra("selectedPage", 0);
        activePage = pageSelected;

        if (gameObj == null) {
            finish();
        } else {
            for (Cheat cheat : gameObj.getCheatList()) {
                if (cheatArray == null) {
                    cheatArray = new ArrayList();
                }
                cheatArray.add(cheat);
            }

            if ((cheatArray == null) || (cheatArray.size() < 1)) {
                onBackPressed();
            }
            visibleCheat = cheatArray.get(pageSelected);

            getSupportActionBar().setTitle((gameObj.getGameName() != null ? gameObj.getGameName() : ""));
            getSupportActionBar().setSubtitle((gameObj.getSystemName() != null ? gameObj.getSystemName() : ""));

            initialisePaging();
        }
    }

    private void init() {
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(this);
        }

        cheatArray = new ArrayList();
        settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();

        mToolbar = tools.initToolbarBase(this, mToolbar);

        adView = new AdView(this, Konstanten.FACEBOOK_AUDIENCE_NETWORK_NATIVE_BANNER_ID, AdSize.BANNER_HEIGHT_50);
        adView.loadAd();
        facebookBanner.addView(adView);
    }

    private void initialisePaging() {
        try {
            mAdapter = new CheatViewFragmentAdapter(getSupportFragmentManager(), gameObj, cheatArray);
            mPager = viewLayout.findViewById(R.id.pager);
            mPager.setAdapter(mAdapter);
            mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    // Save the last selected page
                    editor.putInt(Konstanten.PREFERENCES_PAGE_SELECTED, position);
                    editor.apply();

                    activePage = position;

                    try {
                        visibleCheat = cheatArray.get(position);
                        invalidateOptionsMenu();
                    } catch (Exception e) {
                        Toast.makeText(CheatViewPageIndicatorActivity.this, R.string.err_somethings_wrong, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            MagicIndicator magicIndicator = findViewById(R.id.magic_indicator);
            CommonNavigator commonNavigator = new CommonNavigator(this);
            commonNavigator.setSkimOver(true);
            commonNavigator.setAdapter(new CommonNavigatorAdapter() {
                @Override
                public int getCount() {
                    return cheatArray == null ? 0 : cheatArray.size();
                }

                @Override
                public IPagerTitleView getTitleView(Context context, final int index) {
                    SimplePagerTitleView clipPagerTitleView = new ColorTransitionPagerTitleView(context);
                    clipPagerTitleView.setText(cheatArray.get(index).getCheatTitle());
                    clipPagerTitleView.setNormalColor(Color.parseColor("#88ffffff")); // White transparent
                    clipPagerTitleView.setSelectedColor(Color.WHITE);
                    clipPagerTitleView.setOnClickListener(v -> mPager.setCurrentItem(index));
                    return clipPagerTitleView;

                }

                @Override
                public IPagerIndicator getIndicator(Context context) {
                    LinePagerIndicator indicator = new LinePagerIndicator(context);
                    indicator.setMode(LinePagerIndicator.MODE_EXACTLY);
                    indicator.setYOffset(UIUtil.dip2px(context, 3));
                    indicator.setColors(Color.WHITE);
                    return indicator;
                }
            });
            magicIndicator.setNavigator(commonNavigator);
            ViewPagerHelper.bind(magicIndicator, mPager);
            mPager.setCurrentItem(pageSelected);

            FloatingActionButton fa = viewLayout.findViewById(R.id.add_new_cheat_button);
            fa.setOnClickListener(v -> {
                Intent explicitIntent = new Intent(CheatViewPageIndicatorActivity.this, SubmitCheatFormActivity.class);
                explicitIntent.putExtra("gameObj", gameObj);
                startActivity(explicitIntent);
            });
        } catch (Exception e2) {
            Log.e(TAG, "ERROR: " + getPackageName() + "/" + getTitle() + "... " + e2.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if ((visibleCheat != null) && (visibleCheat.getMemberRating() > 0)) {
            getMenuInflater().inflate(R.menu.handset_cheatview_rating_on_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.handset_cheatview_rating_off_menu, menu);
        }

        if (tools.getMember() != null) {
            getMenuInflater().inflate(R.menu.signout_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.signin_menu, menu);
        }

        String postOrPosts = getString(R.string.forum_many_posts);
        if (visibleCheat.getForumCount() == 1) {
            postOrPosts = getString(R.string.forum_single_post);
        }
        MenuItem forumMenuItem = menu.findItem(R.id.action_forum);
        forumMenuItem.setTitle(getString(R.string.forum_amount_posts, visibleCheat.getForumCount(), postOrPosts));

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);

        // Sharing
        mShare = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        // Search
        getMenuInflater().inflate(R.menu.search_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
//        MenuItem item = menu.findItem(R.id.action_share);
//        mShare = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        String postOrPosts = getString(R.string.forum_many_posts);
        if (visibleCheat.getForumCount() == 1) {
            postOrPosts = getString(R.string.forum_single_post);
        }
        MenuItem forumMenuItem = menu.findItem(R.id.action_forum);
        forumMenuItem.setTitle(getString(R.string.forum_amount_posts, visibleCheat.getForumCount(), postOrPosts));
        return true;
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        mShare.setShareIntent(shareIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent explicitIntent;
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_submit_cheat:
                explicitIntent = new Intent(CheatViewPageIndicatorActivity.this, SubmitCheatFormActivity.class);
                explicitIntent.putExtra("gameObj", gameObj);
                startActivity(explicitIntent);
                return true;
            case R.id.action_rate:
                showRatingDialog();
                return true;
            case R.id.action_forum:
                if (Reachability.reachability.isReachable) {
                    explicitIntent = new Intent(CheatViewPageIndicatorActivity.this, CheatForumActivity.class);
                    explicitIntent.putExtra("gameObj", gameObj);
                    explicitIntent.putExtra("cheatObj", visibleCheat);
                    resultContract.launch(explicitIntent);
                } else {
                    Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_add_to_favorites:
                tools.showSnackbar(outerLayout, getString(R.string.favorite_adding));

                int memberId = 0;
                if (tools.getMember() != null) {
                    memberId = tools.getMember().getMid();
                }
                tools.addFavorite(visibleCheat, memberId, this);
                return true;
            case R.id.action_report:
                showReportDialog();
                return true;
            case R.id.action_metainfo:
                if (Reachability.reachability.isReachable) {
                    new CheatMetaDialog(CheatViewPageIndicatorActivity.this, visibleCheat, outerLayout, tools, restApi).show();
                } else {
                    Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_login:
                resultContract.launch(new Intent(CheatViewPageIndicatorActivity.this, LoginActivity.class));
                return true;
            case R.id.action_logout:
                tools.logout();
                tools.showSnackbar(outerLayout, getString(R.string.logout_ok));
                invalidateOptionsMenu();
                return true;
            case R.id.action_share:
                setShareIntent(tools.setShareText(visibleCheat));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            EventBus.getDefault().register(this);
        } catch (NullPointerException e) {
        }
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        Reachability.unregister(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    public void showReportDialog() {
        if ((tools.getMember() == null) || (tools.getMember().getMid() == 0)) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_SHORT).show();
        } else {
            new ReportCheatMaterialDialog(this, visibleCheat, tools.getMember(), outerLayout, tools);
        }
    }

    public void showRatingDialog() {
        if ((tools.getMember() == null) || (tools.getMember().getMid() == 0)) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_LONG).show();
        } else {
            new RateCheatMaterialDialog(this, visibleCheat, tools.getMember(), outerLayout, tools);
        }
    }

    @Subscribe
    public void onEvent(CheatRatingFinishedEvent result) {
        visibleCheat.setMemberRating(result.getRating());
        cheatArray.get(activePage).setMemberRating(result.getRating());
        invalidateOptionsMenu();
        Toast.makeText(this, R.string.rating_inserted, Toast.LENGTH_SHORT).show();
    }

    public void setRating(int position, float rating) {
        cheatArray.get(position).setMemberRating(rating);
        invalidateOptionsMenu();
    }

    public RestApi getRestApi() {
        return restApi;
    }

    @Override
    public void success() {
        tools.showSnackbar(outerLayout, getString(R.string.add_favorite_ok));
    }

    @Override
    public void fail(Exception e) {
        tools.showSnackbar(outerLayout, getString(R.string.error_adding_favorite));
    }
}