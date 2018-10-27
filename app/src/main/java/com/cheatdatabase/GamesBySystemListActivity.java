package com.cheatdatabase;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.transition.Explode;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.adapters.GamesBySystemRecycleListViewAdapter;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.SystemPlatform;
import com.cheatdatabase.events.GameListRecyclerViewClickEvent;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.widgets.DividerDecoration;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import needle.Needle;

public class GamesBySystemListActivity extends AppCompatActivity {

    private static final String TAG = GamesBySystemListActivity.class.getSimpleName();
    private List<Game> gameArrayList;

    CheatDatabaseApplication cheatDatabaseApplication;

    private SystemPlatform systemObj;

    GamesBySystemRecycleListViewAdapter gamesBySystemRecycleListViewAdapter;

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

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);
        ButterKnife.bind(this);

        systemObj = getIntent().getParcelableExtra("systemObj");
        setTitle(systemObj.getSystemName());

        init();
        mSwipeRefreshLayout.setRefreshing(true);

        mSwipeRefreshLayout.setOnRefreshListener(() -> getGames(true));

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new DividerDecoration(this));
        mRecyclerView.getItemAnimator().setRemoveDuration(50);
        mRecyclerView.setHasFixedSize(true);

        if (Reachability.reachability.isReachable) {
            getGames(false);
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    private void init() {
        gamesBySystemRecycleListViewAdapter = new GamesBySystemRecycleListViewAdapter(this);

        adView = new AdView(this, Konstanten.FACEBOOK_AUDIENCE_NETWORK_NATIVE_BANNER_ID, AdSize.BANNER_HEIGHT_50);
        facebookBanner.addView(adView);
        adView.loadAd();

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        sharedPreferences = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Search
        getMenuInflater().inflate(R.menu.search_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }

    private void getGames(boolean forceLoadOnline) {
        Log.d(TAG, "XXXXX getGames() System ID/NAME: " + systemObj.getSystemId() + "/" + systemObj.getSystemName());

        Needle.onBackgroundThread().execute(() -> {
            gameArrayList = new ArrayList<>();
            Game[] cachedGamesCollection;
            Game[] gamesFound = null;
            boolean isCached = false;
            String achievementsEnabled;
            boolean isAchievementsEnabled = sharedPreferences.getBoolean("enable_achievements", true);
            if (isAchievementsEnabled) {
                achievementsEnabled = Konstanten.ACHIEVEMENTS;
            } else {
                achievementsEnabled = Konstanten.NO_ACHIEVEMENTS;
            }

            TreeMap<String, TreeMap<String, Game[]>> gamesBySystemInCache = cheatDatabaseApplication.getGamesBySystemCached();
            TreeMap gameList = null;
            if (gamesBySystemInCache.containsKey(String.valueOf(systemObj.getSystemId()))) {

                gameList = gamesBySystemInCache.get(String.valueOf(systemObj.getSystemId()));
                if (gameList != null) {

                    if (gameList.containsKey(achievementsEnabled)) {
                        cachedGamesCollection = (Game[]) gameList.get(achievementsEnabled);

                        if (cachedGamesCollection.length > 0) {
                            gamesFound = cachedGamesCollection;
                            isCached = true;
                        }
                    }
                }
            }

            if (!isCached || forceLoadOnline) {
                gamesFound = Webservice.getGameListBySystemId(systemObj.getSystemId(), systemObj.getSystemName(), isAchievementsEnabled);
                while (gamesFound == null) {
                    gamesFound = Webservice.getGameListBySystemId(systemObj.getSystemId(), systemObj.getSystemName(), isAchievementsEnabled);
                }

                TreeMap<String, Game[]> updatedGameListForCache = new TreeMap<>();
                updatedGameListForCache.put(achievementsEnabled, gamesFound);

                String checkWhichSubKey;
                if (achievementsEnabled.equalsIgnoreCase(Konstanten.ACHIEVEMENTS)) {
                    checkWhichSubKey = Konstanten.NO_ACHIEVEMENTS;
                } else {
                    checkWhichSubKey = Konstanten.ACHIEVEMENTS;
                }

                if ((gameList != null) && (gameList.containsKey(checkWhichSubKey))) {
                    Game[] existingGamesInCache = (Game[]) gameList.get(checkWhichSubKey);
                    updatedGameListForCache.put(checkWhichSubKey, existingGamesInCache);
                }

                gamesBySystemInCache.put(String.valueOf(systemObj.getSystemId()), updatedGameListForCache);
                cheatDatabaseApplication.setGamesBySystemCached(gamesBySystemInCache);
            }
            Collections.addAll(gameArrayList, gamesFound);

            updateUI();
        });
    }

    private void updateUI() {
        Needle.onMainThread().execute(() -> {
            try {
                if (gameArrayList != null && gameArrayList.size() > 0) {
                    gamesBySystemRecycleListViewAdapter.setGameList(gameArrayList);
                    mRecyclerView.setAdapter(gamesBySystemRecycleListViewAdapter);
                    gamesBySystemRecycleListViewAdapter.notifyDataSetChanged();
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
        Log.e(TAG, "caught error: " + getPackageName() + "/" + getTitle());
        new AlertDialog.Builder(GamesBySystemListActivity.this).setIcon(R.drawable.ic_action_warning).setTitle(getString(R.string.err)).setMessage(R.string.err_data_not_accessible).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        }).create().show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(this);
        }
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

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    @Subscribe
    public void onEvent(GameListRecyclerViewClickEvent result) {
        if (result.isSucceeded()) {
            Intent intent = new Intent(this, CheatsByGameListActivity.class);
            intent.putExtra("gameObj", result.getGame());
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putParcelable("systemObj", systemObj);
//    }
//
//    @Override
//    public void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//
//        systemObj = savedInstanceState.getParcelable("systemObj");
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//    }
}
