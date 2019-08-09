package com.cheatdatabase;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cheatdatabase.adapters.CheatsByGameRecycleListViewAdapter;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.dialogs.RateCheatMaterialDialog;
import com.cheatdatabase.dialogs.ReportCheatMaterialDialog;
import com.cheatdatabase.events.CheatListRecyclerViewClickEvent;
import com.cheatdatabase.events.CheatRatingFinishedEvent;
import com.cheatdatabase.handset.cheatview.CheatViewPageIndicatorActivity;
import com.cheatdatabase.helpers.DatabaseHelper;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.widgets.DividerDecoration;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.google.gson.Gson;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import needle.Needle;

public class CheatsByGameListActivity extends AppCompatActivity {

    private static String TAG = CheatsByGameListActivity.class.getSimpleName();

    private SharedPreferences sharedPreferences;
    private Editor editor;
    private Member member;

    private List<Cheat> cheatsArrayList;
    private Cheat visibleCheat;

    CheatDatabaseApplication cheatDatabaseApplication;

    CheatsByGameRecycleListViewAdapter cheatsByGameRecycleListViewAdapter;

    Game gameObj;

    @BindView(R.id.outer_layout)
    LinearLayout outerLayout;
    @BindView(R.id.my_recycler_view)
    FastScrollRecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.item_list_empty_view)
    TextView mEmptyView;

    @BindView(R.id.banner_container)
    LinearLayout facebookBanner;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheat_list);
        ButterKnife.bind(this);

        init();

        gameObj = getIntent().getParcelableExtra("gameObj");

        if (gameObj != null) {
            setTitle(gameObj.getGameName());

            mSwipeRefreshLayout.setRefreshing(true);

            getSupportActionBar().setTitle(gameObj.getGameName());
            getSupportActionBar().setSubtitle(gameObj.getSystemName());

            mSwipeRefreshLayout.setOnRefreshListener(() -> getCheats(true));

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
            mRecyclerView.addItemDecoration(new DividerDecoration(this));
            mRecyclerView.getItemAnimator().setRemoveDuration(50);
            mRecyclerView.setHasFixedSize(true);

            if (Reachability.reachability.isReachable) {
                getCheats(false);
            } else {
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
            }
        } else {
            finish();
        }

    }

    private void init() {
        cheatDatabaseApplication = CheatDatabaseApplication.getCurrentAppInstance();

        cheatsByGameRecycleListViewAdapter = new CheatsByGameRecycleListViewAdapter();

        adView = new AdView(this, Konstanten.FACEBOOK_AUDIENCE_NETWORK_NATIVE_BANNER_ID, AdSize.BANNER_HEIGHT_50);
        facebookBanner.addView(adView);
        adView.loadAd();

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        sharedPreferences = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = sharedPreferences.edit();

        if (member == null) {
            member = new Gson().fromJson(sharedPreferences.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
        }
    }

    public void getCheats(boolean forceLoadOnline) {
        Log.d(TAG, "get cheats by game: " + gameObj.getGameName() + " / " + gameObj.getGameId());

        Needle.onBackgroundThread().execute(() -> {
            cheatsArrayList = new ArrayList<>();
            List<Cheat> cachedCheatsCollection;
            List<Cheat> cheatsFound = null;
            boolean isCached = false;
            String achievementsEnabled;
            boolean isAchievementsEnabled = sharedPreferences.getBoolean("enable_achievements", true);

            if (isAchievementsEnabled) {
                achievementsEnabled = Konstanten.ACHIEVEMENTS;
            } else {
                achievementsEnabled = Konstanten.NO_ACHIEVEMENTS;
            }


            try {
                TreeMap<String, TreeMap<String, List<Cheat>>> cheatsByGameInCache = cheatDatabaseApplication.getCheatsByGameCached();
                TreeMap cheatList = null;
                if (cheatsByGameInCache.containsKey(String.valueOf(gameObj.getGameId()))) {

                    cheatList = cheatsByGameInCache.get(String.valueOf(gameObj.getGameId()));
                    if (cheatList != null) {

                        if (cheatList.containsKey(achievementsEnabled)) {
                            cachedCheatsCollection = (List<Cheat>) cheatList.get(achievementsEnabled);

                            if ((cachedCheatsCollection != null) && (cachedCheatsCollection.size() > 0)) {
                                cheatsFound = cachedCheatsCollection;
                                gameObj.setCheatList(cheatsFound);
                                isCached = true;
                            }
                        }
                    }
                }

                if (!isCached || forceLoadOnline) {
                    if (member == null) {
                        cheatsFound = Webservice.getCheatList(gameObj, 0, isAchievementsEnabled);
                    } else {
                        cheatsFound = Webservice.getCheatList(gameObj, member.getMid(), isAchievementsEnabled);
                    }
                    gameObj.setCheatList(cheatsFound);

                    TreeMap<String, List<Cheat>> updatedCheatListForCache = new TreeMap<>();
                    updatedCheatListForCache.put(achievementsEnabled, cheatsFound);

                    String checkWhichSubKey;
                    if (achievementsEnabled.equalsIgnoreCase(Konstanten.ACHIEVEMENTS)) {
                        checkWhichSubKey = Konstanten.NO_ACHIEVEMENTS;
                    } else {
                        checkWhichSubKey = Konstanten.ACHIEVEMENTS;
                    }

                    if ((cheatList != null) && (cheatList.containsKey(checkWhichSubKey))) {
                        List<Cheat> existingGamesInCache = (List<Cheat>) cheatList.get(checkWhichSubKey);
                        updatedCheatListForCache.put(checkWhichSubKey, existingGamesInCache);
                    }

                    cheatsByGameInCache.put(String.valueOf(gameObj.getGameId()), updatedCheatListForCache);
                    cheatDatabaseApplication.setCheatsByGameCached(cheatsByGameInCache);
                }

                cheatsArrayList = cheatsFound;
                updateUI();
            } catch (NullPointerException e) {
                Toast.makeText(this, R.string.err_data_not_accessible, Toast.LENGTH_LONG).show();
                finish();
            }

        });
    }

    public void updateUI() {
        Needle.onMainThread().execute(() -> {
            try {
                if (cheatsArrayList != null && cheatsArrayList.size() > 0) {
                    cheatsByGameRecycleListViewAdapter.setCheats(cheatsArrayList);
                    mRecyclerView.setAdapter(cheatsByGameRecycleListViewAdapter);

                    cheatsByGameRecycleListViewAdapter.notifyDataSetChanged();
                } else {
                    error();
                }
            } catch (Exception e) {
                error();
            }

            mSwipeRefreshLayout.setRefreshing(false);
        });
    }

    private void error() {
        Toast.makeText(this, R.string.err_data_not_accessible, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(this);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("gameObj", gameObj);
    }

    public void showReportDialog() {
        if ((member == null) || (member.getMid() == 0)) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_LONG).show();
        } else {
            new ReportCheatMaterialDialog(this, visibleCheat, member);
        }
    }


    public void showRatingDialog() {
        if ((member == null) || (member.getMid() == 0)) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_LONG).show();
        } else {
            new RateCheatMaterialDialog(this, visibleCheat, member);
        }
    }

    @Subscribe
    public void onEvent(CheatRatingFinishedEvent result) {
        visibleCheat.setMemberRating(result.getRating());
        Toast.makeText(this, R.string.rating_inserted, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        switch (item.getItemId()) {
            case android.R.id.home:
                editor.remove(Konstanten.PREFERENCES_TEMP_GAME_OBJECT_VIEW);
                editor.apply();
                finish();
                return true;
            case R.id.action_add_to_favorites:
                Tools.showSnackbar(outerLayout, getString(R.string.favorite_adding));
                addCheatsToFavoritesTask(gameObj);
                return true;
            case R.id.action_submit_cheat:
                Intent explicitIntent = new Intent(CheatsByGameListActivity.this, SubmitCheatActivity.class);
                explicitIntent.putExtra("gameObj", gameObj);
                startActivity(explicitIntent);
                return true;
            case R.id.action_login:
                Intent loginIntent = new Intent(CheatsByGameListActivity.this, LoginActivity.class);
                startActivityForResult(loginIntent, Konstanten.LOGIN_REGISTER_OK_RETURN_CODE);
                return true;
            case R.id.action_logout:
                member = null;
                Tools.logout(CheatsByGameListActivity.this, editor);
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.add_new_cheat_button)
    void addNewCheat() {
        Intent explicitIntent = new Intent(CheatsByGameListActivity.this, SubmitCheatActivity.class);
        explicitIntent.putExtra("gameObj", gameObj);
        startActivity(explicitIntent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cheats_by_game_menu, menu);

        if (member != null) {
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
    public boolean onPrepareOptionsMenu(final Menu menu) {
        menu.clear();
        if (member == null) {
            getMenuInflater().inflate(R.menu.signin_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.signout_menu, menu);
        }

        getMenuInflater().inflate(R.menu.cheats_by_game_menu, menu);
        getMenuInflater().inflate(R.menu.search_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        member = new Gson().fromJson(sharedPreferences.getString(Konstanten.MEMBER_OBJECT, null), Member.class);

        if (requestCode == Konstanten.LOGIN_REGISTER_OK_RETURN_CODE) {
            if (resultCode == Konstanten.LOGIN_SUCCESS_RETURN_CODE) {
                Toast.makeText(this, R.string.login_ok, Toast.LENGTH_LONG).show();
            } else if (resultCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
                Toast.makeText(this, R.string.register_thanks, Toast.LENGTH_LONG).show();
            }
        }
    }

    void addCheatsToFavoritesTask(Game game) {
        Needle.onBackgroundThread().execute(() -> {
            String text;
            DatabaseHelper db = new DatabaseHelper(this);
            try {
                int retVal = db.insertFavoriteCheats(game);
                if (retVal > 0) {
                    text = getApplicationContext().getString(R.string.add_favorites_ok);
                } else {
                    text = getApplicationContext().getString(R.string.favorite_error);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
                text = getApplicationContext().getString(R.string.favorite_error);
            }

            finishedAddingCheatsToFavorites(text);
        });
    }

    void finishedAddingCheatsToFavorites(String text) {
        Tools.showSnackbar(outerLayout, text);
    }

    @Subscribe
    public void onEvent(CheatListRecyclerViewClickEvent result) {
        if (result.isSucceeded()) {
            this.visibleCheat = result.getCheat();
            Game lastGameObj = result.getCheat().getGame();

            editor.putInt(Konstanten.PREFERENCES_PAGE_SELECTED, result.getPosition());
            editor.apply();

            if (Reachability.reachability.isReachable) {
                // Using local Preferences to pass data for large game objects
                // (instead of intent) such as Pokemon
                Intent explicitIntent = new Intent(CheatsByGameListActivity.this, CheatViewPageIndicatorActivity.class);
                explicitIntent.putExtra("gameObj", gameObj);
                explicitIntent.putExtra("selectedPage", result.getPosition());
                explicitIntent.putExtra("layoutResourceId", R.layout.activity_cheatview_pager);
                startActivity(explicitIntent);
            } else {
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }


}
