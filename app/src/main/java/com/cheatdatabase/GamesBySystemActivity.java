package com.cheatdatabase;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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
import com.google.analytics.tracking.android.Tracker;
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

@EActivity(R.layout.activity_gamelist)
public class GamesBySystemActivity extends ActionBarActivity implements ActionBar.OnNavigationListener {

    private Tracker tracker;

//    private ProgressDialog gameListProgressDialog = null;
//    private GameListAdapter gameListAdapter;
//    private Game[] gamesFound;

    private ArrayList<Game> gameArrayList = new ArrayList<>();

    private Typeface latoFontLight;
    private Typeface latoFontRegular;
    private static final String TAG = GamesBySystemActivity.class.getSimpleName();
    private static final String SCREEN_LABEL = "Game List By System ID Screen";

    //    private RecyclerView.Adapter mAdapter;
//    private RecyclerView.LayoutManager mLayoutManager;
//    private ArrayList<Game> gameList;

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
//        mProgressView.setVisibility(View.VISIBLE);

        mSwipeRefreshLayout.setRefreshing(true);


        setTitle(systemObj.getSystemName());
        tools.initGA(GamesBySystemActivity.this, tracker, SCREEN_LABEL, "Game List", systemObj.getSystemName());

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

        // use a linear layout manager
//        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

//        mAdapter = new GamesBySystemRecycleListViewAdapter(getApplicationContext(), gameList);
//        mGamesBySystemRecycleListViewAdapter.init(gameList);
//        mRecyclerView.setAdapter(mGamesBySystemRecycleListViewAdapter);

//        getListView().setOnItemClickListener(this);
    }

    private void init() {

        Mint.initAndStartSession(this, Konstanten.SPLUNK_MINT_API_KEY);

        tools.loadAd(mAdView, getString(R.string.screen_type));

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        latoFontLight = tools.getFont(getAssets(), Konstanten.FONT_LIGHT);
        latoFontRegular = tools.getFont(getAssets(), Konstanten.FONT_REGULAR);
    }


//    private void startGameListAdapter() {
//        gameListAdapter = new GameListAdapter(this, R.layout.activity_gamelist, gameArrayList);
//        setListAdapter(gameListAdapter);
//
//        if (gameListProgressDialog == null) {
//            gameListProgressDialog = ProgressDialog.show(GamesBySystemActivity.this, getString(R.string.please_wait) + "...", getString(R.string.retrieving_data) + "...", true);
//        }
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                getGames();
//            }
//        }).start();
//    }

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

//    private void handleIntent(final Intent intent) {
//
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                // FIXME sysetmObj hier holen oder schon vorher (oben nach init()) ???
////                systemObj = (SystemPlatform) intent.getSerializableExtra("systemObj");
//
//                try {
//                    setTitle(systemObj.getSystemName());
//                    tools.initGA(GamesBySystemActivity.this, tracker, SCREEN_LABEL, "Game List", systemObj.getSystemName());
//                } catch (Exception e) {
//                    throw e;
//                }
//            }
//        }).start();
//
//    }

    @Background
    public void getGames() {
        Log.d(TAG, "getGames() System ID: " + systemObj.getSystemId());

        Game[] gamesFound = Webservice.getGameListBySystemId(systemObj.getSystemId());
        while (gamesFound == null) {
            gamesFound = Webservice.getGameListBySystemId(systemObj.getSystemId());
        }

        Collections.addAll(gameArrayList, gamesFound);
//        runOnUiThread(runFillListView);
        fillListWithGames();
    }

    @UiThread
    public void fillListWithGames() {
        try {
            if (gameArrayList != null && gameArrayList.size() > 0) {
//                gameListAdapter.notifyDataSetChanged();
//                for (int i = 0; i < gamesFound.length; i++) {
//                    gameListAdapter.add(gamesFound[i]);
//                }
//                gameListProgressDialog.dismiss();
//                gameListAdapter.notifyDataSetChanged();

                mGamesBySystemRecycleListViewAdapter.init(gameArrayList);
                mRecyclerView.setAdapter(mGamesBySystemRecycleListViewAdapter);

                mGamesBySystemRecycleListViewAdapter.notifyDataSetChanged();
            } else {
                error();
            }
        } catch (Exception e) {
//            gameListProgressDialog.dismiss();
            error();
        }

        mSwipeRefreshLayout.setRefreshing(false);
//        mProgressView.setVisibility(View.GONE);
        mRecyclerView.hideLoading();
    }


//    private Runnable runFillListView = new Runnable() {
//
//        @Override
//        public void run() {
//
//            try {
//                if (gameArrayList != null && gameArrayList.size() > 0) {
//                    gameListAdapter.notifyDataSetChanged();
//                    for (int i = 0; i < gamesFound.length; i++) {
//                        gameListAdapter.add(gamesFound[i]);
//                    }
//                    gameListProgressDialog.dismiss();
//                    gameListAdapter.notifyDataSetChanged();
//                } else {
//                    error();
//                }
//            } catch (Exception e) {
//                gameListProgressDialog.dismiss();
//                error();
//            }
//
//            // FIXME wenn keine internetverbindung muss der Fehler
//            // abgefangen werden...
//            // gameListProgressDialog =
//            // ProgressDialog.show(GamesBySystemActivity.this,
//            // getString(R.string.please_wait) + "...",
//            // getString(R.string.retrieving_data) + "...", true);
//        }
//
//    };
//
//	@Override
//	protected void onListItemClick(ListView l, View v, int position, long id) {
//		super.onListItemClick(l, v, position, id);
//
//        Log.d("CLICKED", "CLICKEEDDDDDD");
//        if (Reachability.reachability.isReachable) {
//            Game tmpGame = new Game();
//            tmpGame.setGameId(gamesFound[position].getGameId());
//            tmpGame.setGameName(gamesFound[position].getGameName());
//            tmpGame.setSystemName(systemObj.getSystemName());
//            tmpGame.setSystemId(systemObj.getSystemId());
//
//            Log.d("CLICKED", "CLICKEEDDDDDD 2");
//
//			Intent explicitIntent = new Intent(this, CheatListActivity.class);
//			explicitIntent.putExtra("gameObj", tmpGame);
//			startActivity(explicitIntent);
//		} else {
//			Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
//		}
//	}

    private void error() {
        Log.e("error()", "caught error: " + getPackageName() + "/" + getTitle());
        new AlertDialog.Builder(GamesBySystemActivity.this).setIcon(R.drawable.ic_action_warning).setTitle(getString(R.string.err)).setMessage(R.string.err_data_not_accessible).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        }).create().show();
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        Reachability.unregister(this);
        CheatDatabaseApplication.getEventBus().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Reachability.registerReachability(this);
        CheatDatabaseApplication.getEventBus().register(this);
    }

    public void onEvent(GameListRecyclerViewClickEvent result) {
        if (result.isSucceeded()) {
            Intent explicitIntent = new Intent(this, CheatListActivity.class);
            explicitIntent.putExtra("gameObj", result.getGame());
            startActivity(explicitIntent);
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        return false;
    }

//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//        if (Reachability.reachability.isReachable) {
//            Game tmpGame = new Game();
//            tmpGame.setGameId(gamesFound[position].getGameId());
//            tmpGame.setGameName(gamesFound[position].getGameName());
//            tmpGame.setSystemName(systemObj.getSystemName());
//            tmpGame.setSystemId(systemObj.getSystemId());
//
//            Intent explicitIntent = new Intent(this, CheatListActivity.class);
//            explicitIntent.putExtra("gameObj", tmpGame);
//            startActivity(explicitIntent);
//        } else {
//            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
//        }
//    }


//    private class GameListAdapter extends ArrayAdapter<Game> {
//
//        private ArrayList<Game> gameArrayList;
//
//        public GameListAdapter(Context context, int textViewResourceId, ArrayList<Game> gameArrayList) {
//            super(context, textViewResourceId, gameArrayList);
//            this.gameArrayList = gameArrayList;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            View v = convertView;
//            if (v == null) {
//                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                v = vi.inflate(R.layout.listrow_gamelist, null);
//            }
//
//            Game singleGame = gameArrayList.get(position);
//
//            if (singleGame != null) {
//                TextView tt = (TextView) v.findViewById(R.id.cheat_title);
//                tt.setTypeface(latoFontRegular);
//                TextView bt = (TextView) v.findViewById(R.id.cheats_count);
//                bt.setTypeface(latoFontLight);
//                if (tt != null) {
//                    tt.setText(singleGame.getGameName());
//                }
//                if (bt != null) {
//                    if (singleGame.getCheatsCount() == 1) {
//                        bt.setText(singleGame.getCheatsCount() + " Cheat");
//                    } else {
//                        bt.setText(singleGame.getCheatsCount() + " Cheats");
//                    }
//                }
//            }
//            return v;
//        }
//    }


}
