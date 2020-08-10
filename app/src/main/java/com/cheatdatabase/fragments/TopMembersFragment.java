package com.cheatdatabase.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cheatdatabase.R;
import com.cheatdatabase.activity.CheatsByMemberListActivity;
import com.cheatdatabase.adapters.TopMembersListViewAdapter;
import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.listeners.OnTopMemberListItemSelectedListener;
import com.cheatdatabase.rest.RestApi;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.hilt.android.AndroidEntryPoint;
import dagger.hilt.android.qualifiers.ActivityContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Show Top 20 helping memberList in a list.
 *
 * @author Dominik Erbsland
 */
@AndroidEntryPoint
public class TopMembersFragment extends Fragment implements OnTopMemberListItemSelectedListener {
    private static final String TAG = TopMembersFragment.class.getSimpleName();
    private final int VISIT_WEBSITE = 0;
    private Context context;

    private List<Member> memberList;
    private Member selectedMember;

    private RecyclerView.LayoutManager layoutManager;
    private TopMembersListViewAdapter topMembersListViewAdapter;

    @Inject
    RestApi restApi;

    @BindView(R.id.my_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.empty)
    View emptyView;
    @BindView(R.id.empty_label)
    TextView emptyLabel;

    @Inject
    public TopMembersFragment(@ActivityContext Context context) {
        this.context = context;

        memberList = new ArrayList<>();
        topMembersListViewAdapter = new TopMembersListViewAdapter(this, context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topmembers, container, false);
        ButterKnife.bind(this, view);

        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(context);
        }

        setHasOptionsMenu(true);

        mSwipeRefreshLayout.setOnRefreshListener(this::loadMembersInBackground);

        if (Reachability.reachability.isReachable) {
            layoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(topMembersListViewAdapter);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), RecyclerView.VERTICAL);
            recyclerView.addItemDecoration(dividerItemDecoration);
        } else {
            Toast.makeText(context, R.string.no_internet, Toast.LENGTH_LONG).show();
        }

        loadMembersInBackground();
        return view;
    }

    @Override
    public void onPause() {
        Reachability.unregister(context);
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
                    Toast.makeText(context, getString(R.string.top_members_no_website, selectedMember.getUsername()), Toast.LENGTH_LONG).show();
                }

                return true;
        }

        return super.onContextItemSelected(item);
    }

    private void loadMembersInBackground() {
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(true);
        }

        Call<List<Member>> call = restApi.getMemberTop20();
        call.enqueue(new Callback<List<Member>>() {
            @Override
            public void onResponse(Call<List<Member>> members, Response<List<Member>> response) {
                memberList = response.body();
                notifyAdapter();
            }

            @Override
            public void onFailure(Call<List<Member>> call, Throwable e) {
                Log.e(TAG, "loadMembersInBackground onFailure: " + e.getLocalizedMessage());
                handleEmptyViewState();
            }
        });
    }

    private void notifyAdapter() {
        mSwipeRefreshLayout.setRefreshing(true);

        if (memberList != null && memberList.size() > 0) {
            topMembersListViewAdapter.setMemberList(memberList);
            topMembersListViewAdapter.notifyDataSetChanged();
        } else {
            handleEmptyViewState();
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void handleEmptyViewState() {
        if (mSwipeRefreshLayout == null || !isAdded()) {
            return;
        }

        if (topMembersListViewAdapter.getItemCount() == 0) {
            emptyLabel.setText(getString(R.string.err_no_member_data));
            emptyView.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setVisibility(View.GONE);
        } else {
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMemberClicked(Member member) {
        if (Reachability.reachability.isReachable) {
            Intent intent = new Intent(context, CheatsByMemberListActivity.class);
            intent.putExtra("member", member);
            context.startActivity(intent);
        } else {
            Toast.makeText(context, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onWebsiteClicked(Member member) {
        String url = member.getWebsite();
        if ((url != null) && (url.length() > 4)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

}
