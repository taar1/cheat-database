package com.cheatdatabase.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;

import com.cheatdatabase.MainActivity;
import com.cheatdatabase.R;
import com.cheatdatabase.adapters.TopMembersListViewAdapter;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

/**
 * Show Top 20 helping members in a list.
 *
 * @author Dominik
 */
@EFragment(R.layout.fragment_topmembers)
public class TopMembersFragment extends Fragment {

    private static final String TAG = TopMembersFragment.class.getSimpleName();

    private Member[] members;
    private Member selectedMember;

    private final int VISIT_WEBSITE = 0;

    public String[] myRemoteImages;

    private RecyclerView.LayoutManager mLayoutManager;

    private Typeface latoFontBold;
    private Typeface latoFontLight;
    private Activity parentActivity;

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
    TopMembersListViewAdapter mTopMembersListViewAdapter;


    @AfterViews
    public void onCreateView() {
        parentActivity = getActivity();
        Reachability.registerReachability(parentActivity);

        latoFontLight = tools.getFont(parentActivity.getAssets(), Konstanten.FONT_LIGHT);
        latoFontBold = tools.getFont(parentActivity.getAssets(), Konstanten.FONT_BOLD);

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

    }

    @Override
    public void onPause() {
        Reachability.unregister(parentActivity);
        super.onPause();
    }

    private void initAdapter(ArrayList<Member> members) {
        mTopMembersListViewAdapter.init(members);
        mRecyclerView.setAdapter(mTopMembersListViewAdapter);
        mTopMembersListViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        selectedMember = members[Integer.parseInt(String.valueOf(info.id))];
        menu.setHeaderTitle(R.string.context_menu_title);
        menu.add(0, VISIT_WEBSITE, 1, String.format(getString(R.string.top_members_visit_website, selectedMember.getUsername())));
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
                    Toast.makeText(parentActivity, String.format(getString(R.string.top_members_no_website, selectedMember.getUsername())), Toast.LENGTH_LONG).show();
                }

                return true;
        }

        return super.onContextItemSelected(item);
    }

    @Background(serial = "getMemberTop20")
    public void getMembersInBackground() {
        try {
            members = Webservice.getMemberTop20();
            notifyAdapter();
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
            error(R.string.err_no_member_data);
        }
    }

    @UiThread
    public void notifyAdapter() {
        if (members != null && members.length > 0) {

            ArrayList<Member> memberList = new ArrayList<>();
            for (Member member : members) {
                memberList.add(member);
            }

            initAdapter(memberList);
        } else {
            error(R.string.err_data_not_accessible);
        }
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
