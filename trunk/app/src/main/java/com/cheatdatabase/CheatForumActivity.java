package com.cheatdatabase;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
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
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.ForumPost;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.dialogs.CheatMetaDialog;
import com.cheatdatabase.dialogs.RateCheatDialog;
import com.cheatdatabase.dialogs.ReportCheatDialog;
import com.cheatdatabase.events.CheatRatingFinishedEvent;
import com.cheatdatabase.events.CheatReportingFinishedEvent;
import com.cheatdatabase.helpers.Helper;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.google.android.gms.analytics.HitBuilders;
import com.google.gson.Gson;
import com.mopub.mobileads.MoPubView;
import com.splunk.mint.Mint;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Calendar;

/**
 * Displaying the forum of one cheat.
 */
@EActivity(R.layout.activity_cheat_forum)
public class CheatForumActivity extends AppCompatActivity implements CheatListFragment.CheatListClickCallbacks {

    private static String TAG = CheatForumActivity.class.getSimpleName();

    @Extra
    Cheat cheatObj;

    @Extra
    Game gameObj;

    @Bean
    Tools tools;

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    @ViewById(R.id.llForumMain)
    LinearLayout llForumMain;

    @ViewById(R.id.text_cheat_title)
    TextView tvCheatTitle;

    @ViewById(R.id.tvEmpty)
    TextView tvEmpty;

    @ViewById(R.id.sv)
    ScrollView sv;

    @ViewById(R.id.reload)
    ImageView reloadView;

    @ViewById(R.id.etEnterForumPost)
    EditText editText;

    @ViewById(R.id.btnSubmitPost)
    Button postButton;

    private ForumPost[] forumThread;

    private SharedPreferences settings;

    private Member member;

    private ViewGroup adViewContainer;
    private MoPubView mAdView;

    private Typeface latoFontBold;
    private Typeface latoFontLight;

    private ShareActionProvider mShare;

    @AfterViews
    public void onCreateView() {
        cheatObj = (Cheat) getIntent().getSerializableExtra("cheatObj");
        gameObj = (Game) getIntent().getSerializableExtra("gameObj");

        init();

        tvCheatTitle.setText(cheatObj.getCheatTitle() + " (" + getString(R.string.forum) + ")");
        tvCheatTitle.setTypeface(latoFontBold);

        tvEmpty.setTypeface(latoFontLight);

        if (Reachability.reachability.isReachable) {
            reloadView.setVisibility(View.GONE);
            //new GetForumBackgroundTask().execute();
            loadForumAsync();
        } else {
            reloadView.setVisibility(View.VISIBLE);
            reloadView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (Reachability.reachability.isReachable) {
                        //new GetForumBackgroundTask().execute();
                        loadForumAsync();
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
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(this);
        }
        Mint.initAndStartSession(this, Konstanten.SPLUNK_MINT_API_KEY);

        CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", cheatObj.getGameName() + " (" + cheatObj.getSystemName() + ")").setLabel("activity").build());

        settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);

        mAdView = Tools.initMoPubAdView(this, mAdView);

        latoFontLight = tools.getFont(getAssets(), Konstanten.FONT_LIGHT);
        latoFontBold = tools.getFont(getAssets(), Konstanten.FONT_BOLD);

        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setTitle(cheatObj.getGameName());
        getSupportActionBar().setSubtitle(cheatObj.getSystemName());
    }

    private void handleIntent(final Intent intent) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                cheatObj = (Cheat) intent.getSerializableExtra("cheatObj");
                gameObj = (Game) intent.getSerializableExtra("gameObj");

                CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", cheatObj.getGameName() + " (" + cheatObj.getSystemName() + ")").setLabel("activity").build());
            }
        }).start();

    }

    /**
     * Fill table with parsed ForumPosts.
     */
