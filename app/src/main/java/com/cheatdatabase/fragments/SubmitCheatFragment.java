package com.cheatdatabase.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cheatdatabase.R;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.splunk.mint.Mint;

public class SubmitCheatFragment extends Fragment {

    private Activity ca;
    private Typeface latoFontLight;
    private Typeface latoFontBold;
    private TextView mTitle;
    private TextView mSubtitle;
    private Button mSearchButton;

    public static final String IMAGE_RESOURCE_ID = "iconResourceID";
    public static final String ITEM_NAME = "itemName";

    public SubmitCheatFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ca = getActivity();

        init();

        // Update action bar menu items?
//        setHasOptionsMenu(true);
    }

    private void init() {
        Reachability.registerReachability(ca.getApplicationContext());
        Mint.initAndStartSession(ca.getApplicationContext(), Konstanten.SPLUNK_MINT_API_KEY);

        latoFontLight = Tools.getFont(ca.getAssets(), Konstanten.FONT_LIGHT);
        latoFontBold = Tools.getFont(ca.getAssets(), Konstanten.FONT_BOLD);

//        ca.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onPause() {
        Reachability.unregister(ca.getApplicationContext());
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_submit_cheat_game_selection, container, false);

        mTitle = (TextView) rootView.findViewById(R.id.title);
        mTitle.setTypeface(latoFontBold);
        mSubtitle = (TextView) rootView.findViewById(R.id.subtitle);
        mSubtitle.setTypeface(latoFontLight);

        mSearchButton = (Button) rootView.findViewById(R.id.search_button);
        mSearchButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ca.onSearchRequested();
            }

        });
        mSearchButton.setTypeface(latoFontBold);

        return rootView;
    }

}