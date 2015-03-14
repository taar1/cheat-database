package com.cheatdatabase.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.SystemPlatform;
import com.cheatdatabase.favorites.cheatview.FavoritesExpandableListAdapter;
import com.cheatdatabase.helpers.CheatDatabaseAdapter;
import com.cheatdatabase.helpers.Group;
import com.cheatdatabase.helpers.Tools;
import com.google.analytics.tracking.android.Tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoriteGamesListFragment extends Fragment {

    SparseArray<Group> groups = new SparseArray<Group>();

    protected SystemPlatform systemObj;

    private Tracker tracker;

    private ExpandableListView listView;
    private FavoritesExpandableListAdapter adapter;

    protected final int STEP_ONE_COMPLETE = 1;
    private Game[] gamesFound;

    private Typeface latoFontLight;
    private Typeface latoFontBold;

    private CheatDatabaseAdapter db;

    private Activity parentActivity;

    private RelativeLayout somethingfoundLayout;
    private LinearLayout nothingFoundLayout;

    private TextView nothingFoundTitle;
    private TextView nothingFoundText;

    private static final String SCREEN_LABEL = "Favorites Main Screen";

    public static final String IMAGE_RESOURCE_ID = "iconResourceID";
    public static final String ITEM_NAME = "itemName";

    private final int REMOVE_FROM_FAVORITES = 1;

    private View rootView;

    public FavoriteGamesListFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = getActivity();

        latoFontLight = Tools.getFont(parentActivity.getAssets(), "Lato-Light.ttf");
        latoFontBold = Tools.getFont(parentActivity.getAssets(), "Lato-Bold.ttf");

        db = new CheatDatabaseAdapter(parentActivity);
        db.open();

        Tools.initGA(parentActivity, tracker, SCREEN_LABEL, "Favorites", "User favorites");
        adapter = new FavoritesExpandableListAdapter(parentActivity, groups);

        // TODO FIXME CONTEXT MENU DOES NOT WORK YET

        // Update action bar menu items?
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_favorites_main_list, container, false);

        somethingfoundLayout = (RelativeLayout) rootView.findViewById(R.id.somethingfound_layout);
        nothingFoundLayout = (LinearLayout) rootView.findViewById(R.id.nothingfound_layout);

        nothingFoundTitle = (TextView) rootView.findViewById(R.id.nothingfound_title);
        nothingFoundTitle.setTypeface(latoFontBold);
        nothingFoundText = (TextView) rootView.findViewById(R.id.nothingfound_text);
        nothingFoundText.setTypeface(latoFontLight);

        listView = (ExpandableListView) rootView.findViewById(R.id.listView);

        listView.setAdapter(adapter);
        getViewContent();

        registerForContextMenu(listView);
        registerForContextMenu(rootView);

        return rootView;
    }

    private void getViewContent() {

        new Thread(new Runnable() {

            @Override
            public void run() {
                gamesFound = db.getAllFavoritedGames();

                Message msg = Message.obtain();
                msg.what = STEP_ONE_COMPLETE;
                handler.sendMessage(msg);

                createData();
            }
        }).start();

    }

    public void createData() {

        // If nothing found then display a message.
        if (gamesFound == null) {
            nothingFoundText.setText(R.string.favorite_empty);

            somethingfoundLayout.setVisibility(View.GONE);
            nothingFoundLayout.setVisibility(View.VISIBLE);
        } else {
            if ((gamesFound != null) && (gamesFound.length > 0)) {
                Set<String> systems = new HashSet<String>();
                // Get system names
                for (int i = 0; i < gamesFound.length; i++) {
                    Game game = gamesFound[i];
                    systems.add(game.getSystemName());
                }

                // Sort system names
                List<String> systemsSorted = new ArrayList<String>(systems);
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

    // TODO FIXME CONTEXT MENU IS NOT WORKING YET...
    private Game selectedGame;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int child = ExpandableListView.getPackedPositionChild(info.packedPosition);

        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            selectedGame = gamesFound[Integer.parseInt(String.valueOf(info.id))];

            menu.setHeaderTitle("\"" + selectedGame.getGameName() + "\"");
            menu.add(0, REMOVE_FROM_FAVORITES, 1, R.string.remove_favorite);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();

        // TODO: CONTEXT MENU CLICK NOT WORKING YET
        int groupPos = 0, childPos = 0;

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
            childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
        }

        switch (item.getItemId()) {
            case REMOVE_FROM_FAVORITES:
                removeFavorite();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * Deletes a game from the local favorites database table.
     */
    private void removeFavorite() {
        if (db.deleteFavorites(selectedGame) == true) {
            Toast.makeText(parentActivity, getString(R.string.remove_favorites_ok, "'" + selectedGame.getGameName() + "'"), Toast.LENGTH_SHORT).show();

            // TODO liste neu laden
            gamesFound = db.getAllFavoritedGames();

        } else {
            Toast.makeText(parentActivity, R.string.remove_favorites_nok, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        listView.setAdapter(adapter);
        getViewContent();
        adapter.notifyDataSetChanged();
    }

}
