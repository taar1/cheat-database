package com.cheatdatabase;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.adapters.MemberCheatRecycleListViewAdapter;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.events.CheatListRecyclerViewClickEvent;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.members.cheatview.MemberCheatViewPageIndicator;
import com.cheatdatabase.widgets.DividerDecoration;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.google.gson.Gson;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import needle.Needle;

/**
 * Shows all cheats of one particular member.
 *
 * @author Dominik
 */
public class CheatsByMemberListActivity extends AppCompatActivity {

    private final String TAG = CheatsByMemberListActivity.class.getSimpleName();

    @BindView(R.id.my_recycler_view)
    FastScrollRecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.item_list_empty_view)
    TextView mEmptyView;
    @BindView(R.id.banner_container)
    LinearLayout facebookBanner;
    private AdView adView;

    private MemberCheatRecycleListViewAdapter memberCheatRecycleListViewAdapter;

    private Member member;
    private List<Cheat> cheatList;

    private Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_cheat_list);
        ButterKnife.bind(this);

        member = getIntent().getParcelableExtra("member");
        init();

        memberCheatRecycleListViewAdapter = new MemberCheatRecycleListViewAdapter();

        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getCheats());

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new DividerDecoration(this));
        mRecyclerView.getItemAnimator().setRemoveDuration(50);
        mRecyclerView.setHasFixedSize(true);

        if (Reachability.reachability.isReachable) {
            getCheats();
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    private void init() {
        mToolbar = Tools.initToolbarBase(this, mToolbar);

        SharedPreferences settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();

        adView = new AdView(this, Konstanten.FACEBOOK_AUDIENCE_NETWORK_NATIVE_BANNER_ID, AdSize.BANNER_HEIGHT_50);
        facebookBanner.addView(adView);
        adView.loadAd();

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.members_cheats_title, member.getUsername()));

    }

    void getCheats() {
        Needle.onBackgroundThread().execute(() -> {
            cheatList = Webservice.getCheatsByMemberId(member.getMid());

            editor.putString(Konstanten.PREFERENCES_TEMP_CHEAT_ARRAY_OBJECT_VIEW, new Gson().toJson(cheatList));
            editor.commit();

            fillListWithCheats();
        });

    }

    void fillListWithCheats() {
        Needle.onMainThread().execute(() -> {
            try {
                if (cheatList.size() > 0) {
                    memberCheatRecycleListViewAdapter.setCheatList(cheatList);
                    mRecyclerView.setAdapter(memberCheatRecycleListViewAdapter);

                    memberCheatRecycleListViewAdapter.notifyDataSetChanged();
                } else {
                    error(R.string.err_data_not_accessible);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
                error(R.string.err_no_member_data);
            }

            mSwipeRefreshLayout.setRefreshing(false);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        Reachability.unregister(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("member", member);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void error(int msg) {
        new AlertDialog.Builder(CheatsByMemberListActivity.this).setIcon(R.drawable.ic_action_warning).setTitle(getString(R.string.err)).setMessage(msg).setNeutralButton(R.string.ok, (dialog, whichButton) -> finish()).create().show();
    }

    @Subscribe
    public void onEvent(CheatListRecyclerViewClickEvent result) {
        if (result.isSucceeded()) {
            editor.putInt(Konstanten.PREFERENCES_PAGE_SELECTED, result.getPosition());
            editor.commit();

            if (Reachability.reachability.isReachable) {
                // Using local Preferences to pass data for large game objects
                // (instead of intent) such as Pokemon
                Intent intent = new Intent(CheatsByMemberListActivity.this, MemberCheatViewPageIndicator.class);

                if (cheatList.size() <= 100) {
                    // Delete Walkthrough texts (otherwise runs into a timeout)
                    for (int i = 0; i < cheatList.size(); i++) {
                        if (cheatList.get(i).isWalkthroughFormat()) {
                            cheatList.get(i).setCheatText("");
                        }
                    }

                    intent.putParcelableArrayListExtra("cheatList", (ArrayList) cheatList);
                } else {
                    // Save to SharedPreferences if array too big
                    editor.putString(Konstanten.PREFERENCES_TEMP_CHEAT_ARRAY_OBJECT_VIEW, new Gson().toJson(cheatList));
                    editor.apply();
                }

                intent.putExtra("selectedPage", result.getPosition());
                intent.putExtra("layoutResourceId", R.layout.activity_cheatview_pager);
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

}
