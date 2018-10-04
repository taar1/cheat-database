package com.cheatdatabase.fragments;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.MainActivity;
import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.SystemPlatform;
import com.cheatdatabase.favorites.cheatview.FavoritesExpandableListAdapter;
import com.cheatdatabase.helpers.CheatDatabaseAdapter;
import com.cheatdatabase.helpers.Group;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EFragment(R.layout.fragment_favorites_main_list)
public class FavoriteGamesListFragment extends Fragment {

    SparseArray<Group> groups = new SparseArray<>();

    protected SystemPlatform systemObj;

    private FavoritesExpandableListAdapter adapter;

    protected final int STEP_ONE_COMPLETE = 1;
    private Game[] gamesFound;

    private CheatDatabaseAdapter db;

    private Activity parentActivity;

    @ViewById(R.id.listView)
    ExpandableListView listView;

    @ViewById(R.id.somethingfound_layout)
    RelativeLayout somethingfoundLayout;

    @ViewById(R.id.nothingfound_layout)
    LinearLayout nothingFoundLayout;

    @ViewById(R.id.nothingfound_title)
    TextView nothingFoundTitle;

    @ViewById(R.id.nothingfound_text)
    TextView nothingFoundText;

    @FragmentArg(MainActivity.DRAWER_ITEM_ID)
    int mDrawerId;

    @FragmentArg(MainActivity.DRAWER_ITEM_NAME)
    String mDrawerName;

    private static final String SCREEN_LABEL = "Favorites Main Screen";

    private final int REMOVE_FROM_FAVORITES = 1;

    @AfterViews
    public void createView() {
        parentActivity = getActivity();

        Typeface latoFontLight = Tools.getFont(parentActivity.getAssets(), Konstanten.FONT_LIGHT);
        Typeface latoFontBold = Tools.getFont(parentActivity.getAssets(), Konstanten.FONT_BOLD);

        db = new CheatDatabaseAdapter(parentActivity);
        db.open();

        //CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "User Favorites").setLabel("activity").build());
        adapter = new FavoritesExpandableListAdapter(parentActivity, groups);

        // TODO FIXME CONTEXT MENU DOES NOT WORK YET

        // Update action bar menu items?
        setHasOptionsMenu(true);

        nothingFoundTitle.setTypeface(latoFontBold);
        nothingFoundText.setTypeface(latoFontLight);

        listView.setAdapter(adapter);
        getViewContent();

        registerForContextMenu(listView);
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
//            nothingFoundText.setText(R.string.favorite_empty);
//
//            somethingfoundLayout.setVisibility(View.GONE);
//            nothingFoundLayout.setVisibility(View.VISIBLE);
            changeView();
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

    @UiThread
    public void changeView() {
        nothingFoundText.setText(R.string.favorite_empty);
        somethingfoundLayout.setVisibility(View.GONE);
        nothingFoundLayout.setVisibility(View.VISIBLE);
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
        if (db.deleteCheats(selectedGame)) {
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
