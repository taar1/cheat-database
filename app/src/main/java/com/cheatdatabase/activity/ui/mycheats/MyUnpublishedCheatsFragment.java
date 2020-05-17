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
import com.cheatdatabase.data.model.UnpublishedCheat;
import com.cheatdatabase.listeners.MyUnpublishedCheatsListItemSelectedListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * This is the MODEL of MVVM.
 */
public class MyUnpublishedCheatsFragment extends Fragment implements MyUnpublishedCheatsListItemSelectedListener {
    private static final String TAG = "MyUnpublishedCheatsFt";

    private MyUnpublishedCheatsViewModel myUnpublishedCheatsViewModel;
    private MyUnpublishedCheatsListViewAdapter myUnpublishedCheatsListViewAdapter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    public static MyUnpublishedCheatsFragment newInstance() {
        return new MyUnpublishedCheatsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.unpublished_cheats_fragment, container, false);
        ButterKnife.bind(this, view);

//         MyUnpublishedCheatsFragment binding =  DataBindingUtil.setContentView(this, R.layout.unpublished_cheats_fragment);


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        myUnpublishedCheatsViewModel = new ViewModelProvider(this).get(MyUnpublishedCheatsViewModel.class);
        myUnpublishedCheatsViewModel.init();


        setupRecyclerView();

        myUnpublishedCheatsViewModel.getMyUnpublishedCheatsRepository().observe(getActivity(), new Observer<List<UnpublishedCheat>>() {
            @Override
            public void onChanged(List<UnpublishedCheat> unpublishedCheats) {
                Toast.makeText(getContext(), "XXXXX Unpublished Cheats onChanged", Toast.LENGTH_LONG).show();

                myUnpublishedCheatsListViewAdapter.setUnpublishedCheats(unpublishedCheats);
                myUnpublishedCheatsListViewAdapter.notifyDataSetChanged();
            }
        });

    }

    private void setupRecyclerView() {
        if (myUnpublishedCheatsListViewAdapter == null) {
            myUnpublishedCheatsListViewAdapter = new MyUnpublishedCheatsListViewAdapter(this, getActivity());
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(myUnpublishedCheatsListViewAdapter);
        } else {
            myUnpublishedCheatsListViewAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onCheatClicked(UnpublishedCheat cheat) {
        Log.d(TAG, "onCheatClicked: ");
    }

    @Override
    public void onRejectReasonButtonClicked(UnpublishedCheat cheat) {
        Log.d(TAG, "onRejectReasonButtonClicked: ");

    }

    @Override
    public void onDeleteButtonClicked(UnpublishedCheat cheat) {
        Log.d(TAG, "onDeleteButtonClicked: ");
    }
}
