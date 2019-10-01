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
import com.cheatdatabase.callbacks.RepositoryEntityListCallback;
import com.cheatdatabase.cheat_detail_view.CheatViewPageIndicatorActivity;
import com.cheatdatabase.helpers.DatabaseHelper;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.listeners.OnCheatListItemSelectedListener;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import needle.Needle;

public class CheatsByGameListActivity extends AppCompatActivity implements OnCheatListItemSelectedListener {

    private static String TAG = CheatsByGameListActivity.class.getSimpleName();

    private SharedPreferences sharedPreferences;
    private Editor editor;
    private Member member;
    private List<Cheat> cheatList;
    private Cheat visibleCheat;

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

    private void loadCheats(boolean forceLoadOnline) {
        Log.d(TAG, "XXXXX loadCheats() by game: " + gameObj.getGameName() + " / " + gameObj.getGameId());

        cheatList = new ArrayList<>();
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

        TreeMap<String, TreeMap<String, List<Cheat>>> cheatsByGameInCache = cheatDatabaseApplication.getCheatsByGameCached();
        TreeMap cheatListTree = null;
        if (cheatsByGameInCache.containsKey(String.valueOf(gameObj.getGameId()))) {

            cheatListTree = cheatsByGameInCache.get(String.valueOf(gameObj.getGameId()));
            if (cheatListTree != null) {

                if (cheatListTree.containsKey(achievementsEnabled)) {
                    cachedCheatsCollection = (List<Cheat>) cheatListTree.get(achievementsEnabled);

                    if ((cachedCheatsCollection != null) && (cachedCheatsCollection.size() > 0)) {
                        cheatsFound = cachedCheatsCollection;
                        gameObj.setCheatList(cheatsFound);
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

            Webservice.getCheatList(gameObj, memberId, isAchievementsEnabled, new RepositoryEntityListCallback<Cheat>() {
                @Override
                public void onSuccess(List<Cheat> cheatEntityList) {
                    gameObj.setCheatList(cheatEntityList);

                    TreeMap<String, List<Cheat>> updatedCheatListForCache = new TreeMap<>();
                    updatedCheatListForCache.put(achievementsEnabled, cheatEntityList);

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

                    cheatList = cheatEntityList;

                    updateUI();
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "getCheatList onFailure: " + e.getLocalizedMessage());
                    Needle.onMainThread().execute(() -> Toast.makeText(CheatsByGameListActivity.this, R.string.err_somethings_wrong, Toast.LENGTH_LONG).show());
                }
            });
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

    @Override
    public void onCheatListItemSelected(Cheat cheat, int position) {
        this.visibleCheat = cheat;

        if (Reachability.reachability.isReachable) {

            int i = 0;
            for (Cheat c : gameObj.getCheatList()) {
                if (c == cheat) {
                    position = i;
                }
                i++;
            }

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
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }
}
