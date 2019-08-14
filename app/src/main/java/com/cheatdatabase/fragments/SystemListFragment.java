package com.cheatdatabase.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cheatdatabase.GamesBySystemListActivity;
import com.cheatdatabase.R;
import com.cheatdatabase.adapters.SystemsRecycleListViewAdapter;
import com.cheatdatabase.businessobjects.SystemPlatform;
import com.cheatdatabase.events.RemoteConfigLoadedEvent;
import com.cheatdatabase.events.SystemListRecyclerViewClickEvent;
import com.cheatdatabase.helpers.DatabaseHelper;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.taskresults.GamesAndCheatsCountTaskResult;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import needle.Needle;

public class SystemListFragment extends Fragment {
    private final String TAG = SystemListFragment.class.getSimpleName();

    private RecyclerView.LayoutManager mLayoutManager;
    private GamesAndCheatsCountTaskResult gamesAndCheatsCountTaskResult;
    private List<SystemPlatform> systemGameandCheatCounterList = null;
    private SystemsRecycleListViewAdapter systemsRecycleListViewAdapter;

    @BindView(R.id.my_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    public static SystemListFragment newInstance() {
        return new SystemListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_systemlist, container, false);
        ButterKnife.bind(this, view);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadGamesAndCheatsCounterBackground();
            }
        });

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        initAdapter(Tools.getGameSystemsFromXml(getActivity()));

        mSwipeRefreshLayout.setRefreshing(true);
        loadGamesAndCheatsCounterBackground();

        return view;
    }

    private void initAdapter(List<SystemPlatform> gameSystems) {
        systemsRecycleListViewAdapter = new SystemsRecycleListViewAdapter();
        systemsRecycleListViewAdapter.setSystemPlatforms(gameSystems);
        recyclerView.setAdapter(systemsRecycleListViewAdapter);
        systemsRecycleListViewAdapter.notifyDataSetChanged();
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

//    @Subscribe
//    public void onEvent(GamesAndCheatsCountTaskResult result) {
//        mSwipeRefreshLayout.setRefreshing(false);
//        if (result.isSucceeded()) {
//            initAdapter(result.getSystemPlatforms());
//        } else {
//            Toast.makeText(getActivity(), R.string.no_internet, Toast.LENGTH_LONG).show();
//        }
//    }


    @Subscribe
    public void onEvent(SystemListRecyclerViewClickEvent result) {
        if (result.isSucceeded()) {
            Intent explicitIntent = new Intent(getActivity(), GamesBySystemListActivity.class);
            explicitIntent.putExtra("systemObj", result.getSystemPlatform());
            startActivity(explicitIntent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());

        } else {
            Toast.makeText(getActivity(), R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe
    public void onEvent(RemoteConfigLoadedEvent event) {
        Log.d(TAG, "XXXXX RemoteConfigLoadedEvent FIRED");
        // TODO bei getGameSystemsFromXml() die REMOTE CONFIG verwenden
        // TODO: mFirebaseRemoteConfig.getBoolean(REMOTE_CONFIG_HACKS_ENABLED_KEY)
        // TODO vorher in MainActivity.java noch die remote config values in die sharedpreferences speichern, damit man von hier aus drauf zugreifen kann
        // TODO vorher in MainActivity.java noch die remote config values in die sharedpreferences speichern, damit man von hier aus drauf zugreifen kann
        // TODO vorher in MainActivity.java noch die remote config values in die sharedpreferences speichern, damit man von hier aus drauf zugreifen kann
        // TODO vorher in MainActivity.java noch die remote config values in die sharedpreferences speichern, damit man von hier aus drauf zugreifen kann
        // TODO vorher in MainActivity.java noch die remote config values in die sharedpreferences speichern, damit man von hier aus drauf zugreifen kann

        initAdapter(Tools.getGameSystemsFromXml(getActivity()));
        mSwipeRefreshLayout.setRefreshing(true);
        loadGamesAndCheatsCounterBackground();
    }

    public void loadGamesAndCheatsCounterBackground() {
        Needle.onBackgroundThread().execute(() -> {
            DatabaseHelper db = new DatabaseHelper(getActivity());

            // System Game count will only be updated every 24h
            boolean getSystemsAndCountsFromWebservice = false;

            List<SystemPlatform> systemsLocal = db.getAllSystemsAndCount();
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

                    if ((systemGameandCheatCounterList == null) || (systemGameandCheatCounterList.size() < 1)) {
                        db.deleteSystemsAndCount();
                    } else {
                        // Sort the systems by name
                        Collections.sort(systemGameandCheatCounterList, (system1, system2) -> system1.getSystemName().toLowerCase().compareTo(system2.getSystemName().toLowerCase()));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Load game and cheats counters failed: " + e.getLocalizedMessage());
                }
            } else {
                systemGameandCheatCounterList = systemsLocal;
            }

            updateUI();
        });
    }

    private void updateUI() {
        Needle.onMainThread().execute(() -> {
            try {
                mSwipeRefreshLayout.setRefreshing(false);

                if ((systemGameandCheatCounterList == null) || (systemGameandCheatCounterList.size() < 1)) {
                    Toast.makeText(getActivity(), R.string.no_internet, Toast.LENGTH_LONG).show();
                } else {
                    initAdapter(systemGameandCheatCounterList);
                }
            } catch (NullPointerException ignored) {
            }

        });

    }

}