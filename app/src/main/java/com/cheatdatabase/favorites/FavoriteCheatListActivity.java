package com.cheatdatabase.favorites;

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

import com.cheatdatabase.R;
import com.cheatdatabase.SubmitCheatActivity;
import com.cheatdatabase.adapters.CheatsByGameRecycleListViewAdapter;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.events.CheatRatingFinishedEvent;
import com.cheatdatabase.favorites.handset.cheatview.FavoritesCheatViewPageIndicator;
import com.cheatdatabase.helpers.DatabaseHelper;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.listeners.OnCheatListItemSelectedListener;
import com.cheatdatabase.widgets.DividerDecoration;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.google.gson.Gson;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import needle.Needle;

public class FavoriteCheatListActivity extends AppCompatActivity implements OnCheatListItemSelectedListener {

    private final String TAG = this.getClass().getSimpleName();

    private Game gameObj;

    private CheatsByGameRecycleListViewAdapter cheatsByGameRecycleListViewAdapter;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Member member;

    private int lastPosition;
    private Game lastGameObj;
    private Cheat visibleCheat;

    private List<Cheat> cheatsArrayList;
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
        EventBus.getDefault().register(this);

        loadCheats();
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
            mSwipeRefreshLayout.setOnRefreshListener(() -> loadCheats());

            cheatsByGameRecycleListViewAdapter = new CheatsByGameRecycleListViewAdapter(this, this);
            recyclerView.setAdapter(cheatsByGameRecycleListViewAdapter);

            getSupportActionBar().setTitle(gameObj.getGameName());
            getSupportActionBar().setSubtitle(gameObj.getSystemName());

            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
            recyclerView.addItemDecoration(new DividerDecoration(this));
            recyclerView.getItemAnimator().setRemoveDuration(50);
            recyclerView.setHasFixedSize(true);
        }
    }

    private void init() {
        sharedPreferences = getSharedPreferences(Konstanten.PREFERENCES_FILE, MODE_PRIVATE);
        editor = sharedPreferences.edit();

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

        getMenuInflater().inflate(R.menu.handset_cheatview_rating_off_menu, menu);

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
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCheats() {
        Needle.onBackgroundThread().execute(() -> {
            cheatsArrayList = new ArrayList<>();

            if (gameObj != null) {
                if (gameObj.getCheatList() == null) {
                    DatabaseHelper db = new DatabaseHelper(this);
                    gameObj.setCheatList(db.getAllFavoritedCheatsByGame(gameObj.getGameId()));
                }

                cheatsArrayList = gameObj.getCheatList();

                updateUI();
            } else {
                error();
            }
        });
    }

    private void updateUI() {
        try {
            if (cheatsArrayList != null && cheatsArrayList.size() > 0) {
                cheatsByGameRecycleListViewAdapter.setCheatList(cheatsArrayList);
                cheatsByGameRecycleListViewAdapter.filterList("");
                cheatsByGameRecycleListViewAdapter.notifyDataSetChanged();
            } else {
                error();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
            error();
        }

        Needle.onMainThread().execute(() -> mSwipeRefreshLayout.setRefreshing(false));
    }

    // Save the position of the last element
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("position", lastPosition);
        outState.putParcelable("gameObj", lastGameObj);
        super.onSaveInstanceState(outState);
    }

    @Subscribe
    public void onEvent(CheatRatingFinishedEvent result) {
        visibleCheat.setMemberRating(result.getRating());
        Tools.showSnackbar(outerLayout, getString(R.string.rating_inserted));
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
        Intent explicitIntent = new Intent(FavoriteCheatListActivity.this, SubmitCheatActivity.class);
        explicitIntent.putExtra("gameObj", gameObj);
        startActivity(explicitIntent);
    }

    private void error() {
        Log.e(TAG, "Caught error: " + getPackageName() + "/" + getTitle());

        Needle.onMainThread().execute(() -> {
            new AlertDialog.Builder(this).setIcon(R.drawable.ic_action_warning).setTitle(getString(R.string.err)).setMessage(R.string.err_data_not_accessible).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    finish();
                }
            }).create().show();
        });
    }

    @Override
    public void onCheatListItemSelected(Cheat cheat, int position) {
        this.visibleCheat = cheat;
        this.lastGameObj = cheat.getGame();

        if (Reachability.reachability.isReachable) {
            // Using local Preferences to pass data for large game objects
            // (instead of intent) such as Pokemon

            int i = 0;
            for (Cheat c : gameObj.getCheatList()) {
                if (c == cheat) {
                    position = i;
                }
                i++;
            }

            editor.putInt(Konstanten.PREFERENCES_PAGE_SELECTED, position);
            editor.apply();

            Intent explicitIntent = new Intent(this, FavoritesCheatViewPageIndicator.class);
            explicitIntent.putExtra("gameObj", gameObj);
            explicitIntent.putExtra("position", position);
            explicitIntent.putExtra("layoutResourceId", R.layout.activity_cheatview_pager);
            startActivity(explicitIntent);
        } else {
            Tools.showSnackbar(outerLayout, getString(R.string.no_internet));
        }


    }
}
