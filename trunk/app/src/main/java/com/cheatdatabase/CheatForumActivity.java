package com.cheatdatabase;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.ShareActionProvider;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.ForumPost;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.dialogs.CheatMetaDialog;
import com.cheatdatabase.dialogs.RateCheatDialog;
import com.cheatdatabase.dialogs.RateCheatDialog.RateCheatDialogListener;
import com.cheatdatabase.dialogs.ReportCheatDialog;
import com.cheatdatabase.dialogs.ReportCheatDialog.ReportCheatDialogListener;
import com.cheatdatabase.helpers.Helper;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.google.analytics.tracking.android.Tracker;
import com.google.gson.Gson;
import com.mopub.mobileads.MoPubView;

import java.util.Calendar;

/**
 * Displaying the forum of one cheat.
 *
 * @author Dominik
 */
@SuppressLint("CommitPrefEdits")
public class CheatForumActivity extends ActionBarActivity implements CheatListFragment.Callbacks, ReportCheatDialogListener, RateCheatDialogListener {

    private LinearLayout llForumMain;

    private TextView tvCheatTitle;
    private TextView tvEmpty;
    private Intent intent;
    private Cheat cheatObj;
    private Button postButton;
    private EditText editText;

    private ScrollView sv;

    private ForumPost[] forumThread;

    private SharedPreferences settings;
    private Editor editor;

    private Member member;

    private ViewGroup adViewContainer;
    private MoPubView mAdView;

    private Typeface latoFontBold;
    private Typeface latoFontLight;

    private Tracker tracker;

    private ImageView reloadView;

    private static final String SCREEN_LABEL = "Cheat Forum Activity";
    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheat_forum);

        intent = getIntent();
        init();

        tvCheatTitle = (TextView) findViewById(R.id.text_cheat_title);
        tvCheatTitle.setText(cheatObj.getCheatTitle() + " (" + getString(R.string.forum) + ")");
        tvCheatTitle.setTypeface(latoFontBold);

        llForumMain = (LinearLayout) findViewById(R.id.llForumMain);

        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        tvEmpty.setTypeface(latoFontLight);

        editText = (EditText) findViewById(R.id.etEnterForumPost);
        postButton = (Button) findViewById(R.id.btnSubmitPost);
        sv = (ScrollView) findViewById(R.id.sv);

        reloadView = (ImageView) findViewById(R.id.reload);
        if (Reachability.reachability.isReachable) {
            reloadView.setVisibility(View.GONE);
            new GetForumBackgroundTask().execute();
        } else {
            reloadView.setVisibility(View.VISIBLE);
            reloadView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (Reachability.reachability.isReachable) {
                        new GetForumBackgroundTask().execute();
                    } else {
                        Toast.makeText(CheatForumActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            Toast.makeText(CheatForumActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }

        postButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (Reachability.reachability.isReachable) {
                    if (editText.getText().toString().trim().length() > 0) {
                        submitPost();
                    } else {
                        Toast.makeText(CheatForumActivity.this, R.string.fill_everything, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CheatForumActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void init() {
        Reachability.registerReachability(this.getApplicationContext());

        Tools.initLocalStorage(this, settings, editor);
        Tools.initMoPubAdView(this, mAdView);
        Tools.initToolbar(this, toolbar);

        latoFontLight = Tools.getFont(getAssets(), "Lato-Light.ttf");
        latoFontBold = Tools.getFont(getAssets(), "Lato-Bold.ttf");

        cheatObj = (Cheat) intent.getSerializableExtra("cheatObj");
        // if (cheatObj.getMemberRating() > 0) {
        // highlightRatingIcon(true);
        // }

        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);

        getSupportActionBar().setTitle(cheatObj.getGameName());
        getSupportActionBar().setSubtitle(cheatObj.getSystemName());

        Tools.initGA(this, tracker, SCREEN_LABEL, cheatObj.getGameName() + " (" + cheatObj.getSystemName() + ")", cheatObj.getCheatTitle());
    }

    /**
     * Fill table with parsed ForumPosts.
     */
    private void fillViewWithThread() {
        reloadView.setVisibility(View.GONE);

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
     * Submits the forum post and scrolls down to the bottom of the list.
     */
    private void submitPost() {
        if (member == null) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_LONG).show();
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

                // Führt die ScrollView bis ganz nach unten zum neusten Post
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
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
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
        LinearLayout tl = new LinearLayout(this);
        tl.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        tl.setGravity(Gravity.TOP);
        tl.setBackgroundColor(Color.BLACK);
        tl.setOrientation(LinearLayout.VERTICAL);

        TextView tvFirstThCol = new TextView(this);
        TextView tvSecondThCol = new TextView(this);
        tvFirstThCol.setTypeface(latoFontBold);
        tvSecondThCol.setTypeface(latoFontBold);

        // Headerinfo zu Forumpost
        LinearLayout rowForumPostHeader = new LinearLayout(this);
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
        TextView tvForumPost = new TextView(this);
        tvForumPost.setText(tempFP.getText());
        tvForumPost.setBackgroundColor(Color.BLACK);
        tvForumPost.setTextColor(Color.WHITE);
        tvForumPost.setPadding(10, 10, 10, 40);
        tvForumPost.setTypeface(latoFontLight);
        tvForumPost.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        tl.addView(tvForumPost);

        return tl;
    }

    @Override
    protected void onResume() {
        super.onResume();
        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.handset_forum_menu, menu);

        if (member != null) {
            getMenuInflater().inflate(R.menu.signout_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.signin_menu, menu);
        }

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);

        // Sharing
        ShareActionProvider mShare = (ShareActionProvider) item.getActionProvider();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String result = String.format(getString(R.string.share_email_subject), cheatObj.getGameName());
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, result);
        String fullBody = cheatObj.getGameName() + " (" + cheatObj.getSystemName() + "): " + cheatObj.getCheatTitle() + "\n";
        fullBody += Konstanten.BASE_URL + "display/switch.php?id=" + cheatObj.getCheatId() + "\n\n";
        shareIntent.putExtra(Intent.EXTRA_TEXT, fullBody);
        mShare.setShareIntent(shareIntent);

        // Search
        getMenuInflater().inflate(R.menu.search_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_cheatview:
                onBackPressed();
                return true;
            case R.id.action_rate:
                showRatingDialog();
                return true;
            case R.id.action_add_to_favorites:
                Toast.makeText(this, R.string.favorite_adding, Toast.LENGTH_SHORT).show();
                Helper.addFavorite(this, cheatObj);
                return true;
            case R.id.action_share:
                Helper.shareCheat(cheatObj, this);
                return true;
            case R.id.action_metainfo:
                CheatMetaDialog cmDialog = new CheatMetaDialog(this, cheatObj);
                cmDialog.show();
                return true;
            case R.id.action_report:
                showReportDialog();
                return true;
            case R.id.action_submit_cheat:
                Intent explicitIntent = new Intent(this, SubmitCheatActivity.class);
                explicitIntent.putExtra("gameObj", new Gson().toJson(new Game(cheatObj.getGameId(), cheatObj.getGameName(), cheatObj.getSystemId(), cheatObj.getSystemName())));
                startActivity(explicitIntent);
                return true;
            case R.id.action_login:
                Intent loginIntent = new Intent(this, LoginActivity.class);
                startActivityForResult(loginIntent, Konstanten.LOGIN_REGISTER_OK_RETURN_CODE);
                return true;
            case R.id.action_logout:
                member = null;
                Tools.logout(CheatForumActivity.this, settings.edit());
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // Return result code. Login success, Register success etc.
            int intentReturnCode = data.getIntExtra("result", Konstanten.LOGIN_REGISTER_FAIL_RETURN_CODE);

            if (requestCode == Konstanten.LOGIN_REGISTER_OK_RETURN_CODE) {
                member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
                invalidateOptionsMenu();
                if ((member != null) && intentReturnCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
                    Toast.makeText(CheatForumActivity.this, R.string.register_thanks, Toast.LENGTH_LONG).show();
                } else if ((member != null) && intentReturnCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
                    Toast.makeText(CheatForumActivity.this, R.string.login_ok, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    // @Override
    // public void onClick(View v) {
    // Log.d("onClick", "onClick");
    // Bundle arguments = new Bundle();
    // arguments.putInt(CheatDetailTabletFragment.ARG_ITEM_ID, 1);
    // arguments.putString("cheatObj", new Gson().toJson(cheatObj));
    //
    // if (v == btnViewCheat) {
    // Log.d("onClick", "btnViewCheat");
    // finish();
    // } else if (v == btnMetaInfo) {
    // Log.d("onClick", "btnMetaInfo");
    // // TODO
    // } else if (v == btnShare) {
    // Log.d("onClick", "btnShare");
    // Helper.shareCheat(cheatObj, this);
    // } else if (v == btnReport) {
    // Log.d("onClick", "btnReport");
    // showReportDialog();
    // } else if (v == btnShare) {
    // Log.d("onClick", "btnShare");
    // Helper.shareCheat(cheatObj, this);
    // } else if (v == btnRateCheat) {
    // Log.d("onClick", "btnRateCheat");
    // showRatingDialog();
    // }
    // }

    public void showReportDialog() {
        if ((member == null) || (member.getMid() == 0)) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_LONG).show();
        } else {
            FragmentManager fm = getSupportFragmentManager();
            ReportCheatDialog reportCheatDialog = new ReportCheatDialog();
            reportCheatDialog.show(fm, "fragment_report_cheat");
        }
    }

    @Override
    public void onFinishReportDialog(int selectedReason) {
        String[] reasons = getResources().getStringArray(R.array.report_reasons);
        new ReportCheatTask().execute(reasons[selectedReason]);
    }

    public void showRatingDialog() {
        if ((member == null) || (member.getMid() == 0)) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_LONG).show();
        } else {
            Bundle args = new Bundle();
            args.putSerializable("cheatObj", cheatObj);

            FragmentManager fm = getSupportFragmentManager();
            RateCheatDialog ratingCheatDialog = new RateCheatDialog();
            ratingCheatDialog.setArguments(args);
            ratingCheatDialog.show(fm, "fragment_rating_cheat");
        }
    }

    @Override
    public void onFinishRateCheatDialog(int selectedRating) {
        cheatObj.setMemberRating(selectedRating);
        // highlightRatingIcon(true);
        Toast.makeText(this, R.string.rating_inserted, Toast.LENGTH_SHORT).show();
    }

    // public void highlightRatingIcon(boolean highlight) {
    // if (highlight) {
    // btnRateCheat.setImageResource(R.drawable.ic_action_star);
    // } else {
    // btnRateCheat.setImageResource(R.drawable.ic_action_not_important);
    // }
    // }

    private class ReportCheatTask extends AsyncTask<String, Boolean, Boolean> {

        @Override
        protected Boolean doInBackground(String... reason) {

            try {
                Webservice.reportCheat(cheatObj.getCheatId(), member.getMid(), reason[0]);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(CheatForumActivity.this, R.string.thanks_for_reporting, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(CheatForumActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
            }
        }

    }

    private class ForumPostTask extends AsyncTask<ForumPost, Void, Void> {

        @Override
        protected Void doInBackground(ForumPost... params) {

            ForumPost fp = params[0];

            try {
                Webservice.insertForum(cheatObj.getCheatId(), member.getMid(), member.getPassword(), fp.getText());
            } catch (Exception e) {
                Log.e("CheatForumActivity", e.getLocalizedMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            editText.setText("");
            tvEmpty.setVisibility(View.GONE);
            Toast.makeText(CheatForumActivity.this, R.string.forum_submit_ok, Toast.LENGTH_LONG).show();
        }

    }

    private class GetForumBackgroundTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            forumThread = Webservice.getForum(cheatObj.getCheatId());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            fillViewWithThread();
        }

    }

    @Override
    public void onItemSelected(int position) {
        // TODO Auto-generated method stub
    }


}