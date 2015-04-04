package com.cheatdatabase.favorites;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.dialogs.RateCheatDialog;
import com.cheatdatabase.dialogs.RateCheatDialog.RateCheatDialogListener;
import com.cheatdatabase.favorites.handset.cheatview.FavoritesCheatViewPageIndicator;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;
import com.google.analytics.tracking.android.Tracker;
import com.google.gson.Gson;
import com.mopub.mobileads.MoPubView;
import com.splunk.mint.Mint;

@SuppressLint("NewApi")
public class FavoriteCheatListActivity extends ActionBarActivity implements FavoriteCheatListFragment.ElementsListClickHandler, RateCheatDialogListener {

    String tag = this.getClass().getSimpleName();

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private static final String SCREEN_LABEL = "Favorite Cheat List By Game ID Screen";
    protected Tracker tracker;

    private SharedPreferences settings;
    private Member member;
    private Game gameObj;

    private ProgressDialog cheatProgressDialog = null;

    private int lastPosition;
    private Game lastGameObj;
    private Cheat visibleCheat;
    private FavoritesDetailsFragment favoritesDetailsFragment;
    private FavoritesCheatForumFragment cheatForumFragment;
    private FavoritesCheatMetaFragment cheatMetaFragment;
    private Toolbar mToolbar;
    private MoPubView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_list);

        handleIntent();
        init();

        cheatProgressDialog = ProgressDialog.show(FavoriteCheatListActivity.this, getString(R.string.please_wait) + "...", getString(R.string.retrieving_data) + "...", true);

        if (findViewById(R.id.favorite_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            FavoriteCheatListFragment ff = ((FavoriteCheatListFragment) getSupportFragmentManager().findFragmentById(R.id.favorite_list));
            // ff.setArguments(bundle);
            ff.setActivateOnItemClick(true);
        }

        cheatProgressDialog.dismiss();
        // TODO: If exposing deep links into your app, handle intents here.
    }

    private void init() {
        Mint.initAndStartSession(this, Konstanten.SPLUNK_MINT_API_KEY);

        settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);

        mToolbar = Tools.initToolbarBase(this, mToolbar);
        mAdView = Tools.initMoPubAdView(this, mAdView);

        Tools.initGA(FavoriteCheatListActivity.this, tracker, SCREEN_LABEL, "Favorites Cheat List", gameObj.getGameName());

        if (member == null) {
            member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
        }
    }

    private void handleIntent() {

        new Thread(new Runnable() {

            @Override
            public void run() {
                gameObj = (Game) getIntent().getSerializableExtra("gameObj");

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        getSupportActionBar().setTitle(gameObj.getGameName());
                        getSupportActionBar().setSubtitle(gameObj.getSystemName());
                    }

                });
            }
        }).start();
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

    /**
     * Callback method from
     * {@link FavoriteCheatListFragment.ElementsListClickHandler} indicating
     * that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(int position) {
        this.lastPosition = position;
        this.lastGameObj = gameObj;
        this.visibleCheat = gameObj.getCheats()[position];

        if (mTwoPane) {

            Log.i(tag, "IS DUALPANE");
            cheatForumFragment = new FavoritesCheatForumFragment();
            cheatMetaFragment = new FavoritesCheatMetaFragment();

            Bundle element = new Bundle();
            element.putInt("position", position);
            element.putSerializable("gameObj", gameObj);
            element.putSerializable("cheatObj", visibleCheat);
            element.putString("favoritesCheatForumFragment", new Gson().toJson(cheatForumFragment));
            element.putString("favoritesCheatMetaFragment", new Gson().toJson(cheatMetaFragment));

            favoritesDetailsFragment = new FavoritesDetailsFragment();
            favoritesDetailsFragment.setArguments(element);
            cheatForumFragment.setArguments(element);
            cheatMetaFragment.setArguments(element);

            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.favorite_detail_container, favoritesDetailsFragment);
            transaction.addToBackStack(null);
            transaction.commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            // Intent detailIntent = new Intent(this,
            // FavoriteCheatDetailActivity.class);
            // detailIntent.putExtra(FavoriteCheatDetailFragment.ARG_ITEM_ID,
            // id);
            // detailIntent.putExtra("gameObj", gameObj);
            // detailIntent.putExtra("position", id);
            // // detailIntent.putExtra("bundle", bundle);
            // startActivity(detailIntent);

            Intent intent = new Intent(this, FavoritesCheatViewPageIndicator.class);
            intent.putExtra("layoutResourceId", R.layout.activity_cheatview_pager);
            intent.putExtra("position", position);
            intent.putExtra("gameObj", gameObj);
            startActivity(intent);

        }
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

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        getIntent().putExtra("position", savedInstanceState.getInt("position"));
        getIntent().putExtra("gameObj", savedInstanceState.getSerializable("gameObj"));
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void showRatingDialog() {
        if ((member == null) || (member.getMid() == 0)) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_LONG).show();
        } else {
            Bundle args = new Bundle();
            args.putSerializable("cheatObj", visibleCheat);

            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            RateCheatDialog ratingCheatDialog = new RateCheatDialog();
            ratingCheatDialog.setArguments(args);
            ratingCheatDialog.show(fm, "fragment_rating_cheat");
        }
    }

    @Override
    public void onFinishRateCheatDialog(int selectedRating) {
        visibleCheat.setMemberRating(selectedRating);
        favoritesDetailsFragment.updateMemberCheatRating(selectedRating);

        // FIXME make the star to highlighton all fragments
        favoritesDetailsFragment.highlightRatingIcon(true);
        cheatMetaFragment.highlightRatingIcon(true);
        cheatForumFragment.highlightRatingIcon(true);

        Toast.makeText(this, R.string.rating_inserted, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }
}
