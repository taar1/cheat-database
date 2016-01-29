package com.cheatdatabase;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.adapters.MemberCheatRecycleListViewAdapter;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.events.CheatListRecyclerViewClickEvent;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.members.cheatview.MemberCheatViewPageIndicator;
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
 * Shows all cheats of one particular member.
 *
 * @author Dominik
 */
@EActivity(R.layout.activity_member_cheatlist)
public class MemberCheatListActivity extends AppCompatActivity {

    private final String TAG = MemberCheatListActivity.class.getSimpleName();

    @Extra
    Member memberObj;

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
    MemberCheatRecycleListViewAdapter cheatRecycleListViewAdapter;

    @ViewById(R.id.item_list_empty_view)
    TextView mEmptyView;

    @ViewById(R.id.items_list_load_progress)
    ProgressBarCircularIndeterminate mProgressView;

//    private final int ADD_TO_FAVORITES = 0;
//    private Cheat selectedCheat;
//    private ShareActionProvider mShareActionProvider;

    private ArrayList<Cheat> cheatsArrayList;

    private Typeface latoFontLight;
    private Editor editor;
    private Cheat[] cheats;

    @AfterViews
    void onCreate() {
        init();
        mSwipeRefreshLayout.setRefreshing(true);

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
        CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "Member Cheat List").setLabel(TAG).build());
        Mint.initAndStartSession(this, Konstanten.SPLUNK_MINT_API_KEY);

        tools.loadAd(mAdView, getString(R.string.screen_type));

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.members_cheats_title, memberObj.getUsername()));

        SharedPreferences settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();

        latoFontLight = tools.getFont(getAssets(), Konstanten.FONT_LIGHT);

        //memberObj = (Member) getIntent().getSerializableExtra("memberObj");
    }

    @Background
    void getCheats() {
        cheatsArrayList = new ArrayList<>();

        cheats = Webservice.getCheatsByMemberId(memberObj.getMid());

        editor.putString(Konstanten.PREFERENCES_TEMP_CHEAT_ARRAY_OBJECT_VIEW, new Gson().toJson(cheats));
        editor.commit();

        Collections.addAll(cheatsArrayList, cheats);
        fillListWithCheats();
    }

    @UiThread
    void fillListWithCheats() {
        try {
            if (cheatsArrayList.size() > 0) {
                cheatRecycleListViewAdapter.init(cheatsArrayList);
                mRecyclerView.setAdapter(cheatRecycleListViewAdapter);

                cheatRecycleListViewAdapter.notifyDataSetChanged();
            } else {
                error(R.string.err_data_not_accessible);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
            error(R.string.err_no_member_data);
        }

        mSwipeRefreshLayout.setRefreshing(false);
        mRecyclerView.hideLoading();
    }

    @Override
    protected void onResume() {
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
        outState.putSerializable("memberObj", memberObj);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
//                onBackPressed();
                MainActivity_.intent(this).mFragmentId(MainActivity.DRAWER_TOP_MEMBERS).start();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case ADD_TO_FAVORITES:
//                CheatDatabaseAdapter db = new CheatDatabaseAdapter(this);
//                db.open();
//                int retVal = db.insertFavorite(selectedCheat);
//
//                if (retVal > 0) {
//                    Toast.makeText(MemberCheatListActivity.this, R.string.add_favorite_ok, Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(MemberCheatListActivity.this, R.string.favorite_error, Toast.LENGTH_SHORT).show();
//                }
//                return true;
//        }
//
//        return super.onContextItemSelected(item);
//    }

    private void error(int msg) {
        new AlertDialog.Builder(MemberCheatListActivity.this).setIcon(R.drawable.ic_action_warning).setTitle(getString(R.string.err)).setMessage(msg).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        }).create().show();
    }

    public void onEvent(CheatListRecyclerViewClickEvent result) {
        if (result.isSucceeded()) {
            CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "click").setLabel(result.getCheat().getCheatTitle()).build());

            CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "select_cheat").setLabel(result.getCheat().getGameName() + ": " + result.getCheat().getCheatTitle()).build());

            editor.putInt(Konstanten.PREFERENCES_PAGE_SELECTED, result.getPosition());
            editor.commit();

            if (Reachability.reachability.isReachable) {
                // Using local Preferences to pass data for large game objects
                // (instead of intent) such as Pokemon
                Intent explicitIntent = new Intent(MemberCheatListActivity.this, MemberCheatViewPageIndicator.class);

                if (cheats.length <= 100) {
                    // Delete Walkthrough texts (otherwise runs into a timeout)
                    for (int i = 0; i < cheats.length; i++) {
                        if (cheats[i].isWalkthroughFormat()) {
                            cheats[i].setCheatText("");
                        }
                    }
                    explicitIntent.putExtra("cheatObj", cheats);
                } else {
                    // Save to SharedPreferences if array too big
                    editor.putString(Konstanten.PREFERENCES_TEMP_CHEAT_ARRAY_OBJECT_VIEW, new Gson().toJson(cheats));
                    editor.apply();
                }

                explicitIntent.putExtra("selectedPage", result.getPosition());
                explicitIntent.putExtra("layoutResourceId", R.layout.activity_cheatview_pager);
                startActivity(explicitIntent);
            } else {
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//
//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
//        selectedCheat = cheats[Integer.parseInt(String.valueOf(info.id))];
//        menu.setHeaderTitle(R.string.context_menu_title);
//        menu.add(0, ADD_TO_FAVORITES, 1, R.string.add_one_favorite);
//    }
}
