package com.cheatdatabase.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.MainActivity;
import com.cheatdatabase.R;
import com.cheatdatabase.adapters.TopMembersListViewAdapter;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.google.analytics.tracking.android.Tracker;
import com.squareup.picasso.Picasso;

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

//    private ProgressDialog mProgressDialog = null;
//    private TopMembersAdapter topMembersAdapter;

    private final int VISIT_WEBSITE = 0;

    private Tracker tracker;
    public String[] myRemoteImages;

    private RecyclerView.LayoutManager mLayoutManager;

    private Typeface latoFontBold;
    private Typeface latoFontLight;
    private Activity parentActivity;

    private static final String SCREEN_LABEL = TopMembersFragment.class.getName();
    private static final String GA_TITLE = "Top Members ListView";

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

//            startTopMembersAdapter();
            getMembersInBackground();
        } else {
//            if (reloadView != null) {
//                reloadView.setOnClickListener(new OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        if (Reachability.reachability.isReachable) {
//                            reloadView.setVisibility(View.INVISIBLE);
//                            startTopMembersAdapter();
//                        } else {
//                            Toast.makeText(parentActivity, R.string.no_internet, Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//
//            }

            Toast.makeText(parentActivity, R.string.no_internet, Toast.LENGTH_LONG).show();
        }

        tools.initGA(parentActivity, tracker, SCREEN_LABEL, GA_TITLE, "");
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

//    private void startTopMembersAdapter() {
//
//        topMembersAdapter = new TopMembersAdapter(parentActivity, R.layout.fragment_topmembers);
//        setListAdapter(topMembersAdapter);
//        mProgressDialog = ProgressDialog.show(parentActivity, getString(R.string.please_wait) + "...", getString(R.string.retrieving_data) + "...", true);
//
////        getMembers();
//        getMembersInBackground();
//    }

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

//    @Override
//    public void onListItemClick(ListView l, View v, int position, long id) {
//        super.onListItemClick(l, v, position, id);
//
//        if (Reachability.reachability.isReachable) {
//            Intent explicitIntent = new Intent(parentActivity, MemberCheatListActivity.class);
//            explicitIntent.putExtra("memberObj", members[position]);
//            startActivity(explicitIntent);
//        } else {
//            Toast.makeText(parentActivity, R.string.no_internet, Toast.LENGTH_SHORT).show();
//        }
//    }


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

//            topMembersAdapter.notifyDataSetChanged();
//            for (int i = 0; i < members.length; i++) {
//                topMembersAdapter.add(members[i]);
//            }
//            mProgressDialog.dismiss();
//            topMembersAdapter.notifyDataSetChanged();

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

    /**
     * Adapter to fill the list with the member data
     *
     * @author Dominik
     */
//    private class TopMembersAdapter extends ArrayAdapter<Member> {
//
//        public TopMembersAdapter(Context context, int textViewResourceId) {
//            super(context, textViewResourceId);
//        }
//
//        private void openWebsite(String url) {
//            // TODO member per email informieren, dass jemand seine homepage
//            // geoeffnet hat.
//            if ((url != null) && (url.length() > 4)) {
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setData(Uri.parse(url));
//                startActivity(intent);
//            }
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//
//            View cView = convertView;
//            LayoutInflater vi = (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            cView = vi.inflate(R.layout.topmembers_list_item, null);
//            cView.setDrawingCacheEnabled(true);
//            cView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 320));
//
////            LinearLayout ol = (LinearLayout) parentActivity.findViewById(R.id.outerLayout);
////            ol.setBackgroundResource(R.drawable.bg);
//
//			/*
//             * Dies ist bereis ein Loop der durch die ArrayList geht!
//			 */
//            try {
//                final Member member = members[position];
//
//                ImageView avatarImageView = (ImageView) cView.findViewById(R.id.avatar);
//                avatarImageView.setOnClickListener(new OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        openWebsite(member.getWebsite());
//                    }
//
//                });
//
//                Picasso.with(parentActivity.getApplicationContext()).load(Konstanten.WEBDIR_MEMBER_AVATAR + member.getMid()).placeholder(R.drawable.avatar).into(avatarImageView);
//
//                TextView tvNumeration = (TextView) cView.findViewById(R.id.numeration);
//                tvNumeration.setText(String.valueOf(position + 1));
//                tvNumeration.setTypeface(latoFontBold);
//
//                TextView tvMemberName = (TextView) cView.findViewById(R.id.membername);
//                tvMemberName.setText(member.getUsername().toUpperCase());
//                tvMemberName.setTypeface(latoFontBold);
//
//                TextView tvCheatCount = (TextView) cView.findViewById(R.id.cheatcount);
//                tvCheatCount.setText(getString(R.string.top_members_cheats_count) + ": " + String.valueOf(member.getCheatSubmissionCount()));
//                tvCheatCount.setTypeface(latoFontLight);
//
//                TextView tvHiMessage = (TextView) cView.findViewById(R.id.hi_message);
//                if (member.getGreeting().length() > 1) {
//                    tvHiMessage.setText("\"" + member.getGreeting().replaceAll("\\\\", "").trim() + "\"");
//                } else {
//                    tvHiMessage.setVisibility(View.GONE);
//                }
//                tvHiMessage.setTypeface(latoFontLight);
//
//                TextView tvWebsite = (TextView) cView.findViewById(R.id.website);
//                if (member.getWebsite().length() > 1) {
//                    tvWebsite.setText(member.getWebsite());
//                } else {
//                    tvWebsite.setVisibility(View.GONE);
//                }
//                tvWebsite.setOnClickListener(new OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        openWebsite(member.getWebsite());
//                    }
//                });
//                tvWebsite.setTypeface(latoFontLight);
//                tvWebsite.setDrawingCacheEnabled(false);
//            } catch (Exception e) {
//                Log.e(TAG, "on position: " + position + ": " + e.getMessage());
//                error(R.string.err_no_member_data);
//            }
//            return cView;
//        }
//
//    }

}
