package com.cheatdatabase.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
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

import com.cheatdatabase.CheatDatabaseApplication;
import com.cheatdatabase.R;
import com.cheatdatabase.adapters.GamesBySystemRecycleListViewAdapter;
import com.cheatdatabase.data.model.Game;
import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.data.model.SystemModel;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.listeners.OnGameListItemSelectedListener;
import com.cheatdatabase.rest.RestApi;
import com.cheatdatabase.widgets.DividerDecoration;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdsManager;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.hilt.android.AndroidEntryPoint;
import needle.Needle;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class GamesBySystemListActivity extends AppCompatActivity implements OnGameListItemSelectedListener {

    private static final String TAG = GamesBySystemListActivity.class.getSimpleName();

    private List<Game> gameList;
    private GamesBySystemRecycleListViewAdapter gamesBySystemRecycleListViewAdapter;
    private SystemModel systemObj;
    private Member member;

    @Inject
    Tools tools;

    @Inject
    RestApi restApi;

    @BindView(R.id.outer_layout)
    LinearLayout outerLayout;
    @BindView(R.id.my_recycler_view)
    FastScrollRecyclerView recyclerView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.item_list_empty_view)
    TextView mEmptyView;

    @Override
    protected void onStart() {
        super.onStart();

        if (Reachability.reachability.isReachable) {
            loadGames(false);
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);
        ButterKnife.bind(this);

        NativeAdsManager nativeAdsManager = new NativeAdsManager(this, Konstanten.FACEBOOK_AUDIENCE_NETWORK_NATIVE_AD_IN_RECYCLER_VIEW, 5);
        nativeAdsManager.loadAds(NativeAd.MediaCacheFlag.ALL);

        SystemModel asdf = (SystemModel) getIntent().getSerializableExtra("systemObj");
        systemObj = getIntent().getParcelableExtra("systemObj");
        if (systemObj == null) {
            Toast.makeText(this, R.string.err_somethings_wrong, Toast.LENGTH_LONG).show();
            finish();
        } else {
            setTitle((systemObj.getSystemName() != null ? systemObj.getSystemName() : ""));
            init();

            mSwipeRefreshLayout.setRefreshing(true);
            mSwipeRefreshLayout.setOnRefreshListener(() -> loadGames(true));

            gamesBySystemRecycleListViewAdapter = new GamesBySystemRecycleListViewAdapter(this, nativeAdsManager, tools, this);
            recyclerView.setAdapter(gamesBySystemRecycleListViewAdapter);

            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
            recyclerView.addItemDecoration(new DividerDecoration(this));
            recyclerView.getItemAnimator().setRemoveDuration(50);
            recyclerView.setHasFixedSize(true);
            recyclerView.showScrollbar();
        }
    }

    private void init() {
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (member == null) {
            member = tools.getMember();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
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

        getMenuInflater().inflate(R.menu.search_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        switch (item.getItemId()) {
            case R.id.action_login:
                Intent loginIntent = new Intent(GamesBySystemListActivity.this, LoginActivity.class);
                startActivityForResult(loginIntent, Konstanten.LOGIN_REGISTER_OK_RETURN_CODE);
                return true;
            case R.id.action_logout:
                member = null;
                tools.logout();
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadGames(boolean forceLoadOnline) {
        Log.d(TAG, "loadGames() System ID/NAME: " + systemObj.getSystemId() + "/" + systemObj.getSystemName());

        gameList = new ArrayList<>();
        List<Game> cachedGamesCollection;
        boolean isCached = false;
        String achievementsEnabled;
        boolean isAchievementsEnabled = tools.getBooleanFromSharedPreferences("enable_achievements", true);

        if (isAchievementsEnabled) {
            achievementsEnabled = Konstanten.ACHIEVEMENTS;
        } else {
            achievementsEnabled = Konstanten.NO_ACHIEVEMENTS;
        }

        TreeMap gameListTree = null;
        TreeMap<String, TreeMap<String, List<Game>>> gamesBySystemInCache = CheatDatabaseApplication.getGamesBySystemCached();
        if (gamesBySystemInCache.containsKey(String.valueOf(systemObj.getSystemId()))) {

            gameListTree = gamesBySystemInCache.get(String.valueOf(systemObj.getSystemId()));
            if (gameListTree != null) {

                if (gameListTree.containsKey(achievementsEnabled)) {
                    cachedGamesCollection = (List<Game>) gameListTree.get(achievementsEnabled);

                    if (cachedGamesCollection.size() > 0) {
                        gameList = cachedGamesCollection;
                        isCached = true;
                    }
                }
            }
        }

        if (!isCached || forceLoadOnline || gameList.size() == 0) {
            gameList = new ArrayList<>();
            TreeMap finalGameListTree = gameListTree;

            Call<List<Game>> call = restApi.getGameListBySystemId(systemObj.getSystemId(), isAchievementsEnabled);
            call.enqueue(new Callback<List<Game>>() {
                @Override
                public void onResponse(Call<List<Game>> games, Response<List<Game>> response) {
                    if (response.isSuccessful()) {
                        gameList = response.body();
                        applySystemToGames();

                        TreeMap<String, List<Game>> updatedGameListForCache = new TreeMap<>();
                        updatedGameListForCache.put(achievementsEnabled, gameList);

                        String checkWhichSubKey;
                        if (achievementsEnabled.equalsIgnoreCase(Konstanten.ACHIEVEMENTS)) {
                            checkWhichSubKey = Konstanten.NO_ACHIEVEMENTS;
                        } else {
                            checkWhichSubKey = Konstanten.ACHIEVEMENTS;
                        }

                        if ((finalGameListTree != null) && (finalGameListTree.containsKey(checkWhichSubKey))) {
                            List<Game> existingGamesInCache = (List<Game>) finalGameListTree.get(checkWhichSubKey);
                            updatedGameListForCache.put(checkWhichSubKey, existingGamesInCache);
                        }

                        gamesBySystemInCache.put(String.valueOf(systemObj.getSystemId()), updatedGameListForCache);
                        CheatDatabaseApplication.setGamesBySystemCached(gamesBySystemInCache);

                        updateUI();
                    }
                }

                @Override
                public void onFailure(Call<List<Game>> call, Throwable t) {
                    Log.e(TAG, "Load game and cheats counters failed: " + t.getLocalizedMessage());
                    error();
                }
            });
        } else {
            updateUI();
        }
    }

    private void applySystemToGames() {
        for (Game g : gameList) {
            g.setSystemId(systemObj.getSystemId());
            g.setSystemName(systemObj.getSystemName());
        }
    }

    private void updateUI() {
        if (gameList != null && gameList.size() > 0) {
            gamesBySystemRecycleListViewAdapter.setGameList(gameList);
            gamesBySystemRecycleListViewAdapter.filterList("");
        } else {
            error();
        }

        Needle.onMainThread().execute(() -> mSwipeRefreshLayout.setRefreshing(false));
    }

    private void error() {
        Log.e(TAG, "Caught error: " + getPackageName() + "/" + getTitle());
        Needle.onMainThread().execute(() -> {
            tools.showSnackbar(outerLayout, getString(R.string.err_data_not_accessible));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(this);
        }
    }

    @Override
    protected void onStop() {
        Reachability.unregister(this);
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        member = tools.getMember();

        if (requestCode == Konstanten.LOGIN_REGISTER_OK_RETURN_CODE) {
            if (resultCode == Konstanten.LOGIN_SUCCESS_RETURN_CODE) {
                Toast.makeText(this, R.string.login_ok, Toast.LENGTH_LONG).show();
            } else if (resultCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
                Toast.makeText(this, R.string.register_thanks, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onGameListItemSelected(Game game) {
        if (Reachability.reachability.isReachable) {
            Intent intent = new Intent(this, CheatsByGameListActivity.class);
            intent.putExtra("gameObj", game);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }


}
