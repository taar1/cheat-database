package com.cheatdatabase.fragments;

import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.cheatdatabase.GamesBySystemListActivity_;
import com.cheatdatabase.MainActivity;
import com.cheatdatabase.R;
import com.cheatdatabase.adapters.SystemsRecycleListViewAdapter;
import com.cheatdatabase.businessobjects.SystemPlatform;
import com.cheatdatabase.events.SystemListRecyclerViewClickEvent;
import com.cheatdatabase.helpers.CheatDatabaseAdapter;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.taskresults.GamesAndCheatsCountTaskResult;
import com.google.gson.Gson;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@EFragment(R.layout.fragment_systemlist)
public class SystemListFragment extends Fragment {

    private final String TAG = SystemListFragment.class.getSimpleName();
    private RecyclerView.LayoutManager mLayoutManager;
    private GamesAndCheatsCountTaskResult gamesAndCheatsCountTaskResult;
    private ArrayList<SystemPlatform> systemGameandCheatCounterList = null;

    @ViewById(R.id.my_recycler_view)
    RecyclerView mRecyclerView;
    @ViewById(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @FragmentArg(MainActivity.DRAWER_ITEM_ID)
    int mDrawerId;
    @FragmentArg(MainActivity.DRAWER_ITEM_NAME)
    String mDrawerName;

    @Bean
    Tools tools;
    @Bean
    SystemsRecycleListViewAdapter mSystemsRecycleListViewAdapter;
    @Bean
    CheatDatabaseAdapter db;

    @AfterViews
    public void onCreateView() {

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadGamesAndCheatsCounterBackground();
            }
        });

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        initAdapter(tools.getGameSystemsFromXml(getActivity()));

        mSwipeRefreshLayout.setRefreshing(true);
        loadGamesAndCheatsCounterBackground();
    }

    private void initAdapter(ArrayList<SystemPlatform> gameSystems) {
        mSystemsRecycleListViewAdapter.init(gameSystems);
        mRecyclerView.setAdapter(mSystemsRecycleListViewAdapter);
        mSystemsRecycleListViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void onEvent(GamesAndCheatsCountTaskResult result) {
        mSwipeRefreshLayout.setRefreshing(false);
        if (result.isSucceeded()) {
            initAdapter(result.getSystemPlatforms());
        } else {
            Toast.makeText(getActivity(), R.string.no_internet, Toast.LENGTH_LONG).show();
        }
    }

    @Subscribe
    public void onEvent(SystemListRecyclerViewClickEvent result) {
        if (result.isSucceeded()) {
            //CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "click").setLabel(result.getSystemPlatform().getSystemName()).build());
            GamesBySystemListActivity_.intent(getActivity()).systemObj(result.getSystemPlatform()).start();
        } else {
            Toast.makeText(getActivity(), R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    @Background
    public void loadGamesAndCheatsCounterBackground() {
        db.open();

        // SYSETM'S GAME COUNT WILL ONLY BE LOADED ONCE EVERY 24h
        boolean getSystemsAndCountsFromWebservice = false;

        ArrayList<SystemPlatform> systemsLocal = db.getAllSystemsAndCount();
        if (systemsLocal == null || systemsLocal.size() == 0) {
            getSystemsAndCountsFromWebservice = true;
        } else {
            // Check how old the database entries are. If over than 24 then
            // load them again from the webservice.

            long lastmod = systemsLocal.get(0).getLastModTimeStamp();

            long now = System.currentTimeMillis();
            long differenceInHours = (now - lastmod) / (1000 * 60 * 60);
            long differenceInMins = (now - lastmod) / (1000 * 60);

            Log.d(TAG, "NOW            : " + now);
            Log.d(TAG, "LAST UPDATE    : " + lastmod);
            Log.d(TAG, "DIFFERENCE in H: " + differenceInHours);
            Log.d(TAG, "DIFFERENCE in M: " + differenceInMins);

            if (differenceInHours > 23) {
                Log.d(TAG, "DIFFERENCE MORE THAN 23 HOURS. LOADING VALUES FROM WEBSERVICE AGAIN");
                getSystemsAndCountsFromWebservice = true;
            }
        }

        Log.d(TAG, "getSystemsAndCountsFromWebservice: " + getSystemsAndCountsFromWebservice);

        if (getSystemsAndCountsFromWebservice) {
            try {
                systemGameandCheatCounterList = Webservice.countGamesAndCheatsBySystem();
                Log.d(TAG, "Webservice countGamesAndCheatsBySystem() USED");

                // Update the local database
                db.updateSystemsAndCount(systemGameandCheatCounterList);
                Log.d(TAG, "SYSTEM VALUES FROM WEBSERVICE: " + new Gson().toJson(systemGameandCheatCounterList));

                if ((systemGameandCheatCounterList == null) || systemGameandCheatCounterList.size() == 0) {
                    db.deleteSystemsAndCount();
                    gamesAndCheatsCountTaskResult = new GamesAndCheatsCountTaskResult(new Exception());
                } else {
                    // Sorting
                    Collections.sort(systemGameandCheatCounterList, new Comparator<SystemPlatform>() {
                        @Override
                        public int compare(SystemPlatform system1, SystemPlatform system2) {
                            return system1.getSystemName().compareTo(system2.getSystemName());
                        }
                    });

                    gamesAndCheatsCountTaskResult = new GamesAndCheatsCountTaskResult(systemGameandCheatCounterList);
                }
            } catch (Exception e) {
                db.deleteSystemsAndCount();
                Log.e(TAG, "Load game and cheats counters failed: " + e.getLocalizedMessage());
                gamesAndCheatsCountTaskResult = new GamesAndCheatsCountTaskResult(new Exception());
            }

        } else {
            Log.d(TAG, "USED LOCAL DB DATA");
            gamesAndCheatsCountTaskResult = new GamesAndCheatsCountTaskResult(systemsLocal);
        }

        db.close();
        updateUI();
    }

    @UiThread
    void updateUI() {
        EventBus.getDefault().post(gamesAndCheatsCountTaskResult);
    }

}