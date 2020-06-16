package com.cheatdatabase.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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

import com.cheatdatabase.CheatDatabaseApplication;
import com.cheatdatabase.R;
import com.cheatdatabase.adapters.MemberCheatRecycleListViewAdapter;
import com.cheatdatabase.cheatdetailview.MemberCheatViewPageIndicator;
import com.cheatdatabase.data.model.Cheat;
import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.listeners.OnCheatListItemSelectedListener;
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
        toolbar = Tools.initToolbarBase(this, toolbar);

        emptyView.setText(getString(R.string.no_member_cheats, member.getUsername()));
        emptyView.setVisibility(View.GONE);

        SharedPreferences settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();

        adView = new AdView(this, Konstanten.FACEBOOK_AUDIENCE_NETWORK_NATIVE_BANNER_ID, AdSize.BANNER_HEIGHT_50);
        facebookBanner.addView(adView);
        adView.loadAd();

        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.members_cheats_title, member.getUsername()));
    }

    void getCheats() {
        progressBar.setVisibility(View.VISIBLE);

        Call<List<Cheat>> call = apiService.getCheatsByMemberId(member.getMid());
        call.enqueue(new Callback<List<Cheat>>() {
            @Override
            public void onResponse(Call<List<Cheat>> games, Response<List<Cheat>> response) {

                // TODO FIXME wenn der user keine cheats hat gibts hier glaube ich ein onFailure()... dem nachgehen...
                // TODO FIXME wenn der user keine cheats hat gibts hier glaube ich ein onFailure()... dem nachgehen...
                // TODO FIXME wenn der user keine cheats hat gibts hier glaube ich ein onFailure()... dem nachgehen...
                // TODO FIXME wenn der user keine cheats hat gibts hier glaube ich ein onFailure()... dem nachgehen...
                // TODO FIXME wenn der user keine cheats hat gibts hier glaube ich ein onFailure()... dem nachgehen...

                // TODO FIXME schauen was REST PHP zurück gibt wenn es keine member cheats gibt. müsste korrekterweise ein empty JSON liefern und keinen damit es kein error gibt....
                // TODO FIXME schauen was REST PHP zurück gibt wenn es keine member cheats gibt. müsste korrekterweise ein empty JSON liefern und keinen damit es kein error gibt....
                // TODO FIXME schauen was REST PHP zurück gibt wenn es keine member cheats gibt. müsste korrekterweise ein empty JSON liefern und keinen damit es kein error gibt....
                // TODO FIXME schauen was REST PHP zurück gibt wenn es keine member cheats gibt. müsste korrekterweise ein empty JSON liefern und keinen damit es kein error gibt....
                // TODO FIXME schauen was REST PHP zurück gibt wenn es keine member cheats gibt. müsste korrekterweise ein empty JSON liefern und keinen damit es kein error gibt....

                Log.d(TAG, "XXXXX onResponse SUCCESS");

                if (response.isSuccessful()) {
                    Log.d(TAG, "XXXXX onResponse SUCCESS 1");
                    cheatList = response.body();

                    editor.putString(Konstanten.PREFERENCES_TEMP_CHEAT_ARRAY_OBJECT_VIEW, new Gson().toJson(cheatList));
                    editor.apply();

                    updateUI();
                    Log.d(TAG, "XXXXX onResponse SUCCESS 2");
                } else {
                    Log.d(TAG, "XXXXX onResponse SUCCESS 3");
                    emptyView.setVisibility(View.GONE);
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<List<Cheat>> call, Throwable t) {
                Log.e(TAG, "XXXXX getting member cheats has failed: " + t.getLocalizedMessage());
                emptyView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

                Tools.showSnackbar(outerLayout, getString(R.string.error_loading_cheats));
            }
        });
    }

    private void updateUI() {
        Log.d(TAG, "XXXXX updateUI 1: " + cheatList.size());

        if ((cheatList != null) && (cheatList.size() > 0)) {
            Log.d(TAG, "XXXXX updateUI 2");
            memberCheatRecycleListViewAdapter.setCheatList(cheatList);
            recyclerView.setAdapter(memberCheatRecycleListViewAdapter);

            memberCheatRecycleListViewAdapter.notifyDataSetChanged();

            emptyView.setVisibility(View.GONE);
        } else {
            Log.d(TAG, "XXXXX updateUI 3");
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
