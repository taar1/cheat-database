package com.cheatdatabase.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.MemberCheatListActivity;
import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.google.analytics.tracking.android.Tracker;
import com.squareup.picasso.Picasso;

/**
 * Show Top 20 helping members in a list.
 *
 * @author Dominik
 */
public class TopMembersFragment extends ListFragment {

    private Member[] members;
    private Member selectedMember;

    private ProgressDialog mProgressDialog = null;
    private TopMembersAdapter topMembersAdapter;

    private final int VISIT_WEBSITE = 0;

    private Tracker tracker;
    public String[] myRemoteImages;

    private Typeface latoFontBold;
    private Typeface latoFontLight;

    private View rootView;
    private ImageView reloadView;
    private Activity ca;

    private static final String SCREEN_LABEL = TopMembersFragment.class.getName();
    private static final String GA_TITLE = "Top Members ListView";

    public static final String IMAGE_RESOURCE_ID = "iconResourceID";
    public static final String ITEM_NAME = "itemName";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ca = getActivity();
        Reachability.registerReachability(ca.getApplicationContext());

        latoFontLight = Tools.getFont(ca.getAssets(), "Lato-Light.ttf");
        latoFontBold = Tools.getFont(ca.getAssets(), "Lato-Bold.ttf");

        // Update action bar menu items?
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_topmembers, container, false);

        reloadView = (ImageView) rootView.findViewById(R.id.reload);
        if (Reachability.reachability.isReachable) {
            startTopMembersAdapter();
        } else {
            reloadView.setVisibility(View.VISIBLE);
            reloadView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (Reachability.reachability.isReachable) {
                        reloadView.setVisibility(View.GONE);
                        startTopMembersAdapter();
                    } else {
                        Toast.makeText(ca, R.string.no_internet, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            Toast.makeText(ca, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }

        Tools.initGA(ca, tracker, SCREEN_LABEL, GA_TITLE, "");

        return rootView;
    }

    private void startTopMembersAdapter() {
        reloadView.setVisibility(View.GONE);
        topMembersAdapter = new TopMembersAdapter(ca, R.layout.fragment_topmembers);
        setListAdapter(topMembersAdapter);
        mProgressDialog = ProgressDialog.show(ca, getString(R.string.please_wait) + "...", getString(R.string.retrieving_data) + "...", true);

        getMembers();
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
                    Toast.makeText(ca, String.format(getString(R.string.top_members_no_website, selectedMember.getUsername())), Toast.LENGTH_LONG).show();
                }

                return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (Reachability.reachability.isReachable) {
            Intent explicitIntent = new Intent(ca, MemberCheatListActivity.class);
            explicitIntent.putExtra("memberObj", members[position]);
            startActivity(explicitIntent);
        } else {
            Toast.makeText(ca, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    private void getMembers() {

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    members = Webservice.getMemberTop20();

                    ca.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (members != null && members.length > 0) {
                                topMembersAdapter.notifyDataSetChanged();
                                for (int i = 0; i < members.length; i++) {
                                    topMembersAdapter.add(members[i]);
                                }
                                mProgressDialog.dismiss();
                                topMembersAdapter.notifyDataSetChanged();
                            } else {
                                error(R.string.err_data_not_accessible);
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e("TopMembersActivity:getMembers()", "Webservice.getMemberTop20() == null");
                    error(R.string.err_no_member_data);
                }
            }
        }).start();

    }

    private void error(int msg) {
        new AlertDialog.Builder(ca).setIcon(R.drawable.ic_action_warning).setTitle(getString(R.string.err)).setMessage(msg).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
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
    private class TopMembersAdapter extends ArrayAdapter<Member> {

        public TopMembersAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        private void openWebsite(String url) {
            // TODO member per email informieren, dass jemand seine homepage
            // geoeffnet hat.
            if ((url != null) && (url.length() > 4)) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View cView = convertView;
            LayoutInflater vi = (LayoutInflater) ca.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            cView = vi.inflate(R.layout.topmembers_list_item, null);
            cView.setDrawingCacheEnabled(true);

			/*
             * Dies ist bereis ein Loop der durch die ArrayList geht!
			 */
            try {
                final Member member = members[position];

                ImageView avatarImageView = (ImageView) cView.findViewById(R.id.avatar);
                avatarImageView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        openWebsite(member.getWebsite());
                    }

                });

                Picasso.with(ca.getApplicationContext()).load(Konstanten.WEBDIR_MEMBER_AVATAR + member.getMid()).placeholder(R.drawable.avatar).into(avatarImageView);

                TextView tvNumeration = (TextView) cView.findViewById(R.id.numeration);
                tvNumeration.setText(String.valueOf(position + 1));
                tvNumeration.setTypeface(latoFontBold);

                TextView tvMemberName = (TextView) cView.findViewById(R.id.membername);
                tvMemberName.setText(member.getUsername().toUpperCase());
                tvMemberName.setTypeface(latoFontBold);

                TextView tvCheatCount = (TextView) cView.findViewById(R.id.cheatcount);
                tvCheatCount.setText(getString(R.string.top_members_cheats_count) + ": " + String.valueOf(member.getCheatSubmissionCount()));
                tvCheatCount.setTypeface(latoFontLight);

                TextView tvHiMessage = (TextView) cView.findViewById(R.id.hi_message);
                if (member.getGreeting().length() > 1) {
                    tvHiMessage.setText("\"" + member.getGreeting().replaceAll("\\\\", "").trim() + "\"");
                } else {
                    tvHiMessage.setVisibility(View.GONE);
                }
                tvHiMessage.setTypeface(latoFontLight);

                TextView tvWebsite = (TextView) cView.findViewById(R.id.website);
                if (member.getWebsite().length() > 1) {
                    tvWebsite.setText(member.getWebsite());
                } else {
                    tvWebsite.setVisibility(View.GONE);
                }
                tvWebsite.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        openWebsite(member.getWebsite());
                    }
                });
                tvWebsite.setTypeface(latoFontLight);
                tvWebsite.setDrawingCacheEnabled(false);
            } catch (Exception e) {
                Log.e("TopMembers.getView ERROR:", "on position: " + position + ": " + e.getMessage());
                error(R.string.err_no_member_data);
            }
            return cView;
        }

    }

}
