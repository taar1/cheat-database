package com.cheatdatabase.fragments;

import android.app.Fragment;
import android.graphics.Typeface;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.cheatdatabase.CheatDatabaseApplication;
import com.cheatdatabase.GamesBySystemActivity_;
import com.cheatdatabase.R;
import com.cheatdatabase.adapters.SystemsRecycleListViewAdapter;
import com.cheatdatabase.businessobjects.SystemPlatform;
import com.cheatdatabase.events.GamesAndCheatsCounterLoadedEventResult;
import com.cheatdatabase.events.SystemListRecyclerViewClickEvent;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.google.analytics.tracking.android.Tracker;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@EFragment(R.layout.fragment_systemlist)
public class SystemListFragment extends Fragment {

    private final String TAG = SystemListFragment.class.getSimpleName();

    private Typeface latoFontBold;
    private Typeface latoFontLight;

    public static final String IMAGE_RESOURCE_ID = "iconResourceID";
    public static final String ITEM_NAME = "itemName";

    private Tracker tracker;
    private static final String SCREEN_LABEL = SystemListFragment.class.getName();
    private static final String GA_TITLE = "SystemListFragment";

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<SystemPlatform> gameSystems;
//    private SwipeRefreshLayout mSwipeRefreshLayout;

    @ViewById(R.id.my_recycler_view)
    RecyclerView mRecyclerView;

    @ViewById(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    public SystemListFragment() {
    }

    @AfterViews
    public void onCreateView() {

        latoFontLight = Tools.getFont(getActivity().getAssets(), Konstanten.FONT_LIGHT);
        latoFontBold = Tools.getFont(getActivity().getAssets(), Konstanten.FONT_BOLD);

        // Update action bar menu items?
//        setHasOptionsMenu(true);

        Tools.initGA(getActivity(), tracker, SCREEN_LABEL, GA_TITLE, "");

//        View rootView = inflater.inflate(R.layout.fragment_systemlist, container, false);

//        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);
//        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                new LoadGamesAndCheatsCounterBackgroundTask().execute();
                loadGamesAndCheatsCounterBackground();
            }
        });

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Load Systems from XML. Load counters in background service later on.
        gameSystems = Tools.getGameSystemsFromXml(getActivity());

        // specify an adapter (see also next example)
        mAdapter = new SystemsRecycleListViewAdapter(gameSystems);
        mRecyclerView.setAdapter(mAdapter);

//        new LoadGamesAndCheatsCounterBackgroundTask().execute();
        loadGamesAndCheatsCounterBackground();

//        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        CheatDatabaseApplication.getEventBus().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        CheatDatabaseApplication.getEventBus().register(this);
    }

    public void onEvent(GamesAndCheatsCounterLoadedEventResult result) {
        if (result.isSucceeded()) {
            mAdapter = new SystemsRecycleListViewAdapter(result.getSystemPlatforms());
            mRecyclerView.setAdapter(mAdapter);
        } else {
            Toast.makeText(getActivity(), R.string.no_internet, Toast.LENGTH_LONG).show();
        }
    }

    public void onEvent(SystemListRecyclerViewClickEvent result) {
        if (result.isSucceeded()) {
//            Intent explicitIntent = new Intent(getActivity(), GamesBySystemActivity.class);
//            explicitIntent.putExtra("systemObj", result.getSystemPlatform());
//            startActivity(explicitIntent);

            GamesBySystemActivity_.intent(getActivity()).systemObj(result.getSystemPlatform()).start();
        } else {
            Toast.makeText(getActivity(), R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

//    private class LoadGamesAndCheatsCounterBackgroundTask extends AsyncTask<Void, Void, Void> {
//
//        GamesAndCheatsCounterLoadedEventResult gamesAndCheatsCounterLoadedEventResult;
//
//        @Override
//        protected Void doInBackground(Void... params) {
//
//            mSwipeRefreshLayout.setRefreshing(true);
//
//            ArrayList<SystemPlatform> systemGameandCheatCounterList = null;
//            try {
//                systemGameandCheatCounterList = Webservice.countGamesAndCheatsBySystem();
//            } catch (Exception e) {
//                Log.e("LoadGamesAndCheatsCounterBackgroundTask", "Load game and cheats counters failed: " + e.getLocalizedMessage());
//            }
//
//            if ((systemGameandCheatCounterList == null) || systemGameandCheatCounterList.size() == 0) {
//                gamesAndCheatsCounterLoadedEventResult = new GamesAndCheatsCounterLoadedEventResult(new Exception());
//            } else {
//                // Sorting
//                Collections.sort(systemGameandCheatCounterList, new Comparator<SystemPlatform>() {
//                    @Override
//                    public int compare(SystemPlatform system1, SystemPlatform system2) {
//                        return system1.getSystemName().compareTo(system2.getSystemName());
//                    }
//                });
//
//                gamesAndCheatsCounterLoadedEventResult = new GamesAndCheatsCounterLoadedEventResult(systemGameandCheatCounterList);
//
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            mSwipeRefreshLayout.setRefreshing(false);
//
//            super.onPostExecute(aVoid);
//            CheatDatabaseApplication.getEventBus().post(gamesAndCheatsCounterLoadedEventResult);
//        }
//    }

    GamesAndCheatsCounterLoadedEventResult gamesAndCheatsCounterLoadedEventResult;

    @Background
    void loadGamesAndCheatsCounterBackground() {
        mSwipeRefreshLayout.setRefreshing(true);

        ArrayList<SystemPlatform> systemGameandCheatCounterList = null;
        try {
            systemGameandCheatCounterList = Webservice.countGamesAndCheatsBySystem();
        } catch (Exception e) {
            Log.e(TAG, "Load game and cheats counters failed: " + e.getLocalizedMessage());
        }

        if ((systemGameandCheatCounterList == null) || systemGameandCheatCounterList.size() == 0) {
            gamesAndCheatsCounterLoadedEventResult = new GamesAndCheatsCounterLoadedEventResult(new Exception());
        } else {
            // Sorting
            Collections.sort(systemGameandCheatCounterList, new Comparator<SystemPlatform>() {
                @Override
                public int compare(SystemPlatform system1, SystemPlatform system2) {
                    return system1.getSystemName().compareTo(system2.getSystemName());
                }
            });

            gamesAndCheatsCounterLoadedEventResult = new GamesAndCheatsCounterLoadedEventResult(systemGameandCheatCounterList);

        }

        loadGamesAndCheatsCounterBackgroundFinished();
    }

    @UiThread
    void loadGamesAndCheatsCounterBackgroundFinished() {
        mSwipeRefreshLayout.setRefreshing(false);
        CheatDatabaseApplication.getEventBus().post(gamesAndCheatsCounterLoadedEventResult);
    }


}