package com.cheatdatabase.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import com.applovin.adview.AppLovinAdView;
import com.cheatdatabase.R;
import com.cheatdatabase.callbacks.GenericCallback;
import com.cheatdatabase.data.model.Cheat;
import com.cheatdatabase.data.model.ForumPost;
import com.cheatdatabase.data.model.Game;
import com.cheatdatabase.dialogs.CheatMetaDialog;
import com.cheatdatabase.dialogs.RateCheatMaterialDialog;
import com.cheatdatabase.dialogs.ReportCheatMaterialDialog;
import com.cheatdatabase.events.CheatRatingFinishedEvent;
import com.cheatdatabase.helpers.AeSimpleMD5;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.rest.RestApi;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.hilt.android.AndroidEntryPoint;
import needle.Needle;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Displaying the forum of one cheat.
 */
@AndroidEntryPoint
public class CheatForumActivity extends AppCompatActivity implements GenericCallback {

    private static final String TAG = CheatForumActivity.class.getSimpleName();

    private Cheat cheatObj;
    private Game gameObj;

    @Inject
    Tools tools;
    @Inject
    RestApi restApi;

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
    @BindView(R.id.ad_container)
    AppLovinAdView appLovinAdView;

    private final ActivityResultLauncher<Intent> resultContract =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), getActivityResultRegistry(), activityResult -> {
                int intentReturnCode = activityResult.getResultCode();
                if (intentReturnCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
                    tools.showSnackbar(outerLayout, getString(R.string.register_thanks));
                } else if (intentReturnCode == Konstanten.LOGIN_SUCCESS_RETURN_CODE) {
                    tools.showSnackbar(outerLayout, getString(R.string.login_ok));
                } else if (activityResult.getResultCode() == Konstanten.RECOVER_PASSWORD_ATTEMPT) {
                    tools.showSnackbar(outerLayout, getString(R.string.recover_login_success));
                }
                invalidateOptionsMenu();
            });

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

        appLovinAdView.loadNextAd();

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setTitle((gameObj.getGameName() != null ? gameObj.getGameName() : ""));
        getSupportActionBar().setSubtitle((gameObj.getSystemName() != null ? gameObj.getSystemName() : ""));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        appLovinAdView.loadNextAd();
    }

    /**
     * Submits the forum post and scrolls down to the bottom of the list.
     */
    private void submitPost() {
        if (tools.getMember() == null) {
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
            forumPost.setUsername(tools.getMember().getUsername());
            forumPost.setName(tools.getMember().getUsername());
            forumPost.setEmail(tools.getMember().getEmail());
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.handset_forum_menu, menu);

        if (tools.getMember() != null) {
            getMenuInflater().inflate(R.menu.signout_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.signin_menu, menu);
        }

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
            case R.id.action_cheatview:
                onBackPressed();
                return true;
            case R.id.action_rate:
                showRatingDialog();
                return true;
            case R.id.action_add_to_favorites:
                tools.showSnackbar(outerLayout, getString(R.string.favorite_adding));
                int memberId = 0;
                if (tools.getMember() != null) {
                    memberId = tools.getMember().getMid();
                }
                tools.addFavorite(cheatObj, memberId, this);
                return true;
            case R.id.action_share:
                tools.shareCheat(cheatObj);
                return true;
            case R.id.action_metainfo:
                new CheatMetaDialog(CheatForumActivity.this, cheatObj, outerLayout, tools).show();
                return true;
            case R.id.action_report:
                showReportDialog();
                return true;
            case R.id.action_submit_cheat:
                Intent explicitIntent = new Intent(this, SubmitCheatFormActivity.class);
                explicitIntent.putExtra("gameObj", gameObj);
                startActivity(explicitIntent);
                return true;
            case R.id.action_login:
                resultContract.launch(new Intent(this, LoginActivity.class));
                return true;
            case R.id.action_logout:
                tools.logout();
                tools.showSnackbar(outerLayout, getString(R.string.logout_ok));
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showReportDialog() {
        if ((tools.getMember() == null) || (tools.getMember().getMid() == 0)) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_LONG).show();
        } else {
            new ReportCheatMaterialDialog(this, cheatObj, tools.getMember(), outerLayout, tools);
        }
    }

    public void showRatingDialog() {
        if ((tools.getMember() == null) || (tools.getMember().getMid() == 0)) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_LONG).show();
        } else {
            new RateCheatMaterialDialog(this, cheatObj, tools.getMember(), outerLayout, tools);
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
            call = restApi.insertForum(tools.getMember().getMid(), cheatObj.getCheatId(), AeSimpleMD5.MD5(tools.getMember().getPassword()), forumPost.getText());
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

    @Override
    public void success() {
        Log.d(TAG, "CheatForumActivity ADD FAV success: ");
        tools.showSnackbar(outerLayout, getString(R.string.add_favorite_ok));
    }

    @Override
    public void fail(Exception e) {
        Log.d(TAG, "CheatForumActivity ADD FAV fail: ");
        tools.showSnackbar(outerLayout, getString(R.string.error_adding_favorite));
    }
}