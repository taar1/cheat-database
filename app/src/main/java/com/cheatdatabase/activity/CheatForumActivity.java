package com.cheatdatabase.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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

import com.cheatdatabase.CheatDatabaseApplication;
import com.cheatdatabase.R;
import com.cheatdatabase.callbacks.GenericCallback;
import com.cheatdatabase.data.model.Cheat;
import com.cheatdatabase.data.model.ForumPost;
import com.cheatdatabase.data.model.Game;
import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.dialogs.CheatMetaDialog;
import com.cheatdatabase.dialogs.RateCheatMaterialDialog;
import com.cheatdatabase.dialogs.ReportCheatMaterialDialog;
import com.cheatdatabase.events.CheatRatingFinishedEvent;
import com.cheatdatabase.helpers.AeSimpleMD5;
import com.cheatdatabase.helpers.Helper;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.rest.RestApi;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import needle.Needle;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Displaying the forum of one cheat.
 */
public class CheatForumActivity extends AppCompatActivity implements GenericCallback {

    private static String TAG = CheatForumActivity.class.getSimpleName();

    private Cheat cheatObj;
    private Game gameObj;
    private SharedPreferences settings;
    private ShareActionProvider mShare;
    private Member member;

    @BindView(R.id.outer_layout)
    LinearLayout outerLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.main_forum)
    LinearLayout llForumMain;
    @BindView(R.id.text_cheat_title)
    TextView tvCheatTitle;
    @BindView(R.id.textview_empty)
    TextView tvEmpty;
    @BindView(R.id.sv)
    ScrollView sv;
    @BindView(R.id.reload)
    ImageView reloadView;
    @BindView(R.id.forum_text_input)
    EditText editText;
    @BindView(R.id.submit_button)
    Button postButton;
    @BindView(R.id.banner_container)
    LinearLayout facebookBanner;
    private AdView adView;

    @Inject
    Retrofit retrofit;

    private RestApi restApi;

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

            if (Reachability.reachability.isReachable) {
                reloadView.setVisibility(View.GONE);
                fetchForumPosts();
            } else {
                reloadView.setVisibility(View.VISIBLE);
                reloadView.setOnClickListener(v -> {
                    if (Reachability.reachability.isReachable) {
                        fetchForumPosts();
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

        ((CheatDatabaseApplication) getApplication()).getNetworkComponent().inject(this);
        restApi = retrofit.create(RestApi.class);

        settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);

        adView = new AdView(this, Konstanten.FACEBOOK_AUDIENCE_NETWORK_NATIVE_BANNER_ID, AdSize.BANNER_HEIGHT_50);
        facebookBanner.addView(adView);
        adView.loadAd();

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

            ForumPost forumPost = new ForumPost();
            forumPost.setText(editText.getText().toString().trim());
            forumPost.setUsername(member.getUsername());
            forumPost.setName(member.getUsername());
            forumPost.setEmail(member.getEmail());
            forumPost.setCreated(months[mMonth] + " " + mDay + ", " + mYear + " / " + leadingZeroHour + ":" + leadingZeroMin);

            if (Reachability.reachability.isReachable) {
                llForumMain.addView(createForumPosts(forumPost), new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

                // Führt die ScrollView bis ganz nach unten zum neusten Post
                sv.post(() -> sv.fullScroll(View.FOCUS_DOWN));
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

                submitForumPost(forumPost);

            } else {
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Fills the table of the forum
     *
     * @param forumPost
     * @return
     */
    private LinearLayout createForumPosts(ForumPost forumPost) {
        LinearLayout tl = new LinearLayout(this);
        tl.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        tl.setGravity(Gravity.TOP);
        tl.setBackgroundColor(Color.BLACK);
        tl.setOrientation(LinearLayout.VERTICAL);

        TextView tvFirstThCol = new TextView(this);
        TextView tvSecondThCol = new TextView(this);

        // TODO hier noch programmatisch die FONT auf LATO setzen
//        tvFirstThCol.setTypeface(latoFontBold);
//        tvSecondThCol.setTypeface(latoFontBold);

        // Headerinfo of Forumpost
        LinearLayout rowForumPostHeader = new LinearLayout(this);
        rowForumPostHeader.setBackgroundColor(Color.DKGRAY);
        rowForumPostHeader.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        rowForumPostHeader.setGravity(Gravity.CENTER_HORIZONTAL);
        rowForumPostHeader.setPadding(5, 5, 10, 5);
        rowForumPostHeader.setOrientation(LinearLayout.HORIZONTAL);

        if (!forumPost.getUsername().equalsIgnoreCase("null")) {
            tvFirstThCol.setText(forumPost.getUsername().trim());
        } else {
            tvFirstThCol.setText(forumPost.getName().trim());
        }
        tvFirstThCol.setTextColor(Color.WHITE);
        tvFirstThCol.setGravity(Gravity.START);
        tvFirstThCol.setSingleLine(true);
        tvFirstThCol.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        tvSecondThCol.setText(forumPost.getCreated());
        tvSecondThCol.setTextColor(Color.LTGRAY);
        tvSecondThCol.setGravity(Gravity.END);
        tvSecondThCol.setSingleLine(true);
        tvSecondThCol.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        rowForumPostHeader.addView(tvFirstThCol);
        rowForumPostHeader.addView(tvSecondThCol);

        tl.addView(rowForumPostHeader);

        // Forum-Post
        TextView tvForumPost = new TextView(this);
        tvForumPost.setText(forumPost.getText());
        tvForumPost.setBackgroundColor(Color.BLACK);
        tvForumPost.setTextColor(Color.WHITE);
        tvForumPost.setPadding(10, 10, 10, 40);
        // TODO hier noch programmatisch die FONT auf LATO setzen
//        tvForumPost.setTypeface(latoFontLight);
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
        String cheatShareBody = visibleCheat.getGame().getGameName() + " (" + visibleCheat.getSystem().getSystemName() + "): " + visibleCheat.getCheatTitle() + "\n";
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
            case R.id.action_cheatview:
                onBackPressed();
                return true;
            case R.id.action_rate:
                showRatingDialog();
                return true;
            case R.id.action_add_to_favorites:
                Tools.showSnackbar(outerLayout, getString(R.string.favorite_adding));
                int memberId = 0;
                if (member != null) {
                    memberId = member.getMid();
                }
                Helper.addFavorite(this, cheatObj, memberId, this);
                return true;
            case R.id.action_share:
                Helper.shareCheat(cheatObj, this);
                return true;
            case R.id.action_metainfo:
                CheatMetaDialog cmDialog = new CheatMetaDialog(this, cheatObj, restApi, outerLayout);
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
                } else if ((member != null) && intentReturnCode == Konstanten.LOGIN_SUCCESS_RETURN_CODE) {
                    Toast.makeText(CheatForumActivity.this, R.string.login_ok, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void showReportDialog() {
        if ((member == null) || (member.getMid() == 0)) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_LONG).show();
        } else {
            new ReportCheatMaterialDialog(this, cheatObj, member, restApi, outerLayout);
        }
    }

    public void showRatingDialog() {
        if ((member == null) || (member.getMid() == 0)) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_LONG).show();
        } else {
            new RateCheatMaterialDialog(this, cheatObj, member, restApi, outerLayout);
        }
    }

    @Subscribe
    public void onEvent(CheatRatingFinishedEvent result) {
        Log.d(TAG, "OnEvent result: " + result.getRating());
        cheatObj.setMemberRating(result.getRating());
        // highlightRatingIcon(true);
        Toast.makeText(this, R.string.rating_inserted, Toast.LENGTH_SHORT).show();
    }

    private void submitForumPost(ForumPost forumPost) {
        Call<Void> call = null;
        try {
            call = restApi.insertForum(member.getMid(), cheatObj.getCheatId(), AeSimpleMD5.MD5(member.getPassword()), forumPost.getText());
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> forumPost, Response<Void> response) {
                    Log.d(TAG, "submit forum post SUCCESS");

                    Intent output = new Intent();
                    output.putExtra("newForumCount", cheatObj.getForumCount() + 1);
                    setResult(RESULT_OK, output);
                    updateUI();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable e) {
                    Log.e(TAG, "Submit forum post FAIL: " + e.getLocalizedMessage());
                    Toast.makeText(CheatForumActivity.this, R.string.err_occurred, Toast.LENGTH_LONG).show();
                }
            });
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "postForumEntry: ", e);
        }
    }

    private void updateUI() {
        Needle.onMainThread().execute(() -> {
            editText.setText("");
            tvEmpty.setVisibility(View.GONE);
            Toast.makeText(CheatForumActivity.this, R.string.forum_submit_ok, Toast.LENGTH_LONG).show();
        });
    }

    void fetchForumPosts() {
        Call<List<ForumPost>> call = restApi.getForum(cheatObj.getCheatId());
        call.enqueue(new Callback<List<ForumPost>>() {
            @Override
            public void onResponse(Call<List<ForumPost>> forum, Response<List<ForumPost>> response) {
                List<ForumPost> forumThread = response.body();

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
            }

            @Override
            public void onFailure(Call<List<ForumPost>> call, Throwable e) {
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    @Override
    public void success() {
        Log.d(TAG, "CheatForumActivity ADD FAV success: ");
        Tools.showSnackbar(outerLayout, getString(R.string.add_favorite_ok));
    }

    @Override
    public void fail(Exception e) {
        Log.d(TAG, "CheatForumActivity ADD FAV fail: ");
        Tools.showSnackbar(outerLayout, getString(R.string.error_adding_favorite));
    }
}