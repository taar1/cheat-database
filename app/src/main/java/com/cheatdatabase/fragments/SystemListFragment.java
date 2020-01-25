package com.cheatdatabase.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cheatdatabase.R;
import com.cheatdatabase.activity.GamesBySystemListActivity;
import com.cheatdatabase.adapters.SystemsRecycleListViewAdapter;
import com.cheatdatabase.helpers.DatabaseHelper;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.listeners.OnSystemListItemSelectedListener;
import com.cheatdatabase.model.SystemPlatform;
import com.cheatdatabase.widgets.DividerDecoration;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import needle.Needle;

public class SystemListFragment extends Fragment implements OnSystemListItemSelectedListener {
    private final String TAG = SystemListFragment.class.getSimpleName();

    private List<SystemPlatform> systemGameandCheatCounterList;
    private SystemsRecycleListViewAdapter systemsRecycleListViewAdapter;

    @BindView(R.id.outer_layout)
    LinearLayout outerLayout;
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

        mSwipeRefreshLayout.setOnRefreshListener(() -> loadGamesAndCheatsCounterBackground());

        initAdapter(Tools.getGameSystemsFromXml(getActivity()));

        mSwipeRefreshLayout.setRefreshing(true);
        loadGamesAndCheatsCounterBackground();

        return view;
    }

    private void initAdapter(List<SystemPlatform> gameSystems) {
        systemsRecycleListViewAdapter = new SystemsRecycleListViewAdapter(this);
        systemsRecycleListViewAdapter.setSystemPlatforms(gameSystems);
        recyclerView.setAdapter(systemsRecycleListViewAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        recyclerView.addItemDecoration(new DividerDecoration(getActivity()));
        recyclerView.getItemAnimator().setRemoveDuration(50);
        recyclerView.setHasFixedSize(true);
    }

    private void loadGamesAndCheatsCounterBackground() {
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
            systemsRecycleListViewAdapter.setSystemPlatforms(systemGameandCheatCounterList);
            mSwipeRefreshLayout.setRefreshing(false);

            if ((systemGameandCheatCounterList == null) || (systemGameandCheatCounterList.size() < 1)) {
                try {
                    Tools.showSnackbar(outerLayout, getString(R.string.err_data_not_accessible));
                } catch (NullPointerException | IllegalStateException e) {
                    Log.e(TAG, "NullPointerException or IllegalStateException: " + e.getLocalizedMessage());
                }
            }
        });
    }

    @Override
    public void onSystemListItemSelected(SystemPlatform systemPlatform) {
        Intent explicitIntent = new Intent(getActivity(), GamesBySystemListActivity.class);
        explicitIntent.putExtra("systemObj", systemPlatform);
        startActivity(explicitIntent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
    }
}