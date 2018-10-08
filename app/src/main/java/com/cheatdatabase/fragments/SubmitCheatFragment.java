package com.cheatdatabase.fragments;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
    private Typeface latoFontLight;
    private Typeface latoFontBold;

    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.subtitle)
    TextView mSubtitle;
    @BindView(R.id.search_button)
    Button mSearchButton;

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

        latoFontLight = Tools.getFont(parentActivity.getAssets(), Konstanten.FONT_LIGHT);
        latoFontBold = Tools.getFont(parentActivity.getAssets(), Konstanten.FONT_BOLD);

        mTitle.setTypeface(latoFontBold);
        mSubtitle.setTypeface(latoFontLight);
        mSearchButton.setTypeface(latoFontBold);
        mSearchButton.setOnClickListener(v -> parentActivity.onSearchRequested());

        return view;
    }

    @Override
    public void onPause() {
        Reachability.unregister(parentActivity);
        super.onPause();
    }


}