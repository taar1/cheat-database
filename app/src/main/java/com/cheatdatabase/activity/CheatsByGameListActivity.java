package com.cheatdatabase.activity;

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

import com.cheatdatabase.CheatDatabaseApplication;
import com.cheatdatabase.R;
import com.cheatdatabase.adapters.CheatsByGameRecycleListViewAdapter;
import com.cheatdatabase.cheatdetailview.CheatViewPageIndicatorActivity;
import com.cheatdatabase.data.RoomCheatDatabase;
import com.cheatdatabase.data.dao.FavoriteCheatDao;
import com.cheatdatabase.data.model.Cheat;
import com.cheatdatabase.data.model.Game;
import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.listeners.OnCheatListItemSelectedListener;
import com.cheatdatabase.rest.RestApi;
import com.cheatdatabase.widgets.DividerDecoration;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdsManager;
import com.google.gson.Gson;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import needle.Needle;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CheatsByGameListActivity extends AppCompatActivity implements OnCheatListItemSelectedListener {

    private static String TAG = CheatsByGameListActivity.class.getSimpleName();

    private SharedPreferences sharedPreferences;
    private Editor editor;
    private Member member;
    private ArrayList<Cheat> cheatList;

    private CheatDatabaseApplication cheatDatabaseApplication;
    private CheatsByGameRecycleListViewAdapter cheatsByGameRecycleListViewAdapter;

    private Game gameObj;
    private AdView facebookAdView;

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
    @BindView(R.id.banner_container)
    LinearLayout bannerContainerFacebook;

    @Inject
    Retrofit retrofit;

    private RestApi restApi;

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

        NativeAdsManager nativeAdsManager = new NativeAdsManager(this, Konstanten.FACEBOOK_AUDIENCE_NETWORK_NATIVE_AD_IN_RECYCLER_VIEW, 5);
        nativeAdsManager.loadAds(NativeAd.MediaCacheFlag.ALL);

        gameObj = getIntent().getParcelableExtra("gameObj");

        ((CheatDatabaseApplication) getApplication()).getNetworkComponent().inject(this);
        restApi = retrofit.create(RestApi.class);

        if (gameObj == null) {
            Toast.makeText(this, R.string.err_somethings_wrong, Toast.LENGTH_LONG).show();
            finish();
        } else {
            setTitle((gameObj.getGameName() != null ? gameObj.getGameName() : ""));
            init();

            mSwipeRefreshLayout.setRefreshing(true);
            mSwipeRefreshLayout.setOnRefreshListener(() -> loadCheats(true));

            cheatsByGameRecycleListViewAdapter = new CheatsByGameRecycleListViewAdapter(this, nativeAdsManager, this);
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
        sharedPreferences = getSharedPreferences(Konstanten.PREFERENCES_FILE, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        cheatDatabaseApplication = CheatDatabaseApplication.getCurrentAppInstance();

        facebookAdView = new AdView(this, Konstanten.FACEBOOK_AUDIENCE_NETWORK_NATIVE_BANNER_ID, AdSize.BANNER_HEIGHT_50);
        bannerContainerFacebook.addView(facebookAdView);
        facebookAdView.loadAd();

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (member == null) {
            member = new Gson().fromJson(sharedPreferences.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                editor.remove(Konstanten.PREFERENCES_TEMP_GAME_OBJECT_VIEW);
                editor.apply();
                finish();
                return true;
            case R.id.action_add_to_favorites:
                Tools.showSnackbar(outerLayout, getString(R.string.favorite_adding));
                addCheatsToFavoritesTask();
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

    private void loadCheats(boolean forceLoadOnline) {
        cheatList = new ArrayList<>();
        ArrayList<Cheat> cachedCheatsCollection;
        ArrayList<Cheat> cheatsFound;
        boolean isCached = false;
        String achievementsEnabled;
        boolean isAchievementsEnabled = sharedPreferences.getBoolean("enable_achievements", true);

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
            if (member != null) {
                memberId = member.getMid();
            }

            TreeMap finalCheatListTree = cheatListTree;

            Call<List<Cheat>> call = restApi.getCheatsAndRatings(gameObj.getGameId(), memberId, (isAchievementsEnabled ? 1 : 0));
            call.enqueue(new Callback<List<Cheat>>() {
                @Override
                public void onResponse(Call<List<Cheat>> cheats, Response<List<Cheat>> response) {
                    cheatList = (ArrayList) response.body();

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
            Tools.showSnackbar(outerLayout, getString(R.string.err_data_not_accessible));
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

    @Override
    protected void onDestroy() {
        if (facebookAdView != null) {
            facebookAdView.destroy();
        }
        super.onDestroy();
    }

    @OnClick(R.id.add_new_cheat_button)
    void addNewCheat() {
        Intent explicitIntent = new Intent(CheatsByGameListActivity.this, SubmitCheatActivity.class);
        explicitIntent.putExtra("gameObj", gameObj);
        startActivity(explicitIntent);
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

    void addCheatsToFavoritesTask() {
        FavoriteCheatDao dao = RoomCheatDatabase.getDatabase(this).favoriteDao();

        Call<List<Cheat>> call = restApi.getCheatsByGameId(gameObj.getGameId(), sharedPreferences.getBoolean("enable_achievements", true));
        call.enqueue(new Callback<List<Cheat>>() {
            @Override
            public void onResponse(Call<List<Cheat>> cheats, Response<List<Cheat>> response) {
                List<Cheat> cheatList = response.body();

                for (Cheat cheat : cheatList) {
                    if (cheat.hasScreenshots()) {
                        // TODO FIXME: currently it ignores success/fail of saving screenshots to SD card...
                        // TODO FIXME: currently it ignores success/fail of saving screenshots to SD card...
                        Tools.saveScreenshotsToSdCard(cheat, null);
                    }

                    Needle.onBackgroundThread().execute(() -> {
                        int memberId = 0;
                        if (member != null) {
                            memberId = member.getMid();
                        }

                        dao.insert(cheat.toFavoriteCheatModel(memberId));
                    });

                }

                Tools.showSnackbar(outerLayout, getString(R.string.add_favorites_ok));
            }

            @Override
            public void onFailure(Call<List<Cheat>> call, Throwable e) {
                Log.e(TAG, "insertFavoriteCheats onFailure: " + e.getLocalizedMessage());
                Tools.showSnackbar(outerLayout, getString(R.string.error_adding_favorites));
            }
        });
    }

    @Override
    public void onCheatListItemSelected(Cheat cheat, int position) {
        if (Reachability.reachability.isReachable) {
            editor.putInt(Konstanten.PREFERENCES_PAGE_SELECTED, position);
            editor.apply();

            // Using local Preferences to pass data for large game objects
            // (instead of intent) such as Pokemon
            Intent explicitIntent = new Intent(CheatsByGameListActivity.this, CheatViewPageIndicatorActivity.class);
            explicitIntent.putExtra("gameObj", gameObj);
            explicitIntent.putExtra("selectedPage", position);
            explicitIntent.putExtra("layoutResourceId", R.layout.activity_cheatview_pager);
            startActivity(explicitIntent);
        } else {
            Tools.showSnackbar(outerLayout, getString(R.string.no_internet));
        }
    }
}
