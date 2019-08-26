package com.cheatdatabase;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.cheatdatabase.adapters.GamesBySystemRecycleListViewAdapter;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.businessobjects.SystemPlatform;
import com.cheatdatabase.callbacks.RepositoryEntityListCallback;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.listeners.OnGameListItemSelectedListener;
import com.cheatdatabase.widgets.DividerDecoration;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.google.gson.Gson;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import needle.Needle;

public class GamesBySystemListActivity extends AppCompatActivity implements OnGameListItemSelectedListener {

    private static final String TAG = GamesBySystemListActivity.class.getSimpleName();
    private static final int AD_POSITION = 4;

    private List<Game> gameList;
    private CheatDatabaseApplication cheatDatabaseApplication;
    private GamesBySystemRecycleListViewAdapter gamesBySystemRecycleListViewAdapter;
    private SystemPlatform systemObj;
    private Member member;
    private AdView facebookAdView;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

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

        systemObj = getIntent().getParcelableExtra("systemObj");
        if (systemObj == null) {
            Toast.makeText(this, R.string.err_somethings_wrong, Toast.LENGTH_LONG).show();
            finish();
        } else {
            setTitle((systemObj.getSystemName() != null ? systemObj.getSystemName() : ""));
            init();

            mSwipeRefreshLayout.setRefreshing(true);
            mSwipeRefreshLayout.setOnRefreshListener(() -> loadGames(true));

            gamesBySystemRecycleListViewAdapter = new GamesBySystemRecycleListViewAdapter(this, this);
            recyclerView.setAdapter(gamesBySystemRecycleListViewAdapter);

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
                Tools.logout(GamesBySystemListActivity.this, editor);
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadGames(boolean forceLoadOnline) {
        Log.d(TAG, "XXXXX loadGames() System ID/NAME: " + systemObj.getSystemId() + "/" + systemObj.getSystemName());

//        final List<ListItem> newListItems = new ArrayList<>();

        // TODO eine GameListItem liste erstellen und verwenden damit man einen ListItem type unterschied machen kann (game list item & native ad item)
        // TODO eine GameListItem liste erstellen und verwenden damit man einen ListItem type unterschied machen kann (game list item & native ad item)

        gameList = new ArrayList<>();
        List<Game> cachedGamesCollection;
        boolean isCached = false;
        String achievementsEnabled;
        boolean isAchievementsEnabled = sharedPreferences.getBoolean("enable_achievements", true);

        if (isAchievementsEnabled) {
            achievementsEnabled = Konstanten.ACHIEVEMENTS;
        } else {
            achievementsEnabled = Konstanten.NO_ACHIEVEMENTS;
        }

        TreeMap gameListTree = null;
        TreeMap<String, TreeMap<String, List<Game>>> gamesBySystemInCache = cheatDatabaseApplication.getGamesBySystemCached();
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

        Log.d(TAG, "XXXXX loadGames() 02 isCached: " + isCached);
        Log.d(TAG, "XXXXX loadGames() 02 gameList: " + gameList.size());

        if (!isCached || forceLoadOnline || gameList.size() == 0) {
            gameList = new ArrayList<>();
            TreeMap finalGameListTree = gameListTree;

            // TODO FIXME hier das CALLBACK List<Game> irgendwie noch fixen
            // TODO FIXME hier das CALLBACK List<Game> irgendwie noch fixen
            // TODO FIXME hier das CALLBACK List<Game> irgendwie noch fixen
            // TODO FIXME hier das CALLBACK List<Game> irgendwie noch fixen

            Webservice.getGameListBySystemId(systemObj.getSystemId(), systemObj.getSystemName(), isAchievementsEnabled, new RepositoryEntityListCallback<Game>() {
                @Override
                public void onSuccess(List<Game> gameEntityList) {
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
                    cheatDatabaseApplication.setGamesBySystemCached(gamesBySystemInCache);

                    gameList = gameEntityList;

                    updateUI();
                }

                @Override
                public void onFailure(Exception e) {
                    error();
                }
            });

        }
    }

    private void updateUI() {
        if (gameList != null && gameList.size() > 0) {
            gamesBySystemRecycleListViewAdapter.setGameList(gameList);
            gamesBySystemRecycleListViewAdapter.filterList("");

            //gamesBySystemRecycleListViewAdapter.notifyDataSetChanged();
        } else {
            error();
        }

        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void error() {
        Needle.onMainThread().execute(() -> {
            Log.e(TAG, "Caught error: " + getPackageName() + "/" + getTitle());
            new AlertDialog.Builder(GamesBySystemListActivity.this).setIcon(R.drawable.ic_action_warning).setTitle(getString(R.string.err)).setMessage(R.string.err_data_not_accessible).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    finish();
                }
            }).create().show();
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

    @Override
    public void onGameListItemSelected(Game game) {
        Log.d(TAG, "XXXXX onGameListItemSelected");

        if (Reachability.reachability.isReachable) {
            Intent intent = new Intent(this, CheatsByGameListActivity.class);
            intent.putExtra("gameObj", game);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }
}
