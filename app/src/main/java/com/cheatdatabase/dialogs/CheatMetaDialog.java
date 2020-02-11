package com.cheatdatabase.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cheatdatabase.R;
import com.cheatdatabase.activity.CheatsByMemberListActivity;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.model.Cheat;
import com.cheatdatabase.model.Member;
import com.cheatdatabase.rest.RestApi;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import needle.Needle;
import retrofit2.Retrofit;

@SuppressLint("SimpleDateFormat")
public class CheatMetaDialog extends Dialog implements OnClickListener {
    private static final String TAG = CheatMetaDialog.class.getSimpleName();
    private Context context;

    private Cheat cheat;
    private Member member;

    @Inject
    Retrofit retrofit;

    private RestApi restApi;

    @BindView(R.id.tvAverageRatingText)
    TextView tvAverageRatingText;
    @BindView(R.id.lifetime_views_text)
    TextView tvTotalViewsText;
    @BindView(R.id.submission_date_text)
    TextView tvSubmissionDateText;
    @BindView(R.id.tvSubmittedByText)
    TextView tvAuthorText;
    @BindView(R.id.views_today_text)
    TextView tvViewsTodayText;
    @BindView(R.id.tvTotalSubmissionsMemberText)
    TextView tvTotalSubmissionCountMemberText;
    @BindView(R.id.tvTotalSubmissionsShow)
    TextView tvTotalSubmissionShowAll;
    @BindView(R.id.tvMemberHomepageTitle)
    TextView tvMemberHomepageTitle;
    @BindView(R.id.tvMemberHomepageText)
    TextView tvMemberHomepageText;
    @BindView(R.id.llSubmittedBy)
    LinearLayout llSubmittedBy;
    @BindView(R.id.llCountSubmissions)
    LinearLayout llCountSubmissions;
    @BindView(R.id.llCountSubmissions2)
    LinearLayout llCountSubmissions2;
    @BindView(R.id.llMemberHomepage)
    LinearLayout llMemberHomepage;
    @BindView(R.id.llRating)
    LinearLayout llRating;
    @BindView(R.id.llBuffer3)
    LinearLayout llBuffer3;
    @BindView(R.id.llBuffer4)
    LinearLayout llBuffer4;
    @BindView(R.id.llBuffer5)
    LinearLayout llBuffer5;
    @BindView(R.id.llBuffer6)
    LinearLayout llBuffer6;

    public CheatMetaDialog(Context context, Cheat cheat) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.layout_cheatview_meta_dialog);
        ButterKnife.bind(this);

        // TODO dagger zeugs....
        // TODO dagger zeugs....
        // TODO dagger zeugs....
//        ((CheatDatabaseApplication) getApplication()).getNetworkComponent().inject(this);
//        restApi = retrofit.create(RestApi.class);

        this.context = context;
        this.cheat = cheat;

        member = cheat.getSubmittingMember();

        tvTotalViewsText.setOnClickListener(this);
        tvViewsTodayText.setOnClickListener(this);
        tvTotalSubmissionCountMemberText.setOnClickListener(this);
        tvTotalSubmissionShowAll.setOnClickListener(this);
        tvSubmissionDateText.setOnClickListener(this);

        llRating.setVisibility(View.GONE);
        llSubmittedBy.setVisibility(View.GONE);
        llCountSubmissions.setVisibility(View.GONE);
        llCountSubmissions2.setVisibility(View.GONE);
        llMemberHomepage.setVisibility(View.GONE);

        llBuffer3.setVisibility(View.GONE);
        llBuffer4.setVisibility(View.GONE);
        llBuffer5.setVisibility(View.GONE);
        llBuffer6.setVisibility(View.GONE);

        loadMetaData();
    }


    private void loadMetaData() {
        // TODO meta laden in RETROFIT block verschieben....
        // TODO meta laden in RETROFIT block verschieben....
        // TODO meta laden in RETROFIT block verschieben....
        // TODO meta laden in RETROFIT block verschieben....
//        Call<List<Cheat>> call = restApi.getCheatsAndRatings(gameObj.getGameId(), memberId, (isAchievementsEnabled ? 1 : 0));
//        call.enqueue(new Callback<List<Cheat>>() {
//            @Override
//            public void onResponse(Call<List<Cheat>> cheats, Response<List<Cheat>> response) {
//                cheatList = response.body();
//
//
//            }
//
//            @Override
//            public void onFailure(Call<List<Cheat>> call, Throwable e) {
//                Log.e(TAG, "getCheatList onFailure: " + e.getLocalizedMessage());
//                Toast.makeText(context, R.string.err_somethings_wrong, Toast.LENGTH_LONG).show()
//            }
//        });


        Needle.onBackgroundThread().execute(() -> {
            try {
                Cheat metaCheat = Webservice.getCheatMetaById(cheat.getCheatId());
                cheat.setViewsTotal(metaCheat.getViewsTotal());
                cheat.setViewsToday(metaCheat.getViewsToday());
                cheat.setAuthorName(metaCheat.getAuthor());
                cheat.setCreated(metaCheat.getCreatedDate());
                cheat.setRatingAverage(metaCheat.getRatingAverage());
                cheat.setVotes(metaCheat.getVotes());
                cheat.setMember(metaCheat.getSubmittingMember());

                updateUI();
            } catch (Exception e) {
                // do nothing
            }
        });
    }

    private void updateUI() {
        Needle.onMainThread().execute(() -> {
            try {
                member = cheat.getSubmittingMember();

                if (cheat.getVotes() > 0) {
                    llRating.setVisibility(View.VISIBLE);
                    llBuffer3.setVisibility(View.VISIBLE);
                    llBuffer4.setVisibility(View.VISIBLE);

                    String averageRatingText = context.getString(R.string.meta_average_rating1, Math.round(cheat.getRatingAverage()));
                    averageRatingText += " " + context.getString(R.string.meta_average_rating2, String.valueOf(cheat.getVotes()));
                    tvAverageRatingText.setText(averageRatingText);
                } else {
                    llBuffer3.setVisibility(View.GONE);
                    llBuffer4.setVisibility(View.GONE);
                }

                tvTotalViewsText.setText(String.valueOf(cheat.getViewsTotal()));
                tvViewsTodayText.setText(String.valueOf(cheat.getViewsToday()));

                if (member.getCheatSubmissionCount() > 0) {
                    llBuffer4.setVisibility(View.VISIBLE);
                    tvTotalSubmissionCountMemberText.setText(String.valueOf(member.getCheatSubmissionCount()));
                    tvTotalSubmissionShowAll.setText("[" + context.getString(R.string.meta_show_all_cheats) + "]");
                } else {
                    llCountSubmissions.setVisibility(View.GONE);
                    llCountSubmissions2.setVisibility(View.GONE);
                    llBuffer6.setVisibility(View.GONE);
                }

                /*
                 * Bei Member-ID 1 werden Teile ausgeblendet
                 */
                if (member.getMid() <= 1) {
                    llSubmittedBy.setVisibility(View.GONE);
                    llCountSubmissions.setVisibility(View.GONE);
                    llCountSubmissions2.setVisibility(View.GONE);
                    llMemberHomepage.setVisibility(View.GONE);
                    llBuffer4.setVisibility(View.GONE);
                    llBuffer5.setVisibility(View.GONE);
                    llBuffer6.setVisibility(View.GONE);
                } else {
                    llSubmittedBy.setVisibility(View.VISIBLE);
                    llCountSubmissions.setVisibility(View.VISIBLE);
                    llCountSubmissions2.setVisibility(View.VISIBLE);
                    llBuffer5.setVisibility(View.VISIBLE);
                    llBuffer6.setVisibility(View.VISIBLE);

                    if (member.getWebsite().length() > 3) {
                        llMemberHomepage.setVisibility(View.VISIBLE);
                        tvMemberHomepageText.setText(member.getWebsite());
                        tvMemberHomepageText.setOnClickListener(CheatMetaDialog.this);
                    } else {
                        llBuffer6.setVisibility(View.INVISIBLE);
                    }
                    if (member.getUsername().length() > 1) {
                        tvAuthorText.setText(member.getUsername());
                        tvMemberHomepageTitle.setText(member.getUsername() + "'s " + context.getString(R.string.meta_member_homepage));
                    }
                    tvAuthorText.setOnClickListener(CheatMetaDialog.this);
                }

                Date dateObj = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(cheat.getCreatedDate());

                java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context.getApplicationContext());
                String newDateStr = dateFormat.format(dateObj);
                tvSubmissionDateText.setText(newDateStr);
            } catch (Exception e) {
                Log.e(TAG, "updateUI Error: " + e.getLocalizedMessage());
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == tvTotalSubmissionShowAll) {
            Intent explicitIntent = new Intent(context, CheatsByMemberListActivity.class);
            explicitIntent.putExtra("memberObj", member);
            context.startActivity(explicitIntent);
        } else if (v == tvMemberHomepageText) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(member.getWebsite()));
            context.startActivity(intent);
        }
    }

}
