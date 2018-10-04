package com.cheatdatabase.fragments;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spanned;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.MainActivity;
import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.WelcomeMessage;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_welcome_view)
public class NewsFragment extends Fragment {

    @ViewById(R.id.progress_bar)
    ProgressBar progressBar;

    @ViewById(R.id.reload)
    ImageView reloadView;

    @ViewById(R.id.text_welcome_title)
    TextView welcomeTitle;

    @ViewById(R.id.text_created)
    TextView createdTitle;

    @ViewById(R.id.text_welcome_text)
    TextView welcomeText;

    @FragmentArg(MainActivity.DRAWER_ITEM_ID)
    int mDrawerId;

    @FragmentArg(MainActivity.DRAWER_ITEM_NAME)
    String mDrawerName;

    private Typeface latoFontBold;
    private Typeface latoFontLight;
    private Activity parentActivity;

    public NewsFragment() {
    }

    @AfterViews
    public void onCreateView() {
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

    @Background
    void getWelcomeMessage() {
        if (Reachability.reachability.isReachable) {
            updateUI(Webservice.getWelcomeMessage());
        } else {
            updateUI(null);
        }
    }

    @UiThread
    void updateUI(WelcomeMessage welcomeMessage) {
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
    }

}