package com.cheatdatabase.favorites;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.CheatDetailTabletFragment;
import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.ForumPost;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.helpers.Helper;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.google.gson.Gson;

import java.util.Calendar;

public class FavoritesCheatForumFragment extends Fragment implements OnClickListener {

    private LinearLayout llForumMain;
    private TextView tvEmpty;
    private TextView textCheatTitle;
    private Cheat cheatObj;
    private Button postButton;
    private EditText editText;
    private ScrollView sv;

    private ImageButton btnReport;
    private ImageButton btnShare;
    private ImageButton btnRateCheat;
    private ImageButton btnMetaInfo;
    private ImageButton btnForum;
    private ImageButton btnViewCheat;

    private ForumPost[] forumThread;

    // Soft Keyboard
    private InputMethodManager imm;

    private View rootView;

    private SharedPreferences settings;
    private Member member;

    private Typeface latoFontBold;
    private Typeface latoFontLight;

    private FavoriteCheatListActivity ca;
    private FavoritesDetailsFragment favoritesDetailsFragment;
    private FavoritesCheatMetaFragment favoritesCheatMetaFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ca = (FavoriteCheatListActivity) getActivity();
        Reachability.registerReachability(ca.getApplicationContext());

        latoFontLight = Tools.getFont(getActivity().getAssets(), Konstanten.FONT_LIGHT);
        latoFontBold = Tools.getFont(getActivity().getAssets(), Konstanten.FONT_BOLD);

        Bundle element = this.getArguments();
        cheatObj = (Cheat) element.getSerializable("cheatObj");
        favoritesDetailsFragment = new Gson().fromJson(element.getString("favoritesDetailsFragment"), FavoritesDetailsFragment.class);
        favoritesCheatMetaFragment = new Gson().fromJson(element.getString("favoritesCheatMetaFragment"), FavoritesCheatMetaFragment.class);

        // Soft Keyboard
        imm = (InputMethodManager) ca.getSystemService(Context.INPUT_METHOD_SERVICE);

