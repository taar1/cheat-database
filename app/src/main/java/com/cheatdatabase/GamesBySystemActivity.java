package com.cheatdatabase;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.SystemPlatform;
import com.cheatdatabase.helpers.ActionBarListActivity;
import com.cheatdatabase.helpers.Group;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.google.analytics.tracking.android.Tracker;
import com.mopub.mobileads.MoPubView;
import com.splunk.mint.Mint;

import java.util.ArrayList;
import java.util.Collections;

public class GamesBySystemActivity extends ActionBarListActivity implements ActionBar.OnNavigationListener, AdapterView.OnItemClickListener {

    SparseArray<Group> groups = new SparseArray<Group>();

    protected SystemPlatform systemObj;

    private Tracker tracker;

    private ProgressDialog gameListProgressDialog = null;

    private GameListAdapter gameListAdapter;

    private ArrayList<Game> gameArrayList = new ArrayList<Game>();

    private Game[] gamesFound;

    private Typeface latoFontLight;
    private Typeface latoFontRegular;

    private ImageView reloadView;

    private SharedPreferences settings;
    private Editor editor;

    private ViewGroup adViewContainer;
    private MoPubView mAdView;
    private Toolbar mToolbar;

    private static final String SCREEN_LABEL = "Game List By System ID Screen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gamelist);

        init();
        systemObj = (SystemPlatform) getIntent().getSerializableExtra("systemObj");
        handleIntent(getIntent());

        reloadView = (ImageView) findViewById(R.id.reload);
        if (Reachability.reachability.isReachable) {
            startGameListAdapter();
        } else {
            reloadView.setVisibility(View.VISIBLE);
            reloadView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (Reachability.reachability.isReachable) {
                        startGameListAdapter();
                    } else {
                        Toast.makeText(GamesBySystemActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }

        getListView().setOnItemClickListener(this);
    }

    private void init() {
        Reachability.registerReachability(this);
        Mint.initAndStartSession(this, Konstanten.SPLUNK_MINT_API_KEY);

        mToolbar = Tools.initToolbarBase(this, mToolbar);
        mAdView = Tools.initMoPubAdView(this, mAdView);

        latoFontLight = Tools.getFont(getAssets(), Konstanten.FONT_LIGHT);
        latoFontRegular = Tools.getFont(getAssets(), Konstanten.FONT_REGULAR);
    }

    @Override
    public void onPause() {
        Reachability.unregister(this);
        super.onPause();
    }

    private void startGameListAdapter() {
        reloadView.setVisibility(View.GONE);
        gameListAdapter = new GameListAdapter(this, R.layout.activity_gamelist, gameArrayList);
        setListAdapter(gameListAdapter);

        if (gameListProgressDialog == null) {
            gameListProgressDialog = ProgressDialog.show(GamesBySystemActivity.this, getString(R.string.please_wait) + "...", getString(R.string.retrieving_data) + "...", true);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                getGames();
            }
        }).start();
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

    private void handleIntent(final Intent intent) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                // FIXME sysetmObj hier holen oder schon vorher (oben nach init()) ???
//                systemObj = (SystemPlatform) intent.getSerializableExtra("systemObj");

                try {
                    setTitle(systemObj.getSystemName());
                    Tools.initGA(GamesBySystemActivity.this, tracker, SCREEN_LABEL, "Game List", systemObj.getSystemName());
                } catch (Exception e) {
                    throw e;
                }
            }
        }).start();

    }

    private void getGames() {
        Log.d("getGames", "getGames()");

        // Notloesung... Schauen, wie man diesen Fall besser handlen kann.
        if (systemObj == null) {
            finish();
        } else {
            Log.d("getGames", "SYSTEM-ID: " + systemObj.getSystemId());

            gamesFound = Webservice.getGameListBySystemId(systemObj.getSystemId());
            while (gamesFound == null) {
                gamesFound = Webservice.getGameListBySystemId(systemObj.getSystemId());
            }
            gameArrayList = new ArrayList<Game>();
//            for (int i = 0; i < gamesFound.length; i++) {
//                gameArrayList.add(gamesFound[i]);
//                Log.d("getGames", gamesFound[i].getGameName());
//            }
            Collections.addAll(gameArrayList, gamesFound);
            runOnUiThread(runFillListView);
        }
    }

    private Runnable runFillListView = new Runnable() {

        @Override
        public void run() {

            try {
                if (gameArrayList != null && gameArrayList.size() > 0) {
                    gameListAdapter.notifyDataSetChanged();
                    for (int i = 0; i < gamesFound.length; i++) {
                        gameListAdapter.add(gamesFound[i]);
                    }
                    gameListProgressDialog.dismiss();
                    gameListAdapter.notifyDataSetChanged();
                } else {
                    error();
                }
            } catch (Exception e) {
                gameListProgressDialog.dismiss();
                error();
            }

            // FIXME wenn keine internetverbindung muss der Fehler
            // abgefangen werden...
            // gameListProgressDialog =
            // ProgressDialog.show(GamesBySystemActivity.this,
            // getString(R.string.please_wait) + "...",
            // getString(R.string.retrieving_data) + "...", true);
        }

    };

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

    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (Reachability.reachability.isReachable) {
            Game tmpGame = new Game();
            tmpGame.setGameId(gamesFound[position].getGameId());
            tmpGame.setGameName(gamesFound[position].getGameName());
            tmpGame.setSystemName(systemObj.getSystemName());
            tmpGame.setSystemId(systemObj.getSystemId());

            Intent explicitIntent = new Intent(this, CheatListActivity.class);
            explicitIntent.putExtra("gameObj", tmpGame);
            startActivity(explicitIntent);
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }


    private class GameListAdapter extends ArrayAdapter<Game> {

        private ArrayList<Game> gameArrayList;

        public GameListAdapter(Context context, int textViewResourceId, ArrayList<Game> gameArrayList) {
            super(context, textViewResourceId, gameArrayList);
            this.gameArrayList = gameArrayList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.listrow_gamelist, null);
            }

            Game singleGame = gameArrayList.get(position);

            if (singleGame != null) {
                TextView tt = (TextView) v.findViewById(R.id.cheat_title);
                tt.setTypeface(latoFontRegular);
                TextView bt = (TextView) v.findViewById(R.id.cheats_count);
                bt.setTypeface(latoFontLight);
                if (tt != null) {
                    tt.setText(singleGame.getGameName());
                }
                if (bt != null) {
                    if (singleGame.getCheatsCount() == 1) {
                        bt.setText(singleGame.getCheatsCount() + " Cheat");
                    } else {
                        bt.setText(singleGame.getCheatsCount() + " Cheats");
                    }
                }
            }
            return v;
        }
    }


}
