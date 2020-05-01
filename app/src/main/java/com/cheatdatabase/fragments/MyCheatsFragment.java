package com.cheatdatabase.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.cheatdatabase.R;
import com.cheatdatabase.activity.MainActivity;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.model.Member;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyCheatsFragment extends Fragment {

    private static final String TAG = "MyCheatsFragment";

    @BindView(R.id.outer_layout)
    ConstraintLayout outerLayout;
    @BindView(R.id.card_unpublished_cheats)
    CardView unpublishedCheatsCard;
    @BindView(R.id.card_published_cheats)
    CardView publishedCheatsCard;

    private MainActivity mainActivity;

    public static MyCheatsFragment newInstance() {
        return new MyCheatsFragment();
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_cheats_overview, container, false);
        ButterKnife.bind(this, view);


        SharedPreferences settings = mainActivity.getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        Member member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);


        return view;
    }

    @Override
    public void onPause() {
        Reachability.unregister(mainActivity);
        super.onPause();
    }


}