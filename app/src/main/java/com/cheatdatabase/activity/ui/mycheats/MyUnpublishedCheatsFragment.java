package com.cheatdatabase.activity.ui.mycheats;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.adapters.TopMembersListViewAdapter;
import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.data.model.UnpublishedCheat;
import com.cheatdatabase.listeners.OnTopMemberListItemSelectedListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MyUnpublishedCheatsFragment extends Fragment implements OnTopMemberListItemSelectedListener {
    private static final String TAG = "MyUnpublishedCheatsFragment";

    private MyUnpublishedCheatsViewModel myUnpublishedCheatsViewModel;
    private TopMembersListViewAdapter topMembersListViewAdapter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    public static MyUnpublishedCheatsFragment newInstance() {
        return new MyUnpublishedCheatsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mvvm_test_fragment, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    // TODO continue here: https://www.youtube.com/watch?v=JLwW5HivZg4
    // TODO continue here: https://www.youtube.com/watch?v=JLwW5HivZg4
    // TODO continue here: https://www.youtube.com/watch?v=JLwW5HivZg4
    // TODO continue here: https://www.youtube.com/watch?v=JLwW5HivZg4

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG, "onActivityCreated: ");
        setupRecyclerView();

        myUnpublishedCheatsViewModel = new ViewModelProvider(this).get(MyUnpublishedCheatsViewModel.class);
        myUnpublishedCheatsViewModel.init();

        myUnpublishedCheatsViewModel.getTopMembersRepository().observe(getActivity(), new Observer<List<UnpublishedCheat>>() {
            @Override
            public void onChanged(List<UnpublishedCheat> unpublishedCheats) {
                Toast.makeText(getContext(), "Top Members onChanged", Toast.LENGTH_LONG).show();

                // TODO FIXME
                // TODO FIXME
                // TODO FIXME
                // TODO FIXME
                topMembersListViewAdapter.setMemberList(unpublishedCheats);
                topMembersListViewAdapter.notifyDataSetChanged();
            }
        });

    }


    private void setupRecyclerView() {
        if (topMembersListViewAdapter == null) {
            topMembersListViewAdapter = new TopMembersListViewAdapter(this, getActivity());
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(topMembersListViewAdapter);
        } else {
            topMembersListViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onMemberClicked(Member member) {
        Log.d(TAG, "onMemberClicked: ");
    }

    @Override
    public void onWebsiteClicked(Member member) {
        Log.d(TAG, "onWebsiteClicked: ");
    }
}
