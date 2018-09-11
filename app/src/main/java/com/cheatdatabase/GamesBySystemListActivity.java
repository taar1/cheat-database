package com.cheatdatabase;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.adapters.GamesBySystemRecycleListViewAdapter;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.SystemPlatform;
import com.cheatdatabase.events.GameListRecyclerViewClickEvent;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.MyPrefs_;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.widgets.DividerDecoration;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

@EActivity(R.layout.activity_game_list)
public class GamesBySystemListActivity extends AppCompatActivity {

    private static final String TAG = GamesBySystemListActivity.class.getSimpleName();
    private ArrayList<Game> gameArrayList;

    @App
    CheatDatabaseApplication app;

    @Extra
    SystemPlatform systemObj;

    @Bean
    Tools tools;
    @Bean
    GamesBySystemRecycleListViewAdapter mGamesBySystemRecycleListViewAdapter;

    @Pref
    MyPrefs_ myPrefs;

    @ViewById(R.id.my_recycler_view)
    FastScrollRecyclerView mRecyclerView;
    @ViewById(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    //    @ViewById(R.id.adview)
//    MoPubView mAdView;
    @ViewById(R.id.toolbar)
    Toolbar mToolbar;
    @ViewById(R.id.item_list_empty_view)
    TextView mEmptyView;

    @ViewById(R.id.banner_container)
    LinearLayout facebookBanner;
    private AdView adView;

    @AfterViews
    public void createView() {
        init();
        mSwipeRefreshLayout.setRefreshing(true);

        setTitle(systemObj.getSystemName());
        //CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "loaded").setLabel("GamesBySystemListActivity").build());

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getGames(true);
            }
        });

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new DividerDecoration(this));
        mRecyclerView.getItemAnimator().setRemoveDuration(50);
        mRecyclerView.setHasFixedSize(true);

        if (Reachability.reachability.isReachable) {
            getGames(false);
        }
    }

    private void init() {
        adView = new AdView(this, Konstanten.FACEBOOK_AUDIENCE_NETWORK_NATIVE_BANNER_ID, AdSize.BANNER_HEIGHT_50);
        facebookBanner.addView(adView);
        adView.loadAd();

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
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

    @Background
    public void getGames(boolean forceLoadOnline) {
        Log.d(TAG, "getGames() System ID/NAME: " + systemObj.getSystemId() + "/" + systemObj.getSystemName());

        gameArrayList = new ArrayList<>();
        Game[] cachedGamesCollection;
        Game[] gamesFound = null;
        boolean isCached = false;
        String achievementsEnabled;
        boolean isAchievementsEnabled = myPrefs.isAchievementsEnabled().getOr(true);
        if (isAchievementsEnabled) {
            achievementsEnabled = app.ACHIEVEMENTS;
        } else {
            achievementsEnabled = app.NO_ACHIEVEMENTS;
        }

        TreeMap<String, TreeMap<String, Game[]>> gamesBySystemInCache = app.getGamesBySystemCached();
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
            if (achievementsEnabled.equalsIgnoreCase(app.ACHIEVEMENTS)) {
                checkWhichSubKey = app.NO_ACHIEVEMENTS;
            } else {
                checkWhichSubKey = app.ACHIEVEMENTS;
            }

            if ((gameList != null) && (gameList.containsKey(checkWhichSubKey))) {
                Game[] existingGamesInCache = (Game[]) gameList.get(checkWhichSubKey);
                updatedGameListForCache.put(checkWhichSubKey, existingGamesInCache);
            }

            gamesBySystemInCache.put(String.valueOf(systemObj.getSystemId()), updatedGameListForCache);
            app.setGamesBySystemCached(gamesBySystemInCache);
        }
        Collections.addAll(gameArrayList, gamesFound);
        fillListWithGames();
    }

    @UiThread
    public void fillListWithGames() {
        try {
            if (gameArrayList != null && gameArrayList.size() > 0) {
                mGamesBySystemRecycleListViewAdapter.init(gameArrayList);
                mRecyclerView.setAdapter(mGamesBySystemRecycleListViewAdapter);

                mGamesBySystemRecycleListViewAdapter.notifyDataSetChanged();
            } else {
                error();
            }
        } catch (Exception e) {
            error();
        }

        mSwipeRefreshLayout.setRefreshing(false);
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
            //CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "click").setLabel(result.getGame().getGameName()).build());

            CheatsByGameListActivity_.intent(this).gameObj(result.getGame()).start();
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }


}