//    private void fillViewWithThread() {
//        reloadView.setVisibility(View.GONE);
//
//        llForumMain.removeAllViews();
//        if (forumThread.length > 0) {
//            tvEmpty.setVisibility(View.GONE);
//        } else {
//            tvEmpty.setVisibility(View.VISIBLE);
//        }
//
//        for (int i = 0; i < forumThread.length; i++) {
//            ForumPost tempFP = forumThread[i];
//
//            LinearLayout tl = createForumPosts(tempFP);
//            llForumMain.addView(tl, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
//        }
//
//    }

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

        // Headerinfo of Forumpost
        LinearLayout rowForumPostHeader = new LinearLayout(this);
        rowForumPostHeader.setBackgroundColor(Color.DKGRAY);
        rowForumPostHeader.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        rowForumPostHeader.setGravity(Gravity.CENTER_HORIZONTAL);
        rowForumPostHeader.setPadding(5, 5, 10, 5);
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
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(this);
        }
        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        Reachability.unregister(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
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
        mShare = (android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider(item);
        setShareText(cheatObj);

        // Search
        getMenuInflater().inflate(R.menu.search_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }

    private void setShareText(Cheat visibleCheat) {
        String cheatShareTitle = String.format(getString(R.string.share_email_subject), visibleCheat.getGameName());
        String cheatShareBody = visibleCheat.getGameName() + " (" + visibleCheat.getSystemName() + "): " + visibleCheat.getCheatTitle() + "\n";
        cheatShareBody += Konstanten.BASE_URL + "display/switch.php?id=" + visibleCheat.getCheatId() + "\n\n";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, cheatShareTitle);
        shareIntent.putExtra(Intent.EXTRA_TEXT, cheatShareBody);
        setShareIntent(shareIntent);
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShare != null) {
            mShare.setShareIntent(shareIntent);
        }
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
                Intent explicitIntent = new Intent(this, SubmitCheatActivity_.class);
                explicitIntent.putExtra("gameObj", gameObj);
                startActivity(explicitIntent);
                return true;
            case R.id.action_login:
                Intent loginIntent = new Intent(this, LoginActivity_.class);
                startActivityForResult(loginIntent, Konstanten.LOGIN_REGISTER_OK_RETURN_CODE);
                return true;
            case R.id.action_logout:
                member = null;
                tools.logout(CheatForumActivity.this, settings.edit());
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

    public void showReportDialog() {
        if ((member == null) || (member.getMid() == 0)) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_LONG).show();
        } else {
            Bundle args = new Bundle();
            args.putSerializable("cheatObj", cheatObj);

            FragmentManager fm = getSupportFragmentManager();
            ReportCheatDialog reportCheatDialog = new ReportCheatDialog();
            reportCheatDialog.setArguments(args);
            reportCheatDialog.show(fm, "fragment_report_cheat");
        }
    }

    @Subscribe
    public void onEvent(CheatReportingFinishedEvent result) {
        if (result.isSucceeded()) {
            Toast.makeText(this, R.string.thanks_for_reporting, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
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

    @Subscribe
    public void onEvent(CheatRatingFinishedEvent result) {
        Log.d(TAG, "OnEvent result: " + result.getRating());
        cheatObj.setMemberRating(result.getRating());
        // highlightRatingIcon(true);
        Toast.makeText(this, R.string.rating_inserted, Toast.LENGTH_SHORT).show();
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

    @Background
    public void loadForumAsync() {
        forumThread = Webservice.getForum(cheatObj.getCheatId());

        forumLoaded();
    }

    @UiThread
    public void forumLoaded() {
        reloadView.setVisibility(View.GONE);

        llForumMain.removeAllViews();
        if (forumThread.length > 0) {
            tvEmpty.setVisibility(View.GONE);

            for (ForumPost tempFP : forumThread) {
                LinearLayout tl = createForumPosts(tempFP);
                llForumMain.addView(tl, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            }
        } else {
            tvEmpty.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onItemSelected(int position) {
        // TODO Auto-generated method stub
    }

}