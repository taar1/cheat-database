package com.cheatdatabase.fragments;

import android.app.Activity;
import android.os.Bundle;
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

import androidx.fragment.app.Fragment;

import com.cheatdatabase.R;
import com.cheatdatabase.adapters.FavoritesExpandableListAdapter;
import com.cheatdatabase.data.RoomCheatDatabase;
import com.cheatdatabase.data.dao.FavoriteCheatDao;
import com.cheatdatabase.data.model.FavoriteCheatModel;
import com.cheatdatabase.helpers.Group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import needle.Needle;

public class FavoriteGamesListFragment extends Fragment {
    private static final String TAG = "FavoriteGamesListFragme";

    SparseArray<Group> groups = new SparseArray<>();

    private FavoritesExpandableListAdapter adapter;

    //    private List<Game> gamesFound;
    private List<FavoriteCheatModel> favoriteCheatsList;

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

    private FavoriteCheatDao dao;


    public static FavoriteGamesListFragment newInstance() {
        FavoriteGamesListFragment favoriteGamesListFragment = new FavoriteGamesListFragment();
        return favoriteGamesListFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites_main_list, container, false);
        ButterKnife.bind(this, view);

        dao = RoomCheatDatabase.getDatabase(getActivity()).favoriteDao();
        favoriteCheatsList = dao.getAll();

        adapter = new FavoritesExpandableListAdapter(parentActivity, groups);

        setHasOptionsMenu(true);

        listView.setAdapter(adapter);
        registerForContextMenu(listView);

        loadGames();

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = getActivity();
    }

    private void loadGames() {
        favoriteCheatsList = dao.getAll();

        fillList();
        createData();
    }

    private void fillList() {
        if (groups != null && listView != null) {
            for (int i = 0; i < groups.size(); i++) {
                listView.expandGroup(i, false);
            }
        }
    }

    public void createData() {
        if (favoriteCheatsList == null) {
            handleEmptyState();
        } else {
            if (favoriteCheatsList.size() > 0) {
                Set<String> systems = new HashSet<>();
                // Get system names
                for (FavoriteCheatModel favoriteCheat : favoriteCheatsList) {
                    if ((favoriteCheat.getSystemName() != null) && (favoriteCheat.getSystemName().length() > 0)) {
                        systems.add(favoriteCheat.getSystemName());
                    }
                }

                // Sort system names
                List<String> systemsSorted = new ArrayList<>(systems);
                Collections.sort(systemsSorted);

                // Go through systems
                for (int i = 0; i < systemsSorted.size(); i++) {
                    Log.i(TAG, "systemsSorted: " + systemsSorted.get(i));

                    // Create groups with system names
                    Group group = new Group(systemsSorted.get(i) + "");

                    // Fill each group with the game names
                    for (FavoriteCheatModel favoriteCheat : favoriteCheatsList) {
                        if ((favoriteCheat.getSystemName() != null) && (favoriteCheat.getSystemName().length() > 0)) {
                            if (favoriteCheat.getSystemName().equalsIgnoreCase(systemsSorted.get(i))) {
                                group.children.add(favoriteCheat.getGameName());
                                group.gameChildren.add(favoriteCheat.toGame());
                            }
                        }
                    }
                    groups.append(i, group);
                }
            }
        }
    }

    private void handleEmptyState() {
        Needle.onMainThread().execute(() -> {
            nothingFoundText.setText(R.string.favorite_empty);
            somethingfoundLayout.setVisibility(View.GONE);
            nothingFoundLayout.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        listView.setAdapter(adapter);
        loadGames();
        adapter.notifyDataSetChanged();
    }

}
