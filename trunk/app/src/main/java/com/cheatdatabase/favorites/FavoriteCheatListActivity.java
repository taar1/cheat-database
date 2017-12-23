package com.cheatdatabase.favorites;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.R;
import com.cheatdatabase.SubmitCheatActivity_;
import com.cheatdatabase.adapters.CheatsByGameRecycleListViewAdapter;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.dialogs.RateCheatDialog;
import com.cheatdatabase.events.CheatListRecyclerViewClickEvent;
import com.cheatdatabase.events.CheatRatingFinishedEvent;
import com.cheatdatabase.favorites.handset.cheatview.FavoritesCheatViewPageIndicator;
import com.cheatdatabase.helpers.CheatDatabaseAdapter;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.widgets.DividerDecoration;
import com.cheatdatabase.widgets.EmptyRecyclerView;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.google.gson.Gson;
import com.mopub.mobileads.MoPubView;
import com.splunk.mint.Mint;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;

@EActivity(R.layout.activity_cheat_list)
public class FavoriteCheatListActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    @Extra
    Game gameObj;

    @ViewById(R.id.my_recycler_view)
    EmptyRecyclerView mRecyclerView;

    @ViewById(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Bean
    Tools tools;

    @ViewById(R.id.adview)
    MoPubView mAdView;

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    @Bean
    CheatsByGameRecycleListViewAdapter cheatsByGameRecycleListViewAdapter;

    @ViewById(R.id.item_list_empty_view)
    TextView mEmptyView;

    @ViewById(R.id.items_list_load_progress)
    ProgressBarCircularIndeterminate mProgressView;

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private Member member;

    private int lastPosition;
    private Game lastGameObj;
    private Cheat visibleCheat;

    private CheatDatabaseAdapter db;
    private ArrayList<Cheat> cheatsArrayList;

    @AfterViews
    public void createView() {
        init();
        mSwipeRefreshLayout.setRefreshing(true);

        setTitle(gameObj.getGameName());
        getSupportActionBar().setTitle(gameObj.getGameName());
        getSupportActionBar().setSubtitle(gameObj.getSystemName());

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRecyclerView.showLoading();
                getCheats();
            }
        });


        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new DividerDecoration(this));
        mRecyclerView.getItemAnimator().setRemoveDuration(50);
        mRecyclerView.setEmptyView(mEmptyView);
        mRecyclerView.setLoadingView(mProgressView);
        mRecyclerView.setHasFixedSize(true);

        if (Reachability.reachability.isReachable) {
            mRecyclerView.showLoading();
            getCheats();
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }

    }

    private void init() {
        //CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "Favorites Cheat List").setLabel(TAG).build());

        Mint.initAndStartSession(this, Konstanten.SPLUNK_MINT_API_KEY);

        tools.loadAd(mAdView, getString(R.string.screen_type));

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
        outState.putSerializable("gameObj", lastGameObj);
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
            Bundle args = new Bundle();
            args.putSerializable("cheatObj", visibleCheat);

            FragmentManager fm = getSupportFragmentManager();
            RateCheatDialog ratingCheatDialog = new RateCheatDialog();
            ratingCheatDialog.setArguments(args);
            ratingCheatDialog.show(fm, "fragment_rating_cheat");
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
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Subscribe
    public void onEvent(CheatListRecyclerViewClickEvent result) {
        if (result.isSucceeded()) {
            //CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "click").setLabel(result.getCheat().getCheatTitle()).build());

            this.visibleCheat = result.getCheat();
            this.lastGameObj = result.getCheat().getGame();

            //CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "select_cheat").setLabel(result.getCheat().getGameName() + ": " + result.getCheat().getCheatTitle()).build());

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

    @Click(R.id.add_new_cheat_button)
    void addNewCheat() {
        SubmitCheatActivity_.intent(FavoriteCheatListActivity.this).gameObj(gameObj).start();
    }

    @Background
    void getCheats() {
        cheatsArrayList = new ArrayList<>();

        if (gameObj != null) {
            if (gameObj.getCheats() == null) {
                Collections.addAll(cheatsArrayList, db.getAllFavoritedCheatsByGame(gameObj.getGameId()));
            } else {
                Collections.addAll(cheatsArrayList, gameObj.getCheats());
            }

            Cheat[] cheats = new Cheat[cheatsArrayList.size()];
            for (int i = 0; i < cheatsArrayList.size(); i++) {
                cheats[i] = cheatsArrayList.get(i);
            }

            gameObj.setCheats(cheats);

            fillListWithCheats();
        } else {
            error();
        }
    }

    @UiThread
    public void fillListWithCheats() {
        try {
            if (cheatsArrayList != null && cheatsArrayList.size() > 0) {
                cheatsByGameRecycleListViewAdapter.init(cheatsArrayList);
                mRecyclerView.setAdapter(cheatsByGameRecycleListViewAdapter);

                cheatsByGameRecycleListViewAdapter.notifyDataSetChanged();
            } else {
                error();
            }
        } catch (Exception e) {
            error();
        }

        mSwipeRefreshLayout.setRefreshing(false);
        mRecyclerView.hideLoading();
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
