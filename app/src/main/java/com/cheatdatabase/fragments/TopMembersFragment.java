package com.cheatdatabase.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.R;
import com.cheatdatabase.adapters.TopMembersListViewAdapter;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Webservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import needle.Needle;

/**
 * Show Top 20 helping memberList in a list.
 *
 * @author Dominik
 */
public class TopMembersFragment extends Fragment {
    private static final String TAG = TopMembersFragment.class.getSimpleName();
    private final int VISIT_WEBSITE = 0;

    private List<Member> memberList;
    private Member selectedMember;

    private Activity parentActivity;
    private RecyclerView.LayoutManager mLayoutManager;
    private TopMembersListViewAdapter mTopMembersListViewAdapter;

    @BindView(R.id.my_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.empty)
    View emptyView;
    @BindView(R.id.empty_label)
    TextView emptyLabel;

    public TopMembersFragment() {
        mTopMembersListViewAdapter = new TopMembersListViewAdapter();
        memberList = new ArrayList<>();
        parentActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topmembers, container, false);
        ButterKnife.bind(this, view);

        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(parentActivity);
        }

        setHasOptionsMenu(true);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMembersInBackground();
            }
        });

        if (Reachability.reachability.isReachable) {
            mLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setAdapter(mTopMembersListViewAdapter);
        } else {
            Toast.makeText(parentActivity, R.string.no_internet, Toast.LENGTH_LONG).show();
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadMembersInBackground();
    }

    @Override
    public void onPause() {
        Reachability.unregister(parentActivity);
        super.onPause();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        selectedMember = memberList.get(Integer.parseInt(String.valueOf(info.id)));

        menu.setHeaderTitle(R.string.context_menu_title);
        menu.add(0, VISIT_WEBSITE, 1, getString(R.string.top_members_visit_website, selectedMember.getUsername()));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case VISIT_WEBSITE:
                if (selectedMember.getWebsite().length() > 3) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(selectedMember.getWebsite()));
                    startActivity(intent);
                } else {
                    Toast.makeText(parentActivity, getString(R.string.top_members_no_website, selectedMember.getUsername()), Toast.LENGTH_LONG).show();
                }

                return true;
        }

        return super.onContextItemSelected(item);
    }

    public void loadMembersInBackground() {
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(true);
        }

        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    memberList.clear();
                    Collections.addAll(memberList, Webservice.getMemberTop20());
                } catch (Exception e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }

                notifyAdapter();
            }
        });

    }

    public void notifyAdapter() {
        mSwipeRefreshLayout.setRefreshing(true);

        Needle.onMainThread().execute(new Runnable() {
            @Override
            public void run() {
                if (memberList != null && memberList.size() > 0) {
                    mTopMembersListViewAdapter.setMemberList(memberList);
                    mTopMembersListViewAdapter.notifyDataSetChanged();
                } else {
                    handleEmptyViewState();
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void handleEmptyViewState() {
        if (mSwipeRefreshLayout == null || !isAdded()) {
            return;
        }
        if (mTopMembersListViewAdapter.getItemCount() == 0) {
            emptyLabel.setText(getString(R.string.err_no_member_data));
            emptyView.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setVisibility(View.GONE);
        } else {
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

}
