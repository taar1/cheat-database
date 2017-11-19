package com.cheatdatabase;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.adapters.GamesBySystemRecycleListViewAdapter;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.SystemPlatform;
import com.cheatdatabase.events.GameListRecyclerViewClickEvent;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.widgets.DividerDecoration;
import com.cheatdatabase.widgets.EmptyRecyclerView;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.mopub.mobileads.MoPubView;
import com.splunk.mint.Mint;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;

@EActivity(R.layout.activity_gamelist)
public class GamesBySystemActivity extends AppCompatActivity {

    private static final String TAG = GamesBySystemActivity.class.getSimpleName();
    private ArrayList<Game> gameArrayList;

    @Extra
    SystemPlatform systemObj;

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
    GamesBySystemRecycleListViewAdapter mGamesBySystemRecycleListViewAdapter;

    @ViewById(R.id.item_list_empty_view)
    TextView mEmptyView;

    @ViewById(R.id.items_list_load_progress)
    ProgressBarCircularIndeterminate mProgressView;

    @AfterViews
    public void createView() {
        init();
        mSwipeRefreshLayout.setRefreshing(true);

        setTitle(systemObj.getSystemName());
        //CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "loaded").setLabel("GamesBySystemActivity").build());

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getGames();
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
            getGames();
        }
    }

    private void init() {
        Mint.initAndStartSession(this, Konstanten.SPLUNK_MINT_API_KEY);

        tools.loadAd(mAdView, getString(R.string.screen_type));

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Search
        getMenuInflater().inflate(R.menu.search_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }

    @Background
    public void getGames() {
        Log.d(TAG, "getGames() System ID/NAME: " + systemObj.getSystemId() + "/" + systemObj.getSystemName());

        gameArrayList = new ArrayList<>();
        Game[] gamesFound = Webservice.getGameListBySystemId(systemObj.getSystemId(), systemObj.getSystemName());
        while (gamesFound == null) {
            gamesFound = Webservice.getGameListBySystemId(systemObj.getSystemId(), systemObj.getSystemName());
        }

        Collections.addAll(gameArrayList, gamesFound);
        fillListWithGames();
    }

    @UiThread
    public void fillListWithGames() {
        try {
            if (gameArrayList != null && gameArrayList.size() > 0) {
                mGamesBySystemRecycleListViewAdapter.init(gameArrayList);
                mRecyclerView.setAdapter(mGamesBySystemRecycleListViewAdapter);

                mGamesBySystemRecycleListViewAdapter.notifyDataSetChanged();
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
        new AlertDialog.Builder(GamesBySystemActivity.this).setIcon(R.drawable.ic_action_warning).setTitle(getString(R.string.err)).setMessage(R.string.err_data_not_accessible).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        }).create().show();
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
    public void onEvent(GameListRecyclerViewClickEvent result) {
        if (result.isSucceeded()) {
            //CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "click").setLabel(result.getGame().getGameName()).build());

            CheatListActivity_.intent(this).gameObj(result.getGame()).start();
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }


}
