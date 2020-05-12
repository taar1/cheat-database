package com.cheatdatabase.activity.ui.mvvmexample.ui.mvvmtest;

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
import com.cheatdatabase.listeners.OnTopMemberListItemSelectedListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MvvmTestFragment extends Fragment implements OnTopMemberListItemSelectedListener {
    private static final String TAG = "MvvmTestFragment";

    private MvvmTestViewModel mvvmTestViewModel;
    private TopMembersListViewAdapter topMembersListViewAdapter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    public static MvvmTestFragment newInstance() {
        return new MvvmTestFragment();
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


        mvvmTestViewModel = new ViewModelProvider(this).get(MvvmTestViewModel.class);
        mvvmTestViewModel.init();


        mvvmTestViewModel.getTopMembersRepository().observe(getActivity(), new Observer<List<Member>>() {
            @Override
            public void onChanged(List<Member> members) {
                Toast.makeText(getContext(), "Top Members onChanged", Toast.LENGTH_LONG).show();

                topMembersListViewAdapter.setMemberList(members);
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
