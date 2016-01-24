package com.cheatdatabase;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.adapters.CheatRecycleListViewAdapter;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.dialogs.RateCheatDialog;
import com.cheatdatabase.dialogs.ReportCheatDialog;
import com.cheatdatabase.events.CheatListRecyclerViewClickEvent;
import com.cheatdatabase.events.CheatRatingFinishedEvent;
import com.cheatdatabase.events.CheatReportingFinishedEvent;
import com.cheatdatabase.handset.cheatview.CheatViewPageIndicator;
import com.cheatdatabase.helpers.CheatDatabaseAdapter;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.widgets.DividerDecoration;
import com.cheatdatabase.widgets.EmptyRecyclerView;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.google.android.gms.analytics.HitBuilders;
import com.google.gson.Gson;
import com.mopub.mobileads.MoPubView;
import com.splunk.mint.Mint;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Collections;

/**
 * An activity representing a list of Cheats. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link "CheatDetailActivity"} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link CheatListFragment} and the item details (if present) is a
 * {@link "CheatDetailFragment"}.
 * <p/>
 * This activity also implements the required
 * {@link com.cheatdatabase.CheatListFragment.CheatListClickCallbacks} interface to listen for item selections.
 */
@EActivity(R.layout.activity_cheat_list)
public class CheatListActivity extends AppCompatActivity {

    private static String TAG = CheatListActivity.class.getSimpleName();

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
    CheatRecycleListViewAdapter cheatRecycleListViewAdapter;

    @ViewById(R.id.item_list_empty_view)
    TextView mEmptyView;

    @ViewById(R.id.items_list_load_progress)
    ProgressBarCircularIndeterminate mProgressView;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private static final String SCREEN_LABEL = "Cheat List By Game ID Screen";

    private SharedPreferences settings;
    private Editor editor;
    private Member member;

    private ArrayList<Cheat> cheatsArrayList;

    private int lastPosition;
    private Game lastGameObj;
    private Cheat visibleCheat;

    private CheatDetailTabletFragment cheatDetailFragment;
    private CheatForumFragment cheatForumFragment;
    private CheatDetailTabletMetaFragment cheatDetailTabletMetaFragment;

    private ShareActionProvider mShareActionProvider;


    public ShareActionProvider getmShareActionProvider() {
        return mShareActionProvider;
    }

    @AfterViews
    public void createView() {
        init();
        mSwipeRefreshLayout.setRefreshing(true);

        setTitle(gameObj.getGameName());
        getSupportActionBar().setTitle(gameObj.getGameName());
        getSupportActionBar().setSubtitle(gameObj.getSystemName());

        CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "loaded").setLabel(TAG).build());

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
            Toast.makeText(CheatListActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }


//        if (findViewById(R.id.cheat_detail_container) != null) {
//            // The detail container view will be present only in the
//            // large-screen layouts (res/values-large and
//            // res/values-sw600dp). If this view is present, then the
//            // activity should be in two-pane mode.
//            mTwoPane = true;
//
//            // In two-pane mode, list items should be given the
//            // 'activated' state when touched.
//            //((CheatListFragment) getSupportFragmentManager().findFragmentById(R.id.cheat_list)).setActivateOnItemClick(true);
//
//            // cheatListFragment scheint nicht gebraucht zu werden hier?!
//            // Bundle args = new Bundle();
//            // args.putSerializable("gameObj", gameObj)
//            // cheatListFragment = ((CheatListFragment)
//            // getSupportFragmentManager().findFragmentById(R.id.cheat_list));
//            // cheatListFragment.setActivateOnItemClick(true);
//            // cheatListFragment.setArguments(args);
//        }

    }

    private void init() {
        Reachability.registerReachability(this);
        Mint.initAndStartSession(this, Konstanten.SPLUNK_MINT_API_KEY);
        CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "Cheat List").setLabel("activity").build());

        tools.loadAd(mAdView, getString(R.string.screen_type));

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();

        if (member == null) {
            member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
        }
    }

    @Background
    public void getCheats() {
        Log.d(TAG, "getCheats() GAME: " + gameObj.getGameName() + " / " + gameObj.getGameId());

        cheatsArrayList = new ArrayList<>();
        Cheat[] cheats;

        if (member == null) {
            cheats = Webservice.getCheatList(gameObj, 0);
        } else {
            cheats = Webservice.getCheatList(gameObj, member.getMid());
        }
        gameObj.setCheats(cheats);

        Collections.addAll(cheatsArrayList, cheats);
        fillListWithGames();
    }

    @UiThread
    public void fillListWithGames() {
        try {
            if (cheatsArrayList != null && cheatsArrayList.size() > 0) {
                cheatRecycleListViewAdapter.init(cheatsArrayList);
                mRecyclerView.setAdapter(cheatRecycleListViewAdapter);

                cheatRecycleListViewAdapter.notifyDataSetChanged();
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
        new AlertDialog.Builder(CheatListActivity.this).setIcon(R.drawable.ic_action_warning).setTitle(getString(R.string.err)).setMessage(R.string.err_data_not_accessible).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        }).create().show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Reachability.registerReachability(this);
        CheatDatabaseApplication.getEventBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Reachability.unregister(this);
        CheatDatabaseApplication.getEventBus().unregister(this);
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // schauen wie man status persistent halten kann von fragment
        outState.putSerializable("gameObj", gameObj);
    }


    public void showReportDialog() {
        if ((member == null) || (member.getMid() == 0)) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_LONG).show();
        } else {
            Bundle args = new Bundle();
            args.putSerializable("cheatObj", visibleCheat);

            FragmentManager fm = getSupportFragmentManager();
            ReportCheatDialog reportCheatDialog = new ReportCheatDialog();
            reportCheatDialog.setArguments(args);
            reportCheatDialog.show(fm, "fragment_report_cheat");
        }
    }

