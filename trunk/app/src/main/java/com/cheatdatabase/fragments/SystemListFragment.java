package com.cheatdatabase.fragments;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheatdatabase.MainActivity;
import com.cheatdatabase.R;
import com.cheatdatabase.adapters.SystemsRecycleListViewAdapter;
import com.cheatdatabase.businessobjects.SystemPlatform;
import com.cheatdatabase.events.GamesAndCheatsCounterLoadedEventResult;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.google.analytics.tracking.android.Tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SystemListFragment extends Fragment {

    private Typeface latoFontBold;
    private Typeface latoFontLight;

    public static final String IMAGE_RESOURCE_ID = "iconResourceID";
    public static final String ITEM_NAME = "itemName";

    private Tracker tracker;
    private static final String SCREEN_LABEL = SystemListFragment.class.getName();
    private static final String GA_TITLE = "SystemListFragment";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public SystemListFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        latoFontLight = Tools.getFont(getActivity().getAssets(), Konstanten.FONT_LIGHT);
        latoFontBold = Tools.getFont(getActivity().getAssets(), Konstanten.FONT_BOLD);

        // Update action bar menu items?
//        setHasOptionsMenu(true);

        Tools.initGA(getActivity(), tracker, SCREEN_LABEL, GA_TITLE, "");

        View rootView = inflater.inflate(R.layout.fragment_systemlist, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Load Systems from XML. Load counters in background service later on.
        ArrayList<SystemPlatform> gameSystems = Tools.getGameSystemsFromXml(getActivity());

        // specify an adapter (see also next example)
        mAdapter = new SystemsRecycleListViewAdapter(gameSystems);
        mRecyclerView.setAdapter(mAdapter);

        new LoadGamesAndCheatsCounterBackgroundTask().execute();

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity.getEventBus().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getEventBus().register(this);
    }

    public void onEvent(GamesAndCheatsCounterLoadedEventResult result) {
        if (result.isSucceeded()) {
            mAdapter = new SystemsRecycleListViewAdapter(result.getSystemPlatforms());
            mRecyclerView.setAdapter(mAdapter);
        }
    }


//    @Override
//    public void onListItemClick(ListView l, View v, int position, long id) {
//        super.onListItemClick(l, v, position, id);
//
//        if (Reachability.reachability.isReachable) {
//            Intent explicitIntent = new Intent(getActivity(), GamesBySystemActivity.class);
//            explicitIntent.putExtra("systemObj", Tools.getSystemObjectByName(getActivity(), allSystemNames.get(position).getSystemName()));
//            startActivity(explicitIntent);
//        } else {
//            Toast.makeText(getActivity(), R.string.no_internet, Toast.LENGTH_SHORT).show();
//        }
//    }


    // TODO onListItemClick noch machen in der recycleview
    // TODO onListItemClick noch machen in der recycleview
    // TODO onListItemClick noch machen in der recycleview
    // TODO onListItemClick noch machen in der recycleview
    // TODO onListItemClick noch machen in der recycleview
    // TODO onListItemClick noch machen in der recycleview
    // TODO onListItemClick noch machen in der recycleview
    // TODO onListItemClick noch machen in der recycleview
    // TODO onListItemClick noch machen in der recycleview
    // TODO onListItemClick noch machen in der recycleview
    // TODO onListItemClick noch machen in der recycleview
    // TODO onListItemClick noch machen in der recycleview


    private class LoadGamesAndCheatsCounterBackgroundTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            ArrayList<SystemPlatform> systemGameandCheatCounterList = null;
            try {
                systemGameandCheatCounterList = Webservice.countGamesAndCheatsBySystem();
            } catch (Exception e) {
                Log.e("LoadGamesAndCheatsCounterBackgroundTask", "Load game and cheats counters failed: " + e.getLocalizedMessage());
            }

            GamesAndCheatsCounterLoadedEventResult gamesAndCheatsCounterLoadedEventResult;
            if (systemGameandCheatCounterList == null) {
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

            MainActivity.getEventBus().post(gamesAndCheatsCounterLoadedEventResult);
            return null;
        }

    }

}