package com.cheatdatabase.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.cheatdatabase.R;
import com.cheatdatabase.adapters.TopMembersListViewAdapter;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Webservice;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import needle.Needle;

/**
 * Show Top 20 helping members in a list.
 *
 * @author Dominik
 */
public class TopMembersFragment extends Fragment {
    private static final String TAG = TopMembersFragment.class.getSimpleName();
    private final int VISIT_WEBSITE = 0;

    private List<Member> members;
    private Member selectedMember;

    private Activity parentActivity;
    private RecyclerView.LayoutManager mLayoutManager;
    private TopMembersListViewAdapter mTopMembersListViewAdapter;

    @BindView(R.id.my_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topmembers, container, false);
        ButterKnife.bind(this, view);

        parentActivity = getActivity();

        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(parentActivity);
        }

        mTopMembersListViewAdapter = new TopMembersListViewAdapter();

        // Update action bar menu items?
        setHasOptionsMenu(true);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMembersInBackground();
            }
        });

        if (Reachability.reachability.isReachable) {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            mLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLayoutManager);

            mSwipeRefreshLayout.setRefreshing(true);

            getMembersInBackground();
        } else {
            Toast.makeText(parentActivity, R.string.no_internet, Toast.LENGTH_LONG).show();
        }

        return view;
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
        selectedMember = members.get(Integer.parseInt(String.valueOf(info.id)));

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

    public void getMembersInBackground() {
        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    members.clear();
                    Collections.addAll(members, Webservice.getMemberTop20());
                } catch (Exception e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }

                notifyAdapter();
            }
        });

    }

    public void notifyAdapter() {
        Needle.onMainThread().execute(new Runnable() {
            @Override
            public void run() {
                if (members != null && members.size() > 0) {

                    mTopMembersListViewAdapter.setMemberList(members);
                    if (mTopMembersListViewAdapter != null) {
                        mRecyclerView.setAdapter(mTopMembersListViewAdapter);
                    }
                    mTopMembersListViewAdapter.notifyDataSetChanged();
                    mSwipeRefreshLayout.setRefreshing(false);
                } else {
                    // TODO display empty view
                    // TODO display empty view
                    // TODO display empty view
                    error(R.string.err_no_member_data);
                }
            }
        });

    }


    private void error(int msg) {
        new AlertDialog.Builder(parentActivity).setIcon(R.drawable.ic_action_warning).setTitle(getString(R.string.err)).setMessage(msg).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                // finish();
            }
        }).create().show();
    }

}
