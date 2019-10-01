package com.cheatdatabase.fragments;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cheatdatabase.R;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SubmitCheatFragment extends Fragment {

    private Activity parentActivity;

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.subtitle)
    TextView subtitle;
    @BindView(R.id.search_button)
    Button searchButton;

    public static SubmitCheatFragment newInstance() {
        SubmitCheatFragment submitCheatFragment = new SubmitCheatFragment();
        return submitCheatFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_submit_cheat_game_selection, container, false);
        ButterKnife.bind(this, view);

        parentActivity = getActivity();
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(parentActivity);
        }

        searchButton.setOnClickListener(v -> parentActivity.onSearchRequested());

        return view;
    }

    @Override
    public void onPause() {
        Reachability.unregister(parentActivity);
        super.onPause();
    }


}