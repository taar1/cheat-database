package com.cheatdatabase.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Typeface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.cheatdatabase.MainActivity;
import com.cheatdatabase.R;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.splunk.mint.Mint;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.activity_submit_cheat_game_selection)
public class SubmitCheatFragment extends Fragment {

    private Activity ca;
    private Typeface latoFontLight;
    private Typeface latoFontBold;

    @ViewById(R.id.title)
    TextView mTitle;

    @ViewById(R.id.subtitle)
    TextView mSubtitle;

    @ViewById(R.id.search_button)
    Button mSearchButton;

    @FragmentArg(MainActivity.DRAWER_ITEM_ID)
    int mDrawerId;

    @FragmentArg(MainActivity.DRAWER_ITEM_NAME)
    String mDrawerName;

    @AfterViews
    public void onCreateView() {
        ca = getActivity();

        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(ca);
        }
        Mint.initAndStartSession(ca.getApplicationContext(), Konstanten.SPLUNK_MINT_API_KEY);

        latoFontLight = Tools.getFont(ca.getAssets(), Konstanten.FONT_LIGHT);
        latoFontBold = Tools.getFont(ca.getAssets(), Konstanten.FONT_BOLD);

        mTitle.setTypeface(latoFontBold);
        mSubtitle.setTypeface(latoFontLight);
        mSearchButton.setTypeface(latoFontBold);
        mSearchButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ca.onSearchRequested();
            }

        });
    }


    @Override
    public void onPause() {
        Reachability.unregister(ca);
        super.onPause();
    }


}