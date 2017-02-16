package com.cheatdatabase.search;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.helpers.Group;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@SuppressLint("NewApi")
public class SearchResultsActivity extends AppCompatActivity {

    SparseArray<Group> groups = new SparseArray<>();

    private LinearLayout nothingFoundLayout;
    private RelativeLayout somethingfoundLayout;
    private Button searchButton;
    private TextView nothingFoundText;

    private Game[] gamesFound;
    private String query;
    private ExpandableListView listView;
    private SearchresultExpandableListAdapter adapter;

    protected final int STEP_ONE_COMPLETE = 1;

    private Typeface latoFontBold;

    private Typeface latoFontLight;

    private TextView nothingFoundTitle;

    private ImageView reloadView;

    private SharedPreferences settings;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        somethingfoundLayout = (RelativeLayout) findViewById(R.id.somethingfound_layout);
        nothingFoundLayout = (LinearLayout) findViewById(R.id.nothingfound_layout);
        searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setTypeface(latoFontBold);
        nothingFoundTitle = (TextView) findViewById(R.id.nothingfound_title);
        nothingFoundText = (TextView) findViewById(R.id.nothingfound_text);
        nothingFoundTitle.setTypeface(latoFontBold);
        nothingFoundText.setTypeface(latoFontLight);

        listView = (ExpandableListView) findViewById(R.id.listView);
        adapter = new SearchresultExpandableListAdapter(SearchResultsActivity.this, groups);
        listView.setAdapter(adapter);

        handleIntent(getIntent());
    }

    private void init() {
        setContentView(R.layout.activity_search_result);
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(this);
        }

        latoFontLight = Tools.getFont(getAssets(), Konstanten.FONT_LIGHT);
        latoFontBold = Tools.getFont(getAssets(), Konstanten.FONT_BOLD);

        settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);

        Tools.initToolbarBase(this, toolbar);
    }

    @Override
    public void onPause() {
        Reachability.unregister(this);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Full Text Search
        // getMenuInflater().inflate(R.menu.search_fulltext_menu, menu);

        // Search
        getMenuInflater().inflate(R.menu.search_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);

            getSupportActionBar().setTitle(getString(R.string.game_search_results_title, query.toUpperCase(Locale.ENGLISH).trim()));

            reloadView = (ImageView) findViewById(R.id.reload);
            if (Reachability.reachability.isReachable) {
                searchNow();
            } else {
                reloadView.setVisibility(View.VISIBLE);
                reloadView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (Reachability.reachability.isReachable) {
                            searchNow();
                        } else {
                            Toast.makeText(SearchResultsActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void searchNow() {
        reloadView.setVisibility(View.GONE);
        new Thread(new Runnable() {

            @Override
            public void run() {
                gamesFound = Webservice.searchGames(SearchResultsActivity.this, query);
                createData();

                Message msg = Message.obtain();
                msg.what = STEP_ONE_COMPLETE;
                handler.sendMessage(msg);
            }
        }).start();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STEP_ONE_COMPLETE:
                    fillList();
                    break;
            }
        }

        private void fillList() {
            for (int i = 0; i < groups.size(); i++) {
                listView.expandGroup(i, false);
            }
        }
    };

    public void createData() {

        // If nothing found then display a message.
        if (gamesFound == null) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    nothingFoundText.setText(getString(R.string.error_nothing_found_text, "\"" + query + "\""));

                    somethingfoundLayout.setVisibility(View.GONE);
                    nothingFoundLayout.setVisibility(View.VISIBLE);
                    searchButton.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            onSearchRequested();
                        }
                    });
                }
            });
        } else {
            if ((gamesFound != null) && (gamesFound.length > 0)) {
                Set<String> systems = new HashSet<>();

                // Get system names
                for (Game game : gamesFound) {
                    systems.add(game.getSystemName());
                }

                // Sort system names
                List<String> systemsSorted = new ArrayList<>(systems);
                Collections.sort(systemsSorted);

                // Go through systems
                for (int i = 0; i < systemsSorted.size(); i++) {
                    Log.i("system", systemsSorted.get(i) + "");

                    // Create groups with system names
                    Group group = new Group(systemsSorted.get(i) + "");

                    // Fill each group with the game names
                    for (int l = 0; l < gamesFound.length; l++) {
                        Game game = gamesFound[l];
                        if (game.getSystemName().equalsIgnoreCase(systemsSorted.get(i))) {
                            group.children.add(game.getGameName());
                            group.gameChildren.add(game);
                        }
                    }
                    groups.append(i, group);
                }
            }
        }

    }

}
