package com.cheatdatabase.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cheatdatabase.CheatDatabaseApplication;
import com.cheatdatabase.R;
import com.cheatdatabase.adapters.MemberCheatRecycleListViewAdapter;
import com.cheatdatabase.cheat_detail_view.MemberCheatViewPageIndicator;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.listeners.OnCheatListItemSelectedListener;
import com.cheatdatabase.model.Cheat;
import com.cheatdatabase.model.Member;
import com.cheatdatabase.rest.RestApi;
import com.cheatdatabase.widgets.DividerDecoration;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.google.gson.Gson;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Shows all cheats of one particular member.
 */
public class CheatsByMemberListActivity extends AppCompatActivity implements OnCheatListItemSelectedListener {

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
    @BindView(R.id.outer_layout)
    LinearLayout outerLayout;

    AdView adView;

    private MemberCheatRecycleListViewAdapter memberCheatRecycleListViewAdapter;

    private Member member;
    private List<Cheat> cheatList;

    private Editor editor;

    @Inject
    Retrofit retrofit;

    private RestApi apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_cheat_list);
        ButterKnife.bind(this);

        // Dagger start
        ((CheatDatabaseApplication) getApplication()).getNetworkComponent().inject(this);
        apiService = retrofit.create(RestApi.class);
        // Dagger end

        member = getIntent().getParcelableExtra("member");
        if (member != null) {
            init();

            memberCheatRecycleListViewAdapter = new MemberCheatRecycleListViewAdapter(this);

            mSwipeRefreshLayout.setRefreshing(true);
            mSwipeRefreshLayout.setOnRefreshListener(() -> getCheats());

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
            mRecyclerView.addItemDecoration(new DividerDecoration(this));
            mRecyclerView.getItemAnimator().setRemoveDuration(50);
            mRecyclerView.setHasFixedSize(true);

            if (Reachability.reachability.isReachable) {
                getCheats();
            } else {
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
            }
        } else {
            finish();
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
        Call<List<Cheat>> call = apiService.getCheatsByMemberId(member.getMid());
        call.enqueue(new Callback<List<Cheat>>() {
            @Override
            public void onResponse(Call<List<Cheat>> games, Response<List<Cheat>> response) {
                if (response.isSuccessful()) {
                    cheatList = response.body();

                    // TODO FIXME hier ist cheat ID immer "0"... kommt evtl. nicht vom rest backend korrekt zurück?
                    // TODO FIXME hier ist cheat ID immer "0"... kommt evtl. nicht vom rest backend korrekt zurück?
                    // TODO FIXME hier ist cheat ID immer "0"... kommt evtl. nicht vom rest backend korrekt zurück?
                    // TODO FIXME hier ist cheat ID immer "0"... kommt evtl. nicht vom rest backend korrekt zurück?
                    // TODO FIXME hier ist cheat ID immer "0"... kommt evtl. nicht vom rest backend korrekt zurück?
                    // TODO FIXME hier ist cheat ID immer "0"... kommt evtl. nicht vom rest backend korrekt zurück?
                    // TODO FIXME hier ist cheat ID immer "0"... kommt evtl. nicht vom rest backend korrekt zurück?
                    // TODO FIXME hier ist cheat ID immer "0"... kommt evtl. nicht vom rest backend korrekt zurück?


                    editor.putString(Konstanten.PREFERENCES_TEMP_CHEAT_ARRAY_OBJECT_VIEW, new Gson().toJson(cheatList));
                    editor.apply();

                    updateUI();
                }
            }

            @Override
            public void onFailure(Call<List<Cheat>> call, Throwable t) {
                Log.e(TAG, "getting member cheats has failed: " + t.getLocalizedMessage());
                Tools.showSnackbar(outerLayout, getString(R.string.no_internet));
            }
        });
    }

    private void updateUI() {
        if ((cheatList != null) && (cheatList.size() > 0)) {
            memberCheatRecycleListViewAdapter.setCheatList(cheatList);
            mRecyclerView.setAdapter(memberCheatRecycleListViewAdapter);

            memberCheatRecycleListViewAdapter.notifyDataSetChanged();
        } else {
            Tools.showSnackbar(outerLayout, getString(R.string.err_data_not_accessible));
        }

        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(this);
        }
    }

    @Override
    protected void onStop() {
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

    @Override
    public void onCheatListItemSelected(Cheat cheat, int position) {
        editor.putInt(Konstanten.PREFERENCES_PAGE_SELECTED, position);
        editor.apply();

        // Using local Preferences to pass data (PREFERENCES_TEMP_CHEAT_ARRAY_OBJECT_VIEW) for game objects (instead of intent) otherwise runs into TransactionTooLargeException when passing the array to the next activity.
        if (Reachability.reachability.isReachable) {
            Intent intent = new Intent(CheatsByMemberListActivity.this, MemberCheatViewPageIndicator.class);
            intent.putExtra("selectedPage", position);
            intent.putExtra("layoutResourceId", R.layout.activity_cheatview_pager);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }
}