//    @Override
//    public void onFinishReportDialog(int selectedReason) {
//        String[] reasons = getResources().getStringArray(R.array.report_reasons);
//        reportCheatInBackground(reasons[selectedReason]);
//    }

    public void onEvent(CheatReportingFinishedEvent result) {
        if (result.isSucceeded()) {
            Toast.makeText(this, R.string.thanks_for_reporting, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

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
//        cheatDetailFragment.updateMemberCheatRating(selectedRating);
//
//        // FIXME make the star to highlight on all fragments
//        cheatDetailFragment.highlightRatingIcon(true);
//        cheatDetailTabletMetaFragment.highlightRatingIcon(true);
//        cheatForumFragment.highlightRatingIcon(true);
//
//        Toast.makeText(this, R.string.rating_inserted, Toast.LENGTH_SHORT).show();
//    }

    public void onEvent(CheatRatingFinishedEvent result) {
//        if (mTwoPane) {
        visibleCheat.setMemberRating(result.getRating());
        cheatDetailFragment.updateMemberCheatRating(result.getRating());

        // FIXME make the star to highlight on all fragments
        cheatDetailFragment.highlightRatingIcon(true);
        cheatDetailTabletMetaFragment.highlightRatingIcon(true);
        cheatForumFragment.highlightRatingIcon(true);

        Toast.makeText(this, R.string.rating_inserted, Toast.LENGTH_SHORT).show();
//        }
    }

//    @Background
//    void reportCheatInBackground(String reason) {
//        boolean isSuccess = false;
//
//        try {
//            Webservice.reportCheat(visibleCheat.getCheatId(), member.getMid(), reason);
//            isSuccess = true;
//        } catch (Exception e) {
//            isSuccess = false;
//        }
//        reportCheatFinished(isSuccess);
//    }
//
//    @UiThread
//    void reportCheatFinished(boolean isSuccess) {
//        if (isSuccess) {
//            Toast.makeText(CheatListActivity.this, R.string.thanks_for_reporting, Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(CheatListActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
//        }
//    }

//    /**
//     * Callback method from {@link com.cheatdatabase.CheatListFragment.CheatListClickCallbacks} indicating that
//     * the item with the given ID was selected.
//     */
//    @Override
//    public void onItemSelected(int id) {
//        this.lastPosition = id;
//        this.lastGameObj = this.gameObj;
//        this.visibleCheat = gameObj.getCheats()[id];
//
//        Log.d(TAG, "onItemSelected: " + id);
//        Log.d(TAG, "mTwoPane: " + mTwoPane);
//        Log.d(TAG, "visibleCheat: " + visibleCheat.getCheatTitle());
//
//        CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "select_cheat").setLabel(this.visibleCheat.getGameName() + ": " + this.visibleCheat.getCheatTitle()).build());
//
//        if (mTwoPane) {
//            Log.i(TAG, "Is Dual Pane");
//
//            cheatDetailFragment = new CheatDetailTabletFragment();
//            cheatForumFragment = new CheatForumFragment();
//            cheatDetailTabletMetaFragment = new CheatDetailTabletMetaFragment();
//
//            // VIEW FOR TABLETS
//            Bundle arguments = new Bundle();
//            arguments.putInt(CheatDetailTabletFragment.ARG_ITEM_ID, id);
//            arguments.putSerializable("cheatObj", visibleCheat);
//            arguments.putSerializable("cheatDetailTabletFragment", cheatDetailFragment);
//            arguments.putSerializable("cheatForumFragment", cheatForumFragment);
//            arguments.putSerializable("cheatDetailTabletMetaFragment", cheatDetailTabletMetaFragment);
//            cheatDetailFragment.setArguments(arguments);
//            cheatForumFragment.setArguments(arguments);
//            cheatDetailTabletMetaFragment.setArguments(arguments);
//
//            getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.cheat_detail_container, cheatDetailFragment).commit();
//
//        } else {
//            // In single-pane mode, simply start the detail activity
//            // for the selected item ID.
//            // Intent detailIntent = new Intent(this, YyyDetailActivity.class);
//            // detailIntent.putExtra(YyyDetailFragment.ARG_ITEM_ID, id);
//            // startActivity(detailIntent);
//
//            // editor.putString(Konstanten.PREFERENCES_TEMP_GAME_OBJECT_VIEW,
//            // new Gson().toJson(gameObj));
//            editor.putInt(Konstanten.PREFERENCES_PAGE_SELECTED, id);
//            editor.commit();
//
//            if (Reachability.reachability.isReachable) {
//                // Using local Preferences to pass data for large game objects
//                // (instead of intent) such as Pokemon
//                Intent explicitIntent = new Intent(CheatListActivity.this, CheatViewPageIndicator.class);
//                // explicitIntent.putExtra("gameObj", gameObj);
//                explicitIntent.putExtra("selectedPage", id);
//                explicitIntent.putExtra("layoutResourceId", R.layout.activity_cheatview_pager);
//                // explicitIntent.putExtra("pageIndicatorColor",
//                // R.color.page_indicator);
//                startActivity(explicitIntent);
//            } else {
//                Toast.makeText(CheatListActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        switch (item.getItemId()) {
            case android.R.id.home:
                editor.remove(Konstanten.PREFERENCES_TEMP_GAME_OBJECT_VIEW);
                editor.commit();
                GamesBySystemActivity_.intent(this).systemObj(Tools.getSystemObjectByName(CheatListActivity.this, Tools.getSystemNameById(this, gameObj.getSystemId()))).start();
                return true;
            case R.id.action_add_to_favorites:
                Toast.makeText(CheatListActivity.this, R.string.favorite_adding, Toast.LENGTH_SHORT).show();
                //new AddCheatsToFavoritesTask().execute(gameObj);
                addCheatsToFavoritesTask(gameObj);
                return true;
            case R.id.action_submit_cheat:
                Intent explicitIntent = new Intent(CheatListActivity.this, SubmitCheatActivity.class);
                explicitIntent.putExtra("gameObj", gameObj);
                startActivity(explicitIntent);
                return true;
            case R.id.action_login:
                Intent loginIntent = new Intent(CheatListActivity.this, LoginActivity_.class);
                startActivityForResult(loginIntent, Konstanten.LOGIN_REGISTER_OK_RETURN_CODE);
                return true;
            case R.id.action_logout:
                member = null;
                Tools.logout(CheatListActivity.this, editor);
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);

        // Sharing
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        try {
            String result = String.format(getString(R.string.share_email_subject), visibleCheat.getGameName());
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, result);
            String fullBody = visibleCheat.getGameName() + " (" + visibleCheat.getSystemName() + "): " + visibleCheat.getCheatTitle() + "\n";
            fullBody += Konstanten.BASE_URL + "display/switch.php?id=" + visibleCheat.getCheatId() + "\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, fullBody);
        } catch (Exception ee) {
            // TODO bei einem wipe muss der visibleCheat hier geupdated werden
            // TODO icon unten rechts wird vom share icon überdeckt...
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "EMPTY SUBJECT");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "EMPTY BODY");
        }
        mShareActionProvider.setShareIntent(shareIntent);

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // Return result code. Login success, Register success etc.
            int intentReturnCode = data.getIntExtra("result", Konstanten.LOGIN_REGISTER_FAIL_RETURN_CODE);

            if (requestCode == Konstanten.LOGIN_REGISTER_OK_RETURN_CODE) {
                member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
                invalidateOptionsMenu();
                if ((member != null) && intentReturnCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
                    Toast.makeText(CheatListActivity.this, R.string.register_thanks, Toast.LENGTH_LONG).show();
                } else if ((member != null) && intentReturnCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
                    Toast.makeText(CheatListActivity.this, R.string.login_ok, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Background
    void addCheatsToFavoritesTask(Game game) {
        int returnValueCode;
        CheatDatabaseAdapter dbAdapter = null;
        try {
            dbAdapter = new CheatDatabaseAdapter(CheatListActivity.this);
            dbAdapter.open();
            int retVal = dbAdapter.insertFavorites(game);
            if (retVal > 0) {
                returnValueCode = R.string.add_favorites_ok;
            } else {
                returnValueCode = R.string.favorite_error;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
            returnValueCode = R.string.favorite_error;
        } finally {
            dbAdapter.close();
        }

        finishedAddingCheatsToFavorites(returnValueCode);
    }

    @UiThread
    void finishedAddingCheatsToFavorites(int returnValueCode) {
        // TODO Favorite Icon animation abschalten
        Toast.makeText(CheatListActivity.this, returnValueCode, Toast.LENGTH_LONG).show();
    }

    public void highlightRatingIcon(ImageButton btnRateCheat, boolean highlight) {
        if (highlight) {
            btnRateCheat.setImageResource(R.drawable.ic_action_star);
        } else {
            btnRateCheat.setImageResource(R.drawable.ic_action_not_important);
        }
    }

    public void onEvent(CheatListRecyclerViewClickEvent result) {
        if (result.isSucceeded()) {
            CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "click").setLabel(result.getCheat().getCheatTitle()).build());

            this.visibleCheat = result.getCheat();
            this.lastGameObj = result.getCheat().getGame();

            CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "select_cheat").setLabel(result.getCheat().getGameName() + ": " + result.getCheat().getCheatTitle()).build());

//            if (mTwoPane) {
//                Log.i(TAG, "Is Dual Pane");
//
//                cheatDetailFragment = new CheatDetailTabletFragment();
//                cheatForumFragment = new CheatForumFragment();
//                cheatDetailTabletMetaFragment = new CheatDetailTabletMetaFragment();
//
//                // VIEW FOR TABLETS
//                Bundle arguments = new Bundle();
//                arguments.putInt(CheatDetailTabletFragment.ARG_ITEM_ID, result.getPosition());
//                arguments.putSerializable("cheatObj", result.getCheat());
//                arguments.putSerializable("cheatDetailTabletFragment", cheatDetailFragment);
//                arguments.putSerializable("cheatForumFragment", cheatForumFragment);
//                arguments.putSerializable("cheatDetailTabletMetaFragment", cheatDetailTabletMetaFragment);
//                cheatDetailFragment.setArguments(arguments);
//                cheatForumFragment.setArguments(arguments);
//                cheatDetailTabletMetaFragment.setArguments(arguments);
//
//                getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.cheat_detail_container, cheatDetailFragment).commit();
//
//            } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            // Intent detailIntent = new Intent(this, YyyDetailActivity.class);
            // detailIntent.putExtra(YyyDetailFragment.ARG_ITEM_ID, id);
            // startActivity(detailIntent);

            // editor.putString(Konstanten.PREFERENCES_TEMP_GAME_OBJECT_VIEW,
            // new Gson().toJson(gameObj));
            editor.putInt(Konstanten.PREFERENCES_PAGE_SELECTED, result.getPosition());
            editor.commit();

            if (Reachability.reachability.isReachable) {
                // Using local Preferences to pass data for large game objects
                // (instead of intent) such as Pokemon
                Intent explicitIntent = new Intent(CheatListActivity.this, CheatViewPageIndicator.class);
                explicitIntent.putExtra("gameObj", gameObj);
                explicitIntent.putExtra("selectedPage", result.getPosition());
                explicitIntent.putExtra("layoutResourceId", R.layout.activity_cheatview_pager);
                // explicitIntent.putExtra("pageIndicatorColor",
                // R.color.page_indicator);
                startActivity(explicitIntent);
            } else {
                Toast.makeText(CheatListActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
            }
//            }


        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }


}