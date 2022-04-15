package com.cheatdatabase.cheatdetailview;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;

import com.applovin.adview.AppLovinAdView;
import com.cheatdatabase.R;
import com.cheatdatabase.activity.AuthenticationActivity;
import com.cheatdatabase.activity.CheatForumActivity;
import com.cheatdatabase.activity.SubmitCheatFormActivity;
import com.cheatdatabase.callbacks.OnCheatRated;
import com.cheatdatabase.data.RoomCheatDatabase;
import com.cheatdatabase.data.dao.FavoriteCheatDao;
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
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.hilt.android.AndroidEntryPoint;
import needle.Needle;

/**
 * Horizontal sliding gallery of cheats from a game.
 *
 * @author Dominik Erbsland
 */
@AndroidEntryPoint
public class FavoritesCheatViewPageIndicator extends AppCompatActivity implements OnCheatRated {

    private final String TAG = FavoritesCheatViewPageIndicator.class.getSimpleName();

    @Inject
    Tools tools;

    @Inject
    RestApi restApi;

    @BindView(R.id.outer_layout)
    ConstraintLayout outerLayout;
    @BindView(R.id.ad_container)
    AppLovinAdView appLovinAdView;
    @BindView(R.id.pager)
    ViewPager mPager;
    @BindView(R.id.add_new_cheat_button)
    FloatingActionButton fab;

    private FavoriteCheatDao dao;
    private Intent intent;

    private int pageSelected;

    private Game gameObj;
    private List<Cheat> cheatArray;
    private Cheat visibleCheat;

    private int activePage;

    private Toolbar mToolbar;

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
        setContentView(R.layout.activity_cheatview_pager);
        ButterKnife.bind(this);

        intent = getIntent();

        init();

        try {
            gameObj = intent.getParcelableExtra("gameObj");
            pageSelected = intent.getIntExtra("position", 0);

            activePage = pageSelected;
            cheatArray = gameObj.getCheatList();
            visibleCheat = cheatArray.get(pageSelected);

            getSupportActionBar().setTitle(gameObj.getGameName());
            getSupportActionBar().setSubtitle(gameObj.getSystemName());

            initialisePaging();
        } catch (Exception e) {
            Log.e(FavoritesCheatViewPageIndicator.class.getName(), e.getMessage() + "");
        }
    }

    private void init() {
        dao = RoomCheatDatabase.getDatabase(this).favoriteDao();

        mToolbar = tools.initToolbarBase(this, mToolbar);

        appLovinAdView.loadNextAd();
    }

    private void initialisePaging() {

        final String[] cheatTitles = new String[cheatArray.size()];
        for (int i = 0; i < cheatArray.size(); i++) {
            cheatTitles[i] = cheatArray.get(i).getCheatTitle();
        }

        try {
            FavoritesCheatViewFragmentAdapter mAdapter = new FavoritesCheatViewFragmentAdapter(getSupportFragmentManager(), gameObj);
            mPager.setAdapter(mAdapter);
            mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    // Save the last selected page
                    tools.putInt(Konstanten.PREFERENCES_PAGE_SELECTED, position);

                    activePage = position;

                    try {
                        visibleCheat = cheatArray.get(position);
                        invalidateOptionsMenu();
                    } catch (Exception e) {
                        Toast.makeText(FavoritesCheatViewPageIndicator.this, R.string.err_somethings_wrong, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

//            MagicIndicator magicIndicator = findViewById(R.id.magic_indicator);
//            CommonNavigator commonNavigator = new CommonNavigator(this);
//            commonNavigator.setSkimOver(true);
//            commonNavigator.setAdapter(new CommonNavigatorAdapter() {
//                @Override
//                public int getCount() {
//                    return cheatArray == null ? 0 : cheatArray.size();
//                }
//
//                @Override
//                public IPagerTitleView getTitleView(Context context, final int index) {
//                    SimplePagerTitleView clipPagerTitleView = new ColorTransitionPagerTitleView(context);
//                    clipPagerTitleView.setText(cheatTitles[index]);
//                    clipPagerTitleView.setNormalColor(Color.parseColor("#88ffffff")); // White transparent
//                    clipPagerTitleView.setSelectedColor(Color.WHITE);
//                    clipPagerTitleView.setOnClickListener(v -> mPager.setCurrentItem(index));
//                    return clipPagerTitleView;
//                }
//
//                @Override
//                public IPagerIndicator getIndicator(Context context) {
//                    LinePagerIndicator indicator = new LinePagerIndicator(context);
//                    indicator.setMode(LinePagerIndicator.MODE_EXACTLY);
//                    indicator.setYOffset(UIUtil.dip2px(context, 3));
//                    indicator.setColors(Color.WHITE);
//                    return indicator;
//                }
//            });
//            magicIndicator.setNavigator(commonNavigator);
//            ViewPagerHelper.bind(magicIndicator, mPager);
            mPager.setCurrentItem(pageSelected);

            fab.setOnClickListener(v -> {
                Intent intent = new Intent(FavoritesCheatViewPageIndicator.this, SubmitCheatFormActivity.class);
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
            getMenuInflater().inflate(R.menu.favorites_cheatview_rating_on_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.favorites_cheatview_rating_off_menu, menu);
        }

        if (tools.getMember() != null) {
            getMenuInflater().inflate(R.menu.signout_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.signin_menu, menu);
        }

        // Search
        getMenuInflater().inflate(R.menu.search_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent explicitIntent;
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_submit_cheat:
                explicitIntent = new Intent(FavoritesCheatViewPageIndicator.this, SubmitCheatFormActivity.class);
                explicitIntent.putExtra("gameObj", gameObj);
                startActivity(explicitIntent);
                return true;
            case R.id.action_rate:
                showRatingDialog();
                return true;
            case R.id.action_forum:
                if (Reachability.reachability.isReachable) {
                    explicitIntent = new Intent(FavoritesCheatViewPageIndicator.this, CheatForumActivity.class);
                    explicitIntent.putExtra("gameObj", gameObj);
                    explicitIntent.putExtra("cheatObj", visibleCheat);
                    startActivity(explicitIntent);
                } else {
                    Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_remove_from_favorites:
                Needle.onBackgroundThread().execute(() -> dao.delete(visibleCheat.toFavoriteCheatModel(0)));
                showUndoSnackbar();
                return true;
            case R.id.action_report:
                showReportDialog();
                return true;
            case R.id.action_metainfo:
                if (Reachability.reachability.isReachable) {
                    new CheatMetaDialog(FavoritesCheatViewPageIndicator.this, visibleCheat, outerLayout, tools).show();
                } else {
                    Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_login:
                resultContract.launch(new Intent(FavoritesCheatViewPageIndicator.this, AuthenticationActivity.class));
                return true;
            case R.id.action_logout:
                tools.logout();
                tools.showSnackbar(outerLayout, getString(R.string.logout_ok));
                invalidateOptionsMenu();
                return true;
            case R.id.action_share:
                tools.shareCheat(visibleCheat);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Reachability.registerReachability(this);
        invalidateOptionsMenu();
    }

    @Override
    protected void onStop() {
        Reachability.unregister(this);
        super.onStop();
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
            Toast.makeText(this, R.string.error_login_to_rate, Toast.LENGTH_LONG).show();
        } else {
            new RateCheatMaterialDialog(this, visibleCheat, outerLayout, tools, restApi, this);
        }
    }

    public void setRating(int position, float rating) {
        cheatArray.get(position).setMemberRating(rating);
        invalidateOptionsMenu();
    }

    private void showUndoSnackbar() {
        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.outer_layout), R.string.remove_favorite_neutral_ok, Snackbar.LENGTH_INDEFINITE);
        mySnackbar.setAction(R.string.undo, v -> Needle.onBackgroundThread().execute(() -> dao.insert(visibleCheat.toFavoriteCheatModel((tools.getMember() != null ? tools.getMember().getMid() : 0)))));
        mySnackbar.show();
    }

    @Override
    public void onCheatRated(@NotNull CheatRatingFinishedEvent cheatRatingFinishedEvent) {
        visibleCheat.setMemberRating(cheatRatingFinishedEvent.getRating());
        cheatArray.get(activePage).setMemberRating(cheatRatingFinishedEvent.getRating());
        invalidateOptionsMenu();
    }
}