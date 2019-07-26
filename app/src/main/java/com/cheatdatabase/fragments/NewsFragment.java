package com.cheatdatabase.fragments;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.WelcomeMessage;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;

import butterknife.BindView;
import butterknife.ButterKnife;
import needle.Needle;

public class NewsFragment extends Fragment {

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.reload)
    ImageView reloadView;
    @BindView(R.id.text_welcome_title)
    TextView welcomeTitle;
    @BindView(R.id.text_created)
    TextView createdTitle;
    @BindView(R.id.text_welcome_text)
    TextView welcomeText;

    private Typeface latoFontBold;
    private Typeface latoFontLight;
    private Activity parentActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome_view, container, false);
        ButterKnife.bind(this, view);

        parentActivity = getActivity();
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(parentActivity);
        }

        latoFontBold = Tools.getFont(parentActivity.getAssets(), Konstanten.FONT_BOLD);
        latoFontLight = Tools.getFont(parentActivity.getAssets(), Konstanten.FONT_LIGHT);

        progressBar.setVisibility(View.VISIBLE);
        reloadView.setVisibility(View.INVISIBLE);

        createdTitle.setTypeface(latoFontLight);
        welcomeTitle.setTypeface(latoFontBold);
        welcomeText.setTypeface(latoFontLight);

        reloadView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadView.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                getWelcomeMessage();
            }
        });

        getWelcomeMessage();

        return view;
    }

    @Override
    public void onPause() {
        Reachability.unregister(getActivity());
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
    }

    void getWelcomeMessage() {
        Needle.onBackgroundThread().execute(() -> {
            if (Reachability.reachability.isReachable) {
                updateUI(Webservice.getWelcomeMessage());
            } else {
                updateUI(null);
            }
        });
    }

    void updateUI(WelcomeMessage welcomeMessage) {
        Needle.onMainThread().execute(() -> {
            if (welcomeMessage != null) {
                welcomeTitle.setText(welcomeMessage.getTitle());
                welcomeTitle.setVisibility(View.VISIBLE);

                createdTitle.setText(Tools.convertDateToLocaleDateFormat(welcomeMessage.getCreated(), parentActivity));

                Spanned spanned = Html.fromHtml(welcomeMessage.getWelcomeMessage());
                welcomeText.setText(spanned);
                welcomeText.setVisibility(View.VISIBLE);
                Linkify.addLinks(welcomeText, Linkify.ALL);
                reloadView.setVisibility(View.GONE);
            } else {
                Toast.makeText(parentActivity, R.string.no_internet, Toast.LENGTH_SHORT).show();
                reloadView.setVisibility(View.VISIBLE);
            }
            progressBar.setVisibility(View.GONE);
        });
    }

}