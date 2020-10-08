package com.cheatdatabase.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.adapters.MemberCheatRecycleListViewAdapter;
import com.cheatdatabase.cheatdetailview.MemberCheatViewPageIndicator;
import com.cheatdatabase.data.model.Cheat;
import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.listeners.OnMyCheatListItemSelectedListener;
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
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Shows all cheats of one particular member.
 */
@AndroidEntryPoint
public class CheatsByMemberListActivity extends AppCompatActivity implements OnMyCheatListItemSelectedListener {

    private final String TAG = CheatsByMemberListActivity.class.getSimpleName();

    @Inject
    Tools tools;
    @Inject
    RestApi restApi;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.my_recycler_view)
    FastScrollRecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.item_list_empty_view)
    TextView emptyView;
    @BindView(R.id.banner_container)
    LinearLayout facebookBanner;
    @BindView(R.id.outer_layout)
    ConstraintLayout outerLayout;

    AdView adView;

    private MemberCheatRecycleListViewAdapter memberCheatRecycleListViewAdapter;

    private Member authorMember;
    private List<Cheat> cheatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_cheat_list);
        ButterKnife.bind(this);

        // The Member of whom to display the cheats.
        authorMember = getIntent().getParcelableExtra("member");
        if (authorMember != null) {
            init();

            memberCheatRecycleListViewAdapter = new MemberCheatRecycleListViewAdapter(this);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
            recyclerView.addItemDecoration(new DividerDecoration(this));
            recyclerView.getItemAnimator().setRemoveDuration(50);
            recyclerView.setHasFixedSize(true);

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
        toolbar = tools.initToolbarBase(this, toolbar);

        emptyView.setText(getString(R.string.no_member_cheats, authorMember.getUsername()));
        emptyView.setVisibility(View.GONE);


        adView = new AdView(this, Konstanten.FACEBOOK_AUDIENCE_NETWORK_NATIVE_BANNER_ID, AdSize.BANNER_HEIGHT_50);
        facebookBanner.addView(adView);
        adView.loadAd();

        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.members_cheats_title, authorMember.getUsername()));
    }

    void getCheats() {
        progressBar.setVisibility(View.VISIBLE);

        Call<List<Cheat>> call = restApi.getCheatsByMemberId(authorMember.getMid());
        call.enqueue(new Callback<List<Cheat>>() {
            @Override
            public void onResponse(Call<List<Cheat>> games, Response<List<Cheat>> response) {
                if (response.isSuccessful()) {
                    cheatList = response.body();

                    tools.putString(Konstanten.PREFERENCES_TEMP_CHEAT_ARRAY_OBJECT_VIEW, new Gson().toJson(cheatList));

                    updateUI();
                } else {
                    emptyView.setVisibility(View.GONE);
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<List<Cheat>> call, Throwable t) {
                Log.e(TAG, "getting member cheats has failed: " + t.getLocalizedMessage());
                emptyView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

                tools.showSnackbar(outerLayout, getString(R.string.error_loading_cheats));
            }
        });
    }

    private void updateUI() {
        if ((cheatList != null) && (cheatList.size() > 0)) {
            memberCheatRecycleListViewAdapter.setCheatList(cheatList);
            memberCheatRecycleListViewAdapter.setLoggedInMember(tools.getMember());
            recyclerView.setAdapter(memberCheatRecycleListViewAdapter);

            memberCheatRecycleListViewAdapter.notifyDataSetChanged();

            emptyView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
        }
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
        outState.putParcelable("member", authorMember);
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
        tools.putInt(Konstanten.PREFERENCES_PAGE_SELECTED, position);

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

    @Override
    public void onCheatListItemEditSelected(Cheat cheat, int position) {
        // TODO create a edit cheat MVVM construct here....
        Log.d(TAG, "XXXXX DDDDD onCheatListItemEditSelected: ");
    }
}
