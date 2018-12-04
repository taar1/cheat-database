package com.cheatdatabase.fragments;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.favorites.cheatview.FavoritesExpandableListAdapter;
import com.cheatdatabase.helpers.DatabaseHelper;
import com.cheatdatabase.helpers.Group;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import needle.Needle;

public class FavoriteGamesListFragment extends Fragment {

    SparseArray<Group> groups = new SparseArray<>();

    private FavoritesExpandableListAdapter adapter;

    protected final int STEP_ONE_COMPLETE = 1;
    private List<Game> gamesFound;

    private DatabaseHelper db;

    private Activity parentActivity;

    @BindView(R.id.listView)
    ExpandableListView listView;
    @BindView(R.id.somethingfound_layout)
    RelativeLayout somethingfoundLayout;
    @BindView(R.id.nothingfound_layout)
    LinearLayout nothingFoundLayout;
    @BindView(R.id.nothingfound_title)
    TextView nothingFoundTitle;
    @BindView(R.id.nothingfound_text)
    TextView nothingFoundText;

    private final int REMOVE_FROM_FAVORITES = 1;
    private Typeface latoFontLight;
    private Typeface latoFontBold;

    public static FavoriteGamesListFragment newInstance() {
        FavoriteGamesListFragment favoriteGamesListFragment = new FavoriteGamesListFragment();
        return favoriteGamesListFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites_main_list, container, false);
        ButterKnife.bind(this, view);


        Log.d("XXXXX", "XXXXX onCreateView XXXXX");

        db = new DatabaseHelper(getActivity());
        gamesFound = db.getAllFavoritedGames();
        adapter = new FavoritesExpandableListAdapter(parentActivity, groups);

        setHasOptionsMenu(true);

        nothingFoundTitle.setTypeface(latoFontBold);
        nothingFoundText.setTypeface(latoFontLight);

        listView.setAdapter(adapter);
        registerForContextMenu(listView);

        loadGames();

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        parentActivity = getActivity();

        latoFontLight = Tools.getFont(parentActivity.getAssets(), Konstanten.FONT_LIGHT);
        latoFontBold = Tools.getFont(parentActivity.getAssets(), Konstanten.FONT_BOLD);
    }

    private void loadGames() {
        gamesFound = db.getAllFavoritedGames();

        Message msg = Message.obtain();
        msg.what = STEP_ONE_COMPLETE;
        handler.sendMessage(msg);

        createData();
    }

    public void createData() {
        if (gamesFound == null) {
            handleEmptyState();
        } else {
            if ((gamesFound != null) && (gamesFound.size() > 0)) {
                Set<String> systems = new HashSet<>();
                // Get system names
                for (Game game : gamesFound) {
                    if ((game.getSystemName() != null) && (game.getSystemName().length() > 0)) {
                        systems.add(game.getSystemName());
                    }
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
                    for (Game game : gamesFound) {
                        if ((game.getSystemName() != null) && (game.getSystemName().length() > 0)) {
                            if (game.getSystemName().equalsIgnoreCase(systemsSorted.get(i))) {
                                group.children.add(game.getGameName());
                                group.gameChildren.add(game);
                            }
                        }
                    }
                    groups.append(i, group);
                }
            }
        }

    }

    public void handleEmptyState() {
        Needle.onMainThread().execute(() -> {
            nothingFoundText.setText(R.string.favorite_empty);
            somethingfoundLayout.setVisibility(View.GONE);
            nothingFoundLayout.setVisibility(View.VISIBLE);
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
            if (groups != null && listView != null) {
                for (int i = 0; i < groups.size(); i++) {
                    listView.expandGroup(i, false);
                }
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("XXXXX", "XXXXX laskdjaölskdhasöldjaölsdjaölsdjasdf XXXXX");
        switch (item.getItemId()) {
            case android.R.id.home:

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        listView.setAdapter(adapter);
        loadGames();
        adapter.notifyDataSetChanged();
    }

}
