package com.cheatdatabase.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.applovin.adview.AppLovinAdView;
import com.cheatdatabase.CheatDatabaseApplication;
import com.cheatdatabase.R;
import com.cheatdatabase.adapters.CheatsByGameRecycleListViewAdapter;
import com.cheatdatabase.cheatdetailview.CheatViewPageIndicatorActivity;
import com.cheatdatabase.data.RoomCheatDatabase;
import com.cheatdatabase.data.dao.FavoriteCheatDao;
import com.cheatdatabase.data.model.Cheat;
import com.cheatdatabase.data.model.Game;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.listeners.OnCheatListItemSelectedListener;
import com.cheatdatabase.rest.RestApi;
import com.cheatdatabase.widgets.DividerDecoration;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.hilt.android.AndroidEntryPoint;
import needle.Needle;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class CheatsByGameListActivity extends AppCompatActivity implements OnCheatListItemSelectedListener {

    private static final String TAG = CheatsByGameListActivity.class.getSimpleName();

    @Inject
    Tools tools;

    @Inject
    RestApi restApi;

    @BindView(R.id.outer_layout)
    ConstraintLayout outerLayout;
    @BindView(R.id.my_recycler_view)
    FastScrollRecyclerView recyclerView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.item_list_empty_view)
    TextView mEmptyView;
    @BindView(R.id.ad_container)
    AppLovinAdView appLovinAdView;

    private ArrayList<Cheat> cheatList;
    private boolean isAchievementsEnabled;

    private CheatDatabaseApplication cheatDatabaseApplication;
    private CheatsByGameRecycleListViewAdapter cheatsByGameRecycleListViewAdapter;

    private Game gameObj;

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
    protected void onStart() {
        super.onStart();

        if (Reachability.reachability.isReachable) {
            loadCheats(false);
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheat_list);
        ButterKnife.bind(this);

        gameObj = getIntent().getParcelableExtra("gameObj");

        if (gameObj == null) {
            Toast.makeText(this, R.string.err_somethings_wrong, Toast.LENGTH_LONG).show();
            finish();
        } else {
            setTitle((gameObj.getGameName() != null ? gameObj.getGameName() : ""));
            init();

            mSwipeRefreshLayout.setRefreshing(true);
            mSwipeRefreshLayout.setOnRefreshListener(() -> loadCheats(true));

            cheatsByGameRecycleListViewAdapter = new CheatsByGameRecycleListViewAdapter(this, tools, this);
            recyclerView.setAdapter(cheatsByGameRecycleListViewAdapter);

            getSupportActionBar().setTitle(gameObj.getGameName() != null ? gameObj.getGameName() : "");
            getSupportActionBar().setSubtitle(gameObj.getSystemName() != null ? gameObj.getSystemName() : "");

            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
            recyclerView.addItemDecoration(new DividerDecoration(this));
            recyclerView.getItemAnimator().setRemoveDuration(50);
            recyclerView.setHasFixedSize(true);
            recyclerView.showScrollbar();
        }
    }

    private void init() {
        cheatDatabaseApplication = CheatDatabaseApplication.getCurrentAppInstance();

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        appLovinAdView.loadNextAd();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.cheats_by_game_menu, menu);

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
    public boolean onPrepareOptionsMenu(final Menu menu) {
        menu.clear();
        if (tools.getMember() == null) {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                tools.removeValue(Konstanten.PREFERENCES_TEMP_GAME_OBJECT_VIEW);
                finish();
                return true;
            case R.id.action_add_to_favorites:
                tools.showSnackbar(outerLayout, getString(R.string.favorite_adding));
                addCheatsToFavoritesTask();
                return true;
            case R.id.action_submit_cheat:
                Intent explicitIntent = new Intent(CheatsByGameListActivity.this, SubmitCheatFormActivity.class);
                explicitIntent.putExtra("gameObj", gameObj);
                startActivity(explicitIntent);
                return true;
            case R.id.action_login:
                resultContract.launch(new Intent(CheatsByGameListActivity.this, LoginActivity.class));
                return true;
            case R.id.action_logout:
                tools.logout();
                tools.showSnackbar(outerLayout, getString(R.string.logout_ok));
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadCheats(boolean forceLoadOnline) {
        cheatList = new ArrayList<>();
        ArrayList<Cheat> cachedCheatsCollection;
        ArrayList<Cheat> cheatsFound;
        boolean isCached = false;
        String achievementsEnabled;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        isAchievementsEnabled = prefs.getBoolean("enable_achievements", true);

        if (isAchievementsEnabled) {
            achievementsEnabled = Konstanten.ACHIEVEMENTS;
        } else {
            achievementsEnabled = Konstanten.NO_ACHIEVEMENTS;
        }

        TreeMap<String, TreeMap<String, List<Cheat>>> cheatsByGameInCache = cheatDatabaseApplication.getCheatsByGameCached();
        TreeMap cheatListTree = null;
        if (cheatsByGameInCache.containsKey(String.valueOf(gameObj.getGameId()))) {

            cheatListTree = cheatsByGameInCache.get(String.valueOf(gameObj.getGameId()));
            if (cheatListTree != null) {

                if (cheatListTree.containsKey(achievementsEnabled)) {
                    cachedCheatsCollection = (ArrayList<Cheat>) cheatListTree.get(achievementsEnabled);

                    if ((cachedCheatsCollection != null) && (cachedCheatsCollection.size() > 0)) {
                        cheatsFound = cachedCheatsCollection;
                        gameObj.setCheatList(cheatsFound);

                        cheatList = cheatsFound;
                        isCached = true;
                    }
                }
            }
        }

        if (!isCached || forceLoadOnline) {
            int memberId = 0;
            if (tools.getMember() != null) {
                memberId = tools.getMember().getMid();
            }

            TreeMap finalCheatListTree = cheatListTree;

            Call<List<Cheat>> call = restApi.getCheatsAndRatings(gameObj.getGameId(), memberId, (isAchievementsEnabled ? 1 : 0));
            call.enqueue(new Callback<List<Cheat>>() {
                @Override
                public void onResponse(Call<List<Cheat>> cheats, Response<List<Cheat>> response) {
                    cheatList = (ArrayList<Cheat>) response.body();

                    gameObj.setCheatList(cheatList);

                    TreeMap<String, List<Cheat>> updatedCheatListForCache = new TreeMap<>();
                    updatedCheatListForCache.put(achievementsEnabled, cheatList);

                    String checkWhichSubKey;
                    if (achievementsEnabled.equalsIgnoreCase(Konstanten.ACHIEVEMENTS)) {
                        checkWhichSubKey = Konstanten.NO_ACHIEVEMENTS;
                    } else {
                        checkWhichSubKey = Konstanten.ACHIEVEMENTS;
                    }

                    if ((finalCheatListTree != null) && (finalCheatListTree.containsKey(checkWhichSubKey))) {
                        List<Cheat> existingGamesInCache = (List<Cheat>) finalCheatListTree.get(checkWhichSubKey);
                        updatedCheatListForCache.put(checkWhichSubKey, existingGamesInCache);
                    }

                    cheatsByGameInCache.put(String.valueOf(gameObj.getGameId()), updatedCheatListForCache);
                    cheatDatabaseApplication.setCheatsByGameCached(cheatsByGameInCache);

                    updateUI();
                }

                @Override
                public void onFailure(Call<List<Cheat>> call, Throwable e) {
                    Log.e(TAG, "getCheatList onFailure: " + e.getLocalizedMessage());
                    Needle.onMainThread().execute(() -> Toast.makeText(CheatsByGameListActivity.this, R.string.err_somethings_wrong, Toast.LENGTH_LONG).show());
                }
            });
        } else {
            updateUI();
        }
    }

    private void updateUI() {
        if (cheatList != null && cheatList.size() > 0) {
            cheatsByGameRecycleListViewAdapter.setCheatList(cheatList);
            cheatsByGameRecycleListViewAdapter.filterList("");
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
        EventBus.getDefault().unregister(this);
        Reachability.unregister(this);
        super.onStop();
    }

    @OnClick(R.id.add_new_cheat_button)
    void addNewCheat() {
        Intent explicitIntent = new Intent(CheatsByGameListActivity.this, SubmitCheatFormActivity.class);
        explicitIntent.putExtra("gameObj", gameObj);
        startActivity(explicitIntent);
    }

    void addCheatsToFavoritesTask() {
        FavoriteCheatDao dao = RoomCheatDatabase.getDatabase(this).favoriteDao();

        Call<List<Cheat>> call = restApi.getCheatsByGameId(gameObj.getGameId(), (isAchievementsEnabled ? 1 : 0));
        call.enqueue(new Callback<List<Cheat>>() {
            @Override
            public void onResponse(Call<List<Cheat>> cheats, Response<List<Cheat>> response) {
                List<Cheat> cheatList = response.body();

                for (Cheat cheat : cheatList) {
                    if (cheat.hasScreenshots()) {
                        // TODO FIXME: currently it ignores success/fail of saving screenshots to SD card...
                        // TODO FIXME: currently it ignores success/fail of saving screenshots to SD card...
                        tools.saveScreenshotsToSdCard(cheat, null);
                    }

                    Needle.onBackgroundThread().execute(() -> {
                        int memberId = 0;
                        if (tools.getMember() != null) {
                            memberId = tools.getMember().getMid();
                        }

                        dao.insert(cheat.toFavoriteCheatModel(memberId));
                    });

                }

                tools.showSnackbar(outerLayout, getString(R.string.add_favorites_ok));
            }

            @Override
            public void onFailure(Call<List<Cheat>> call, Throwable e) {
                Log.e(TAG, "insertFavoriteCheats onFailure: " + e.getLocalizedMessage());
                tools.showSnackbar(outerLayout, getString(R.string.error_adding_favorites));
            }
        });
    }

    @Override
    public void onCheatListItemSelected(Cheat cheat, int position) {
        if (Reachability.reachability.isReachable) {
            tools.putInt(Konstanten.PREFERENCES_PAGE_SELECTED, position);

            // Using local Preferences to pass data for large game objects
            // (instead of intent) such as Pokemon
            Intent explicitIntent = new Intent(CheatsByGameListActivity.this, CheatViewPageIndicatorActivity.class);
            explicitIntent.putExtra("gameObj", gameObj);
            explicitIntent.putExtra("selectedPage", position);
            explicitIntent.putExtra("layoutResourceId", R.layout.activity_cheatview_pager);
            startActivity(explicitIntent);
        } else {
            tools.showSnackbar(outerLayout, getString(R.string.no_internet));
        }
    }

}
