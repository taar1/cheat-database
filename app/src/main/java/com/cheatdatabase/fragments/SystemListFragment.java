package com.cheatdatabase.fragments;

import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.cheatdatabase.CheatDatabaseApplication;
import com.cheatdatabase.GamesBySystemActivity_;
import com.cheatdatabase.R;
import com.cheatdatabase.adapters.SystemsRecycleListViewAdapter;
import com.cheatdatabase.businessobjects.SystemPlatform;
import com.cheatdatabase.taskresults.GamesAndCheatsCountTaskResult;
import com.cheatdatabase.events.SystemListRecyclerViewClickEvent;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.tasks.GamesAndCheatsCountTask;
import com.google.analytics.tracking.android.Tracker;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

@EFragment(R.layout.fragment_systemlist)
public class SystemListFragment extends Fragment {

    private final String TAG = SystemListFragment.class.getSimpleName();

    private Tracker tracker;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<SystemPlatform> gameSystems;

    @ViewById(R.id.my_recycler_view)
    RecyclerView mRecyclerView;

    @ViewById(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Bean
    Tools tools;

    @Bean
    GamesAndCheatsCountTask gamesAndCheatsCountTask;

    @AfterViews
    public void onCreateView() {

        // Load Systems from XML. Load counters in background service later on.
        gameSystems = tools.getGameSystemsFromXml(getActivity());

        tools.initGA(getActivity(), tracker, TAG, TAG, "");

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                gamesAndCheatsCountTask.loadGamesAndCheatsCounterBackground();
            }
        });

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new SystemsRecycleListViewAdapter(gameSystems);
        mRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout.setRefreshing(true);
        gamesAndCheatsCountTask.loadGamesAndCheatsCounterBackground();
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

    public void onEvent(GamesAndCheatsCountTaskResult result) {
        mSwipeRefreshLayout.setRefreshing(false);
        if (result.isSucceeded()) {
            mAdapter = new SystemsRecycleListViewAdapter(result.getSystemPlatforms());
            mRecyclerView.setAdapter(mAdapter);
        } else {
            Toast.makeText(getActivity(), R.string.no_internet, Toast.LENGTH_LONG).show();
        }
    }

    public void onEvent(SystemListRecyclerViewClickEvent result) {
        if (result.isSucceeded()) {
            GamesBySystemActivity_.intent(getActivity()).systemObj(result.getSystemPlatform()).start();
        } else {
            Toast.makeText(getActivity(), R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

}