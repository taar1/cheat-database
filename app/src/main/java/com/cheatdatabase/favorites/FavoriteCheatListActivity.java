package com.cheatdatabase.favorites;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.R;
import com.cheatdatabase.SubmitCheatActivity;
import com.cheatdatabase.adapters.CheatsByGameRecycleListViewAdapter;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.dialogs.RateCheatMaterialDialog;
import com.cheatdatabase.events.CheatListRecyclerViewClickEvent;
import com.cheatdatabase.events.CheatRatingFinishedEvent;
import com.cheatdatabase.favorites.handset.cheatview.FavoritesCheatViewPageIndicator;
import com.cheatdatabase.helpers.CheatDatabaseAdapter;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.widgets.DividerDecoration;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.google.gson.Gson;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import needle.Needle;

public class FavoriteCheatListActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    private Game gameObj;

    private CheatsByGameRecycleListViewAdapter cheatsByGameRecycleListViewAdapter;

    @BindView(R.id.my_recycler_view)
    FastScrollRecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.item_list_empty_view)
    TextView mEmptyView;
    @BindView(R.id.items_list_load_progress)
    ProgressBarCircularIndeterminate mProgressView;

    @BindView(R.id.banner_container)
    LinearLayout facebookBanner;
    private AdView adView;

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private Member member;

    private int lastPosition;
    private Game lastGameObj;
    private Cheat visibleCheat;

    private CheatDatabaseAdapter db;
    private List<Cheat> cheatsArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameObj = (Game) getIntent().getParcelableExtra("gameObj");

        setContentView(R.layout.activity_cheat_list);
        setTitle(gameObj.getGameName());

        init();

        cheatsByGameRecycleListViewAdapter = new CheatsByGameRecycleListViewAdapter();

        mSwipeRefreshLayout.setRefreshing(true);

        getSupportActionBar().setTitle(gameObj.getGameName());
        getSupportActionBar().setSubtitle(gameObj.getSystemName());

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                mRecyclerView.showLoading();
                getCheats();
            }
        });


        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new DividerDecoration(this));
        mRecyclerView.getItemAnimator().setRemoveDuration(50);
//        mRecyclerView.setEmptyView(mEmptyView);
//        mRecyclerView.setLoadingView(mProgressView);
        mRecyclerView.setHasFixedSize(true);

        if (Reachability.reachability.isReachable) {
//            mRecyclerView.showLoading();
            getCheats();
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
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

        settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();

        db = new CheatDatabaseAdapter(this);
        db.open();

        if (member == null) {
            member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Dominik: This is actually not needed because I am saving the
                // selected Fragment ID in the local storage.
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is NOT part of this app's task, so create a new
                    // task when navigating up, with a synthesized back stack.
                    // Add all of this activity's parents to the back stack
                    TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
    public boolean onPrepareOptionsMenu(Menu menu) {

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

    // Save the position of the last element
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("position", lastPosition);
        outState.putParcelable("gameObj", lastGameObj);
        super.onSaveInstanceState(outState);
    }

//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        getIntent().putExtra("position", savedInstanceState.getInt("position"));
//        getIntent().putExtra("gameObj", savedInstanceState.getSerializable("gameObj"));
//        super.onRestoreInstanceState(savedInstanceState);
//    }

    public void showRatingDialog() {
        if ((member == null) || (member.getMid() == 0)) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_LONG).show();
        } else {
            new RateCheatMaterialDialog(this, visibleCheat, member);
        }
    }

//    @Override
//    public void onFinishRateCheatDialog(int selectedRating) {
//        visibleCheat.setMemberRating(selectedRating);
//        favoritesDetailsFragment.updateMemberCheatRating(selectedRating);
//
//        // FIXME make the star to highlighton all fragments
//        favoritesDetailsFragment.highlightRatingIcon(true);
//        favoritesCheatMetaFragment.highlightRatingIcon(true);
//        favoritesCheatForumFragment.highlightRatingIcon(true);
//
//        Toast.makeText(this, R.string.rating_inserted, Toast.LENGTH_SHORT).show();
//    }

    @Subscribe
    public void onEvent(CheatRatingFinishedEvent result) {
        visibleCheat.setMemberRating(result.getRating());
//        favoritesDetailsFragment.updateMemberCheatRating(result.getRating());
//
//        // FIXME make the star to highlighton all fragments
//        favoritesDetailsFragment.highlightRatingIcon(true);
//        favoritesCheatMetaFragment.highlightRatingIcon(true);
//        favoritesCheatForumFragment.highlightRatingIcon(true);

        Toast.makeText(this, R.string.rating_inserted, Toast.LENGTH_SHORT).show();
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
    public void onEvent(CheatListRecyclerViewClickEvent result) {
        if (result.isSucceeded()) {
            this.visibleCheat = result.getCheat();
            this.lastGameObj = result.getCheat().getGame();

            // editor.putString(Konstanten.PREFERENCES_TEMP_GAME_OBJECT_VIEW,
            // new Gson().toJson(gameObj));
            editor.putInt(Konstanten.PREFERENCES_PAGE_SELECTED, result.getPosition());
            editor.commit();

            if (Reachability.reachability.isReachable) {
                // Using local Preferences to pass data for large game objects
                // (instead of intent) such as Pokemon

                Intent explicitIntent = new Intent(this, FavoritesCheatViewPageIndicator.class);
                explicitIntent.putExtra("gameObj", gameObj);
                explicitIntent.putExtra("position", result.getPosition());
                explicitIntent.putExtra("layoutResourceId", R.layout.activity_cheatview_pager);
                startActivity(explicitIntent);
            } else {
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.add_new_cheat_button)
    void clickAddNewCheat() {
        Intent explicitIntent = new Intent(FavoriteCheatListActivity.this, SubmitCheatActivity.class);
        explicitIntent.putExtra("gameObj", gameObj);
        startActivity(explicitIntent);
    }

    void getCheats() {
        Needle.onBackgroundThread().execute(() -> {
            cheatsArrayList = new ArrayList<>();

            if (gameObj != null) {
                if (gameObj.getCheatList() == null) {
                    gameObj.setCheatList(db.getAllFavoritedCheatsByGame(gameObj.getGameId()));
                }

                fillListWithCheats();
            } else {
                error();
            }
        });
    }

    @MainThread
    public void fillListWithCheats() {
        // TODO FIXME schauen ob man das in eine Needle packen muss

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
//        mRecyclerView.hideLoading();
    }

    private void error() {
        Log.e(TAG, "caught error: " + getPackageName() + "/" + getTitle());
        new AlertDialog.Builder(this).setIcon(R.drawable.ic_action_warning).setTitle(getString(R.string.err)).setMessage(R.string.err_data_not_accessible).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        }).create().show();
    }


}