        settings = ca.getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_cheat_forum, container, false);

        btnViewCheat = (ImageButton) rootView.findViewById(R.id.btn_view_cheat);
        btnViewCheat.setOnClickListener(this);
        btnMetaInfo = (ImageButton) rootView.findViewById(R.id.btn_meta_info);
        btnMetaInfo.setOnClickListener(this);
        btnForum = (ImageButton) rootView.findViewById(R.id.btn_forum);
        btnReport = (ImageButton) rootView.findViewById(R.id.btn_report);
        btnReport.setVisibility(View.GONE);
        btnShare = (ImageButton) rootView.findViewById(R.id.btn_share);
        btnShare.setOnClickListener(this);
        btnRateCheat = (ImageButton) rootView.findViewById(R.id.btn_rate_cheat);
        btnRateCheat.setOnClickListener(this);
        if (cheatObj.getMemberRating() > 0) {
            highlightRatingIcon(true);
        }

        textCheatTitle = (TextView) rootView.findViewById(R.id.text_cheat_title);
        textCheatTitle.setVisibility(View.VISIBLE);
        textCheatTitle.setTypeface(latoFontBold);
        textCheatTitle.setText(cheatObj.getCheatTitle());

        llForumMain = (LinearLayout) rootView.findViewById(R.id.llForumMain);

        tvEmpty = (TextView) rootView.findViewById(R.id.tvEmpty);
        tvEmpty.setTypeface(latoFontLight);

        editText = (EditText) rootView.findViewById(R.id.etEnterForumPost);
        postButton = (Button) rootView.findViewById(R.id.btnSubmitPost);
        sv = (ScrollView) rootView.findViewById(R.id.sv);

        if (Reachability.reachability.isReachable) {
            new GetForumPostsTask().execute();
        } else {
            // TODO ausgabe wegen connection, reload button anzeigen
        }

        postButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (editText.getText().toString().trim().length() > 0) {
                    submitPost();
                } else {
                    Toast.makeText(getActivity(), R.string.fill_everything, Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }

    public void highlightRatingIcon(boolean highlight) {
        try {
            if (highlight) {
                btnRateCheat.setImageResource(R.drawable.ic_action_star);
            } else {
                btnRateCheat.setImageResource(R.drawable.ic_action_not_important);
            }
        } catch (NullPointerException e) {
        }
    }

    /**
     * Tabelle mit den geparsten ForumPosts füllen.
     */
    private void fillViewWithThread() {

        llForumMain.removeAllViews();
        if (forumThread.length > 0) {
            tvEmpty.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.VISIBLE);
        }

        for (int i = 0; i < forumThread.length; i++) {
            ForumPost tempFP = forumThread[i];

            LinearLayout tl = createForumPosts(tempFP);
            llForumMain.addView(tl, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }

    }

    /**
     * Adds a post to the forum and scrolls all the way down to the latest post.
     */
    private void submitPost() {

        if (member == null) {
            Toast.makeText(getActivity(), R.string.error_login_required, Toast.LENGTH_LONG).show();
        } else {
            // get the current date
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            int mDay = c.get(Calendar.DAY_OF_MONTH);
            int mHour = c.get(Calendar.HOUR_OF_DAY);
            String leadingZeroHour = String.valueOf(mHour);
            if (leadingZeroHour.length() == 1) {
                leadingZeroHour = "0" + mHour;
            }
            int mMin = c.get(Calendar.MINUTE);
            String leadingZeroMin = String.valueOf(mMin);
            if (leadingZeroMin.length() == 1) {
                leadingZeroMin = "0" + mMin;
            }
            // int mdate = c.get(Calendar.DATE);

            String[] months = getResources().getStringArray(R.array.months);

            ForumPost tempFP = new ForumPost();
            tempFP.setText(editText.getText().toString().trim());
            tempFP.setUsername(member.getUsername());
            tempFP.setName(member.getUsername());
            tempFP.setEmail(member.getEmail());
            tempFP.setCreated(months[mMonth] + " " + mDay + ", " + mYear + " / " + leadingZeroHour + ":" + leadingZeroMin);

            if (Reachability.reachability.isReachable) {
                llForumMain.addView(createForumPosts(tempFP), new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

                // Scrolling down to the latest post
                sv.post(new Runnable() {
                    @Override
                    public void run() {
                        sv.fullScroll(View.FOCUS_DOWN);
                    }

                });
                editText.setEnabled(false);
                postButton.setEnabled(false);
                new CountDownTimer(5000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }

                    @Override
                    public void onFinish() {
                        postButton.setEnabled(true);
                        editText.setEnabled(true);
                    }
                }.start();

                new ForumPostTask().execute(tempFP);
            } else {
                Toast.makeText(getActivity(), R.string.no_internet, Toast.LENGTH_SHORT).show();
            }

        }

    }

    /**
     * Fills the table of the forum
     *
     * @param tempFP
     * @return
     */
    private LinearLayout createForumPosts(ForumPost tempFP) {
        LinearLayout tl = new LinearLayout(getActivity());
        tl.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        tl.setGravity(Gravity.TOP);
        tl.setBackgroundColor(Color.BLACK);
        tl.setOrientation(LinearLayout.VERTICAL);

        TextView tvFirstThCol = new TextView(getActivity());
        TextView tvSecondThCol = new TextView(getActivity());

        // Headerinfo zu Forumpost
        LinearLayout rowForumPostHeader = new LinearLayout(getActivity());
        rowForumPostHeader.setBackgroundColor(Color.DKGRAY);
        rowForumPostHeader.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        rowForumPostHeader.setGravity(Gravity.CENTER_HORIZONTAL);
        rowForumPostHeader.setPadding(4, 1, 8, 1);
        rowForumPostHeader.setOrientation(LinearLayout.HORIZONTAL);

        if (!tempFP.getUsername().equalsIgnoreCase("null")) {
            tvFirstThCol.setText(tempFP.getUsername().trim());
        } else {
            tvFirstThCol.setText(tempFP.getName().trim());
        }
        tvFirstThCol.setTextColor(Color.WHITE);
        tvFirstThCol.setGravity(Gravity.START);
        tvFirstThCol.setSingleLine(true);
        tvFirstThCol.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        tvSecondThCol.setText(tempFP.getCreated());
        tvSecondThCol.setTextColor(Color.LTGRAY);
        tvSecondThCol.setGravity(Gravity.END);
        tvSecondThCol.setSingleLine(true);
        tvSecondThCol.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        rowForumPostHeader.addView(tvFirstThCol);
        rowForumPostHeader.addView(tvSecondThCol);

        tl.addView(rowForumPostHeader);

        // Forum-Post
        TextView tvForumPost = new TextView(getActivity());
        tvForumPost.setText(tempFP.getText());
        tvForumPost.setBackgroundColor(Color.BLACK);
        tvForumPost.setTextColor(Color.WHITE);
        tvForumPost.setPadding(10, 10, 10, 40);
        tvForumPost.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        tl.addView(tvForumPost);

        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        return tl;
    }

    @Override
    public void onResume() {
        super.onResume();
        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
    }

    @Override
    public void onClick(View v) {
        Bundle arguments = new Bundle();
        arguments.putInt(CheatDetailTabletFragment.ARG_ITEM_ID, 1);
        arguments.putSerializable("cheatObj", cheatObj);
        arguments.putString("favoritesDetailsFragment", new Gson().toJson(favoritesDetailsFragment));
        arguments.putString("favoritesCheatForumFragment", new Gson().toJson(FavoritesCheatForumFragment.class));
        arguments.putString("favoritesCheatMetaFragment", new Gson().toJson(favoritesCheatMetaFragment));

        if (v == btnViewCheat) {
            Log.d("onClick", "btnViewCheat");
            favoritesDetailsFragment.setArguments(arguments);
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.favorite_detail_container, favoritesDetailsFragment).commit();
        } else if (v == btnMetaInfo) {
            Log.d("onClick", "btnMetaInfo");
            favoritesCheatMetaFragment.setArguments(arguments);
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.favorite_detail_container, favoritesCheatMetaFragment).commit();
        } else if (v == btnForum) {
            Log.d("onClick", "btnForum");
        } else if (v == btnShare) {
            Log.d("onClick", "btnShare");
            Helper.shareCheat(cheatObj, ca);
        } else if (v == btnRateCheat) {
            Log.d("onClick", "btnRateCheat");
            ca.showRatingDialog();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    private class ForumPostTask extends AsyncTask<ForumPost, Void, Void> {

        @Override
        protected Void doInBackground(ForumPost... params) {

            ForumPost fp = params[0];

            try {
                Webservice.insertForum(cheatObj.getCheatId(), member.getMid(), member.getPassword(), fp.getText());
            } catch (Exception e) {
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            editText.setText("");
            tvEmpty.setVisibility(View.GONE);
            Toast.makeText(getActivity(), R.string.forum_submit_ok, Toast.LENGTH_LONG).show();
        }

    }

    private class GetForumPostsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            try {
                if (Reachability.reachability.isReachable) {
                    forumThread = Webservice.getForum(cheatObj.getCheatId());
                }
            } catch (Exception e) {
                Log.e("CheatForum: BackgroundTask", e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            fillViewWithThread();
        }

    }

}