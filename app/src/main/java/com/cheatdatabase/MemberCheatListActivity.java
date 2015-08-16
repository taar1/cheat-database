package com.cheatdatabase;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.helpers.ActionBarListActivity;
import com.cheatdatabase.helpers.CheatDatabaseAdapter;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.members.cheatview.MemberCheatViewPageIndicator;
import com.google.gson.Gson;
import com.mopub.mobileads.MoPubView;
import com.splunk.mint.Mint;

/**
 * Shows all cheats of one particular member.
 *
 * @author Dominik
 */
@SuppressLint("NewApi")
public class MemberCheatListActivity extends ActionBarListActivity implements AdapterView.OnItemClickListener {

    private Cheat[] cheats;
    private Cheat selectedCheat;
    private Member memberToDisplayCheatsFrom;

    private ProgressDialog mProgressDialog = null;
    private CheatAdapter memberCheatsAdapter;

    private final int ADD_TO_FAVORITES = 0;

    private Typeface latoFontLight;
    private CheatDatabaseAdapter db;
    private MoPubView mAdView;
    private SharedPreferences settings;
    private Editor editor;
    private ImageView reloadView;

    private Toolbar mToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_cheatlist);

        init();

        registerForContextMenu(getListView());

        reloadView = (ImageView) findViewById(R.id.reload);
        if (Reachability.reachability.isReachable) {
            getCheatList();
        } else {
            reloadView.setVisibility(View.VISIBLE);
            reloadView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (Reachability.reachability.isReachable) {
                        getCheatList();
                    } else {
                        Toast.makeText(MemberCheatListActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }

        getListView().setOnItemClickListener(this);
    }

    private void init() {
        Reachability.registerReachability(this);
        Mint.initAndStartSession(this, Konstanten.SPLUNK_MINT_API_KEY);

        settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();

        latoFontLight = Tools.getFont(getAssets(), Konstanten.FONT_LIGHT);

        memberToDisplayCheatsFrom = (Member) getIntent().getSerializableExtra("memberObj");

        mAdView = Tools.initMoPubAdView(this, mAdView);
        mToolbar = Tools.initToolbarBase(this, mToolbar);
        getSupportActionBar().setTitle(getString(R.string.members_cheats_title, memberToDisplayCheatsFrom.getUsername()));
    }

    @Override
    public void onPause() {
        Reachability.unregister(this);
        super.onPause();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        selectedCheat = cheats[Integer.parseInt(String.valueOf(info.id))];
        menu.setHeaderTitle(R.string.context_menu_title);
        menu.add(0, ADD_TO_FAVORITES, 1, R.string.add_one_favorite);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Search
        // getMenuInflater().inflate(R.menu.search_menu, menu);
        //
        // // Associate searchable configuration with the SearchView
        // SearchManager searchManager = (SearchManager)
        // getSystemService(Context.SEARCH_SERVICE);
        // SearchView searchView = (SearchView)
        // menu.findItem(R.id.search).getActionView();
        // searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ADD_TO_FAVORITES:
                db = new CheatDatabaseAdapter(this);
                db.open();
                int retVal = db.insertFavorite(selectedCheat);

                if (retVal > 0) {
                    Toast.makeText(MemberCheatListActivity.this, R.string.add_favorite_ok, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MemberCheatListActivity.this, R.string.favorite_error, Toast.LENGTH_SHORT).show();
                }
                return true;
        }

        return super.onContextItemSelected(item);
    }

    private void getCheatList() {
        reloadView.setVisibility(View.GONE);
        memberCheatsAdapter = new CheatAdapter(this, R.layout.layout_list);
        setListAdapter(memberCheatsAdapter);
        mProgressDialog = ProgressDialog.show(MemberCheatListActivity.this, getString(R.string.please_wait) + "...", getString(R.string.retrieving_data) + "...", true);

        getCheats();
    }

    private void getCheats() {

        new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    cheats = Webservice.getCheatsByMemberId(memberToDisplayCheatsFrom.getMid());
                    new SaveToLocalStorageTask().execute(cheats);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (cheats != null && cheats.length > 0) {
                                memberCheatsAdapter.notifyDataSetChanged();
                                for (int i = 0; i < cheats.length; i++) {
                                    memberCheatsAdapter.add(cheats[i]);
                                }
                                mProgressDialog.dismiss();
                                memberCheatsAdapter.notifyDataSetChanged();
                            } else {
                                error(R.string.err_data_not_accessible);
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e("MemberCheatList:getCheats()", "Webservice.getCheatList() == null");
                    error(R.string.err_no_member_data);
                }

            }
        }).start();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (Reachability.reachability.isReachable) {

            Intent explicitIntent = new Intent(MemberCheatListActivity.this, MemberCheatViewPageIndicator.class);

            if (cheats.length <= 100) {
                // Delete Walkthrough texts (otherwise runs into a timeout)
                for (int i = 0; i < cheats.length; i++) {
                    if (cheats[i].isWalkthroughFormat()) {
                        cheats[i].setCheatText("");
                    }
                }
                explicitIntent.putExtra("cheatsObj", cheats);
            } else {
                // Save to SharedPreferences if array too big
                editor.putString(Konstanten.PREFERENCES_TEMP_CHEAT_ARRAY_OBJECT_VIEW, new Gson().toJson(cheats));
                editor.apply();
            }

            explicitIntent.putExtra("selectedPage", position);
            startActivity(explicitIntent);
        } else {
            Toast.makeText(MemberCheatListActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    private class SaveToLocalStorageTask extends AsyncTask<Cheat[], Void, Void> {

        @Override
        protected Void doInBackground(Cheat[]... params) {
            editor.putString(Konstanten.PREFERENCES_TEMP_CHEAT_ARRAY_OBJECT_VIEW, new Gson().toJson(params[0]));
            editor.commit();
            return null;
        }
    }

    private void error(int msg) {
        new AlertDialog.Builder(MemberCheatListActivity.this).setIcon(R.drawable.ic_action_warning).setTitle(getString(R.string.err)).setMessage(msg).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        }).create().show();
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    private class CheatAdapter extends ArrayAdapter<Cheat> {

        public CheatAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.member_cheatlist_item, null);
            }

			/*
             * Dies ist bereis ein Loop der durch die ArrayList geht!
			 */
            try {
                Cheat cheat = cheats[position];

                TextView tvGameName = (TextView) v.findViewById(R.id.gamename);
                tvGameName.setText(cheat.getGameName() + " (" + cheat.getSystemName() + ")");

                TextView tvCheatTitle = (TextView) v.findViewById(R.id.cheattitle);
                tvCheatTitle.setTypeface(latoFontLight);
                if (tvCheatTitle != null) {
                    tvCheatTitle.setText(cheat.getCheatTitle());
                }

                if ((!cheat.isScreenshots()) && (cheat.getLanguageId() != Konstanten.GERMAN)) {
                    LinearLayout flagLayout = (LinearLayout) v.findViewById(R.id.flag_layout);
                    flagLayout.setVisibility(View.GONE);
                } else {
                    ImageView screenshotFlag = (ImageView) v.findViewById(R.id.ivMap);
                    if (cheat.isScreenshots()) {
                        screenshotFlag.setImageResource(R.drawable.flag_img);
                    } else {
                        screenshotFlag.setVisibility(View.GONE);
                    }

                    ImageView germanFlag = (ImageView) v.findViewById(R.id.ivFlag);
                    if (cheat.getLanguageId() == Konstanten.GERMAN) {
                        germanFlag.setImageResource(R.drawable.flag_german);
                    } else {
                        germanFlag.setVisibility(View.GONE);
                    }
                }

            } catch (Exception e) {
                Log.e("MemberCheatList.getView ERROR:", e.getMessage());
                error(R.string.err_no_member_data);
            }

            return v;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // http://developer.android.com/design/patterns/navigation.html#up-vs-back
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
//                // Dominik: This is actually not needed because I am saving the
//                // selected Fragment ID in the local storage.
//                Intent upIntent = NavUtils.getParentActivityIntent(this);
////                upIntent.putExtra("fragmentId", MainActivity.DRAWER_TOP_MEMBERS);
////                upIntent.putExtra("mFragmentId", 2); (test only)
//                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
//                    // This activity is NOT part of this app's task, so create a new
//                    // task when navigating up, with a synthesized back stack.
//                    // Add all of this activity's parents to the back stack
//                    TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
//                } else {
//                    // This activity is part of this app's task, so simply
//                    // navigate up to the logical parent activity.
////                    NavUtils.navigateUpTo(this, upIntent);
////                    NavUtils.navigateUpFromSameTask(this);
//
//                    onBackPressed();
//                }

                MainActivity_.intent(this).mFragmentId(MainActivity.DRAWER_TOP_MEMBERS).start();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
