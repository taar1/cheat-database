package com.cheatdatabase.cheatdetailview;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
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

import com.applovin.adview.AppLovinAdView;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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

import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.hilt.android.AndroidEntryPoint;

/**
 * Horizontal sliding through cheats submitted by members.
 *
 * @author Dominik Erbsland
 */
@AndroidEntryPoint
public class MemberCheatViewPageIndicator extends AppCompatActivity implements GenericCallback {

    private final String TAG = MemberCheatViewPageIndicator.class.getName();

    @Inject
    Tools tools;

    @Inject
    RestApi restApi;

    @BindView(R.id.outer_layout)
    LinearLayout outerLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.ad_container)
    AppLovinAdView appLovinAdView;

    private Intent intent;
    private View viewLayout;
    private int pageSelected;
    private List<Cheat> cheatList;
    private Cheat visibleCheat;
    private Game gameObj;
    private MemberCheatViewFragmentAdapter memberCheatViewFragmentAdapter;
    private ViewPager mPager;
    private int activePage;
    private ShareActionProvider mShare;

    private final ActivityResultLauncher<Intent> resultContract =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), getActivityResultRegistry(), activityResult -> {
                int intentReturnCode = activityResult.getResultCode();
                if (intentReturnCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
                    tools.showSnackbar(outerLayout, getString(R.string.register_thanks));
                } else if (intentReturnCode == Konstanten.LOGIN_SUCCESS_RETURN_CODE) {
                    tools.showSnackbar(outerLayout, getString(R.string.login_ok));
                } else if (activityResult.getResultCode() == Konstanten.RECOVER_PASSWORD_ATTEMPT) {
                    tools.showSnackbar(outerLayout, getString(R.string.recover_login_success));
                }
                invalidateOptionsMenu();
            });


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();

        LayoutInflater inflater = LayoutInflater.from(this);
        viewLayout = inflater.inflate(R.layout.activity_cheatview_pager, null);
        setContentView(viewLayout);
        ButterKnife.bind(this);

        init();

        Type type = new TypeToken<List<Cheat>>() {
        }.getType();
        cheatList = new Gson().fromJson(tools.getSharedPreferences().getString(Konstanten.PREFERENCES_TEMP_CHEAT_ARRAY_OBJECT_VIEW, null), type);

        if ((cheatList == null) || (cheatList.size() < 1)) {
            cheatList = intent.getParcelableArrayListExtra("cheatList");
        }

        if ((cheatList == null) || (cheatList.size() < 1)) {
            handleNullPointerException();
        } else {
            pageSelected = intent.getIntExtra("selectedPage", 0);
            activePage = pageSelected;

            try {
                visibleCheat = cheatList.get(pageSelected);

                gameObj = visibleCheat.getGame();
                gameObj.setSystemId(visibleCheat.getSystem().getSystemId());
                gameObj.setSystemName(visibleCheat.getSystem().getSystemName());

                getSupportActionBar().setTitle(visibleCheat.getGame().getGameName());
                getSupportActionBar().setSubtitle(visibleCheat.getSystem().getSystemName());

                initialisePaging();
            } catch (NullPointerException e) {
                handleNullPointerException();
            }
        }
    }

    private void handleNullPointerException() {
        Toast.makeText(this, R.string.err_somethings_wrong, Toast.LENGTH_LONG).show();
        finish();
    }

    private void init() {
        mToolbar = tools.initToolbarBase(this, mToolbar);
        appLovinAdView.loadNextAd();
    }

    private void initialisePaging() {
        try {
            memberCheatViewFragmentAdapter = new MemberCheatViewFragmentAdapter(getSupportFragmentManager(), cheatList, outerLayout);

            mPager = viewLayout.findViewById(R.id.pager);
            mPager.setAdapter(memberCheatViewFragmentAdapter);
            mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    // Save selected page
                    tools.putInt(Konstanten.PREFERENCES_PAGE_SELECTED, position);

                    activePage = position;

                    try {
                        visibleCheat = cheatList.get(position);
                        invalidateOptionsMenu();

                        gameObj = visibleCheat.getGame();
                        gameObj.setSystemId(visibleCheat.getSystem().getSystemId());
                        gameObj.setSystemName(visibleCheat.getSystem().getSystemName());

                        getSupportActionBar().setTitle(visibleCheat.getGame().getGameName());
                        getSupportActionBar().setSubtitle(visibleCheat.getSystem().getSystemName());
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(MemberCheatViewPageIndicator.this, R.string.err_somethings_wrong, Toast.LENGTH_SHORT).show();
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
                    return cheatList == null ? 0 : cheatList.size();
                }

                @Override
                public IPagerTitleView getTitleView(Context context, final int index) {
                    SimplePagerTitleView clipPagerTitleView = new ColorTransitionPagerTitleView(context);
                    clipPagerTitleView.setText(cheatList.get(index).getCheatTitle());
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
                Intent intent = new Intent(MemberCheatViewPageIndicator.this, SubmitCheatFormActivity.class);
                intent.putExtra("gameObj", gameObj);
                startActivity(intent);
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

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShare != null) {
            mShare.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent explicitIntent;
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_submit_cheat:
                explicitIntent = new Intent(MemberCheatViewPageIndicator.this, SubmitCheatFormActivity.class);
                explicitIntent.putExtra("gameObj", gameObj);
                startActivity(explicitIntent);
                return true;
            case R.id.action_rate:
                showRatingDialog();
                return true;
            case R.id.action_forum:
                explicitIntent = new Intent(MemberCheatViewPageIndicator.this, CheatForumActivity.class);
                explicitIntent.putExtra("gameObj", gameObj);
                explicitIntent.putExtra("cheatObj", visibleCheat);
                explicitIntent.putExtra("cheatList", new Gson().toJson(cheatList));
                startActivity(explicitIntent);
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
                new CheatMetaDialog(MemberCheatViewPageIndicator.this, visibleCheat, outerLayout, tools).show();
                return true;
            case R.id.action_login:
                resultContract.launch(new Intent(MemberCheatViewPageIndicator.this, LoginActivity.class));
                return true;
            case R.id.action_logout:
                tools.logout();
                invalidateOptionsMenu();
                return true;
            case R.id.action_share:
                setShareIntent(tools.setShareText(visibleCheat));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showReportDialog() {
        if ((tools.getMember() == null) || (tools.getMember().getMid() == 0)) {
            Toast.makeText(MemberCheatViewPageIndicator.this, R.string.error_login_required, Toast.LENGTH_SHORT).show();
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
        cheatList.get(activePage).setMemberRating(result.getRating());
        invalidateOptionsMenu();
        Toast.makeText(this, R.string.rating_inserted, Toast.LENGTH_SHORT).show();
    }

    public void setRating(int position, float rating) {
        cheatList.get(position).setMemberRating(rating);
        invalidateOptionsMenu();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(this);
        }

        invalidateOptionsMenu();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        Reachability.unregister(this);
        super.onStop();
    }

    public RestApi getRestApi() {
        return restApi;
    }

    @Override
    public void success() {
        Log.d(TAG, "MemberCheatViewPageIndicator ADD FAV success: ");
        tools.showSnackbar(outerLayout, getString(R.string.add_favorite_ok));
    }

    @Override
    public void fail(Exception e) {
        Log.d(TAG, "MemberCheatViewPageIndicator ADD FAV fail: ");
        tools.showSnackbar(outerLayout, getString(R.string.error_adding_favorite));
    }
}