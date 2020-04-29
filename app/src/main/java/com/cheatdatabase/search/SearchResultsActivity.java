package com.cheatdatabase.search;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import com.cheatdatabase.CheatDatabaseApplication;
import com.cheatdatabase.R;
import com.cheatdatabase.activity.CheatsByGameListActivity;
import com.cheatdatabase.helpers.Group;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.listeners.OnGameListItemSelectedListener;
import com.cheatdatabase.model.Game;
import com.cheatdatabase.rest.RestApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import needle.Needle;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

@SuppressLint("NewApi")
public class SearchResultsActivity extends AppCompatActivity implements OnGameListItemSelectedListener {
    private static final String TAG = "SearchResultsActivity";

    SparseArray<Group> groups = new SparseArray<>();

    @BindView(R.id.nothingfound_layout)
    LinearLayout nothingFoundLayout;
    @BindView(R.id.somethingfound_layout)
    RelativeLayout somethingfoundLayout;
    @BindView(R.id.search_button)
    Button searchButton;
    @BindView(R.id.nothingfound_text)
    TextView nothingFoundText;
    @BindView(R.id.nothingfound_title)
    TextView nothingFoundTitle;
    @BindView(R.id.reload)
    ImageView reloadView;
    @BindView(R.id.listView)
    ExpandableListView listView;

    @Inject
    Retrofit retrofit;

    private RestApi restApi;

    private List<Game> gamesFound;
    private String query;

    protected final int STEP_ONE_COMPLETE = 1;

    private Typeface latoFontBold;
    private Typeface latoFontLight;

    private Toolbar toolbar;
    private SearchresultExpandableListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        ButterKnife.bind(this);

        init();

        searchButton.setTypeface(latoFontBold);
        nothingFoundTitle.setTypeface(latoFontBold);
        nothingFoundText.setTypeface(latoFontLight);

        adapter = new SearchresultExpandableListAdapter(SearchResultsActivity.this, groups, this);
        listView.setAdapter(adapter);

        handleIntent(getIntent());
    }

    private void init() {
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(this);
        }

        ((CheatDatabaseApplication) getApplication()).getNetworkComponent().inject(this);
        restApi = retrofit.create(RestApi.class);

        latoFontLight = Tools.getFont(getAssets(), Konstanten.FONT_LIGHT);
        latoFontBold = Tools.getFont(getAssets(), Konstanten.FONT_BOLD);

        Tools.initToolbarBase(this, toolbar);
    }

    @Override
    public void onPause() {
        Reachability.unregister(this);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

            if (Reachability.reachability.isReachable) {
                searchNow();
            } else {
                reloadView.setVisibility(View.VISIBLE);
                reloadView.setOnClickListener(v -> {
                    if (Reachability.reachability.isReachable) {
                        searchNow();
                    } else {
                        Toast.makeText(SearchResultsActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                    }
                });
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void searchNow() {
        reloadView.setVisibility(View.GONE);

        Call<List<Game>> call = restApi.universalGameSearch(query, Konstanten.CURRENT_VERSION);
        call.enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(Call<List<Game>> cheats, Response<List<Game>> response) {
                gamesFound = response.body();
                createData();

                Message msg = Message.obtain();
                msg.what = STEP_ONE_COMPLETE;
                handler.sendMessage(msg);
            }

            @Override
            public void onFailure(Call<List<Game>> call, Throwable e) {
                Log.e(TAG, "searchGames onFailure: " + e.getLocalizedMessage());
                Needle.onMainThread().execute(() -> Toast.makeText(SearchResultsActivity.this, R.string.err_somethings_wrong, Toast.LENGTH_LONG).show());
            }
        });
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
            runOnUiThread(() -> {
                nothingFoundText.setText(getString(R.string.error_nothing_found_text));

                somethingfoundLayout.setVisibility(View.GONE);
                nothingFoundLayout.setVisibility(View.VISIBLE);
                searchButton.setOnClickListener(v -> onSearchRequested());
            });
        } else {
            if (gamesFound.size() > 0) {
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
                    Log.d(TAG, "System: " + systemsSorted.get(i) + "");

                    // Create groups with system names
                    Group group = new Group(systemsSorted.get(i) + "");

                    // Fill each group with the game names
                    for (Game game : gamesFound) {
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

    @Override
    public void onGameListItemSelected(Game game) {
        if (Reachability.reachability.isReachable) {
            Intent explicitIntent = new Intent(this, CheatsByGameListActivity.class);
            explicitIntent.putExtra("gameObj", game);
            startActivity(explicitIntent);
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }
}
