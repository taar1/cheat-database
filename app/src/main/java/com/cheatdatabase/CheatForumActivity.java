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
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;

import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.ForumPost;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.dialogs.CheatMetaDialog;
import com.cheatdatabase.dialogs.RateCheatMaterialDialog;
import com.cheatdatabase.dialogs.ReportCheatMaterialDialog;
import com.cheatdatabase.events.CheatRatingFinishedEvent;
import com.cheatdatabase.helpers.Helper;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import needle.Needle;

/**
 * Displaying the forum of one cheat.
 */
public class CheatForumActivity extends AppCompatActivity {

    private static String TAG = CheatForumActivity.class.getSimpleName();

    private Cheat cheatObj;
    private Game gameObj;

    @BindView(R.id.outer_layout)
    LinearLayout outerLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.llForumMain)
    LinearLayout llForumMain;
    @BindView(R.id.text_cheat_title)
    TextView tvCheatTitle;
    @BindView(R.id.tvEmpty)
    TextView tvEmpty;
    @BindView(R.id.sv)
    ScrollView sv;
    @BindView(R.id.reload)
    ImageView reloadView;
    @BindView(R.id.etEnterForumPost)
    EditText editText;
    @BindView(R.id.btnSubmitPost)
    Button postButton;

    private SharedPreferences settings;
    private ShareActionProvider mShare;
    private Member member;
    private Typeface latoFontBold;
    private Typeface latoFontLight;

    @BindView(R.id.banner_container)
    LinearLayout facebookBanner;
    private AdView adView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheat_forum);
        ButterKnife.bind(this);

        cheatObj = getIntent().getParcelableExtra("cheatObj");
        gameObj = getIntent().getParcelableExtra("gameObj");

        if (cheatObj == null || gameObj == null) {
            Toast.makeText(this, R.string.err_somethings_wrong, Toast.LENGTH_LONG).show();
            finish();
        } else {
            init();

            tvCheatTitle.setText(cheatObj.getCheatTitle() + " (" + getString(R.string.forum) + ")");
            tvCheatTitle.setTypeface(latoFontBold);

            tvEmpty.setTypeface(latoFontLight);

            if (Reachability.reachability.isReachable) {
                reloadView.setVisibility(View.GONE);
                loadForumAsync();
            } else {
                reloadView.setVisibility(View.VISIBLE);
                reloadView.setOnClickListener(v -> {
                    if (Reachability.reachability.isReachable) {
                        loadForumAsync();
                    } else {
                        Toast.makeText(CheatForumActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                    }
                });
                Toast.makeText(CheatForumActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
            }

            postButton.setOnClickListener(v -> {
                if (Reachability.reachability.isReachable) {
                    if (editText.getText().toString().trim().length() > 0) {
                        submitPost();
                    } else {
                        Toast.makeText(CheatForumActivity.this, R.string.fill_everything, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CheatForumActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void init() {
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(this);
        }

        settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);

        adView = new AdView(this, Konstanten.FACEBOOK_AUDIENCE_NETWORK_NATIVE_BANNER_ID, AdSize.BANNER_HEIGHT_50);
        facebookBanner.addView(adView);
        adView.loadAd();

        latoFontLight = Tools.getFont(getAssets(), Konstanten.FONT_LIGHT);
        latoFontBold = Tools.getFont(getAssets(), Konstanten.FONT_BOLD);

        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setTitle((gameObj.getGameName() != null ? gameObj.getGameName() : ""));
        getSupportActionBar().setSubtitle((gameObj.getSystemName() != null ? gameObj.getSystemName() : ""));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
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
        if (adView != null) {
            adView.destroy();
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
        mShare = (androidx.appcompat.widget.ShareActionProvider) MenuItemCompat.getActionProvider(item);
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
                Tools.showSnackbar(outerLayout, getString(R.string.favorite_adding));
                Helper.addFavorite(this, outerLayout, cheatObj);
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
                explicitIntent.putExtra("gameObj", gameObj);
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

    public void showReportDialog() {
        if ((member == null) || (member.getMid() == 0)) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_LONG).show();
        } else {
            new ReportCheatMaterialDialog(this, cheatObj, member);
        }
    }

    public void showRatingDialog() {
        if ((member == null) || (member.getMid() == 0)) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_LONG).show();
        } else {
            new RateCheatMaterialDialog(this, cheatObj, member);
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
                Log.e(TAG, e.getLocalizedMessage());
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

    public void loadForumAsync() {
        Needle.onBackgroundThread().execute(() -> forumLoaded(Webservice.getForum(cheatObj.getCheatId())));
    }

    public void forumLoaded(List<ForumPost> forumThread) {

        Needle.onMainThread().execute(() -> {
            reloadView.setVisibility(View.GONE);

            llForumMain.removeAllViews();
            if (forumThread.size() > 0) {
                tvEmpty.setVisibility(View.GONE);

                for (ForumPost forumPost : forumThread) {
                    LinearLayout linearLayout = createForumPosts(forumPost);
                    llForumMain.addView(linearLayout, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                }
            } else {
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });


    }

}