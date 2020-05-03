package com.cheatdatabase.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.cheatdatabase.R;
import com.cheatdatabase.activity.CheatsByMemberListActivity;
import com.cheatdatabase.data.model.Cheat;
import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.rest.RestApi;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("SimpleDateFormat")
public class CheatMetaDialog extends Dialog implements OnClickListener {
    private static final String TAG = "CheatMetaDialog";

    private Context context;
    private Cheat cheat;
    private Member member;
    private RestApi restApi;
    private View view;

    private ConstraintLayout outerLayout;
    private TextView averageRatingTitle;
    private TextView averageRatingText;
    private TextView lifetimeViewsText;
    private TextView submissionDateText;
    private TextView submittedByText;
    private TextView viewsTodayText;
    private TextView totalCheatsByMemberText;
    private TextView showAllCheatsByMember;
    private TextView websiteTitle;
    private TextView websiteText;
    private TextView lifetimeViewsTitle;
    private TextView submissionDateTitle;
    private TextView submittedByTitle;
    private TextView viewsTodayTitle;
    private TextView totalCheatsByMemberTitle;
    private View divider3;
    private View divider4;
    private View divider5;
    private View divider6;

    public CheatMetaDialog(Context context, Cheat cheat, RestApi restApi, View view) {
        super(context);
        this.context = context;
        this.cheat = cheat;
        this.restApi = restApi;
        this.view = view;

        member = cheat.getSubmittingMember();

        final MaterialDialog metaInfoDialog = new MaterialDialog.Builder(context)
                .title(R.string.title_cheat_details)
                .customView(R.layout.layout_cheatview_meta_dialog, false)
                .positiveText(R.string.ok)
                .theme(Theme.DARK)
                .show();

        bind(metaInfoDialog.getCustomView());

        lifetimeViewsText.setOnClickListener(this);
        viewsTodayText.setOnClickListener(this);
        totalCheatsByMemberText.setOnClickListener(this);
        showAllCheatsByMember.setOnClickListener(this);
        submissionDateText.setOnClickListener(this);

        loadMetaData();

    }

    private void loadMetaData() {
        Call<JsonObject> call = restApi.getCheatMetaById(cheat.getCheatId());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> metaInfo, Response<JsonObject> response) {
                JsonObject metaInfoData = response.body();
                Log.d(TAG, "onResponse metaInfo: " + metaInfo);

                if (metaInfoData.get("viewsTotal") != null) {
                    cheat.setViewsTotal(metaInfoData.get("viewsTotal").getAsInt());
                }
                if (metaInfoData.get("viewsToday") != null) {
                    cheat.setViewsToday(metaInfoData.get("viewsToday").getAsInt());
                }
                if (metaInfoData.get("author") != null) {
                    cheat.setAuthorName(metaInfoData.get("author").getAsString());
                }
                if (metaInfoData.get("created") != null) {
                    cheat.setCreated(metaInfoData.get("created").getAsString());
                }
                if (metaInfoData.get("rating") != null) {
                    cheat.setRatingAverage(metaInfoData.get("rating").getAsFloat());
                }
                if (metaInfoData.get("votes") != null) {
                    cheat.setVotes(metaInfoData.get("votes").getAsInt());
                }

                if (member == null) {
                    member = new Member();
                }
                if (metaInfoData.get("memberId") != null) {
                    member.setMid(metaInfoData.get("memberId").getAsInt());
                }
                if (metaInfoData.get("website") != null) {
                    member.setWebsite(metaInfoData.get("website").getAsString());
                }
                if (metaInfoData.get("city") != null) {
                    member.setCity(metaInfoData.get("city").getAsString());
                }

                cheat.setMember(member);

                updateUI();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable e) {
                Log.e(TAG, "getCheatList onFailure: " + e.getLocalizedMessage());
//                Toast.makeText(context, R.string.err_somethings_wrong, Toast.LENGTH_LONG).show();
                Tools.showSnackbar(view, context.getString(R.string.err_somethings_wrong), 5000);
            }
        });
    }

    private void updateUI() {
        try {
            member = cheat.getSubmittingMember();

            // 1: SUBMISSION DATE
            Date dateObj = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(cheat.getCreatedDate());
            java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context.getApplicationContext());
            String newDateStr = dateFormat.format(dateObj);
            submissionDateText.setText(newDateStr);

            // 2: LIFETIME VIEWS
            lifetimeViewsText.setText(String.valueOf(cheat.getViewsTotal()));

            // 3: VIEWS TODAY
            viewsTodayText.setText(String.valueOf(cheat.getViewsToday()));

            divider3.setVisibility(View.GONE);
            divider4.setVisibility(View.GONE);
            divider5.setVisibility(View.GONE);
            divider6.setVisibility(View.GONE);

            // 4: AVERAGE CHEAT RATING
            if (cheat.getVotes() > 0) {
                averageRatingTitle.setVisibility(View.VISIBLE);
                averageRatingText.setVisibility(View.VISIBLE);

                String averageRatingText = context.getString(R.string.meta_average_rating1, Math.round(cheat.getRatingAverage()));
                averageRatingText += " " + context.getString(R.string.meta_average_rating2, String.valueOf(cheat.getVotes()));
                this.averageRatingText.setText(averageRatingText);

                divider3.setVisibility(View.VISIBLE);
            } else {
                averageRatingTitle.setVisibility(View.GONE);
                averageRatingText.setVisibility(View.GONE);
            }


            // 5: SUBMITTED BY
            if (member.getUsername() != null) {
                submittedByTitle.setVisibility(View.VISIBLE);
                submittedByText.setVisibility(View.VISIBLE);

                submittedByText.setText(member.getUsername());
                submittedByText.setOnClickListener(CheatMetaDialog.this);

                divider4.setVisibility(View.VISIBLE);
            } else {
                submittedByTitle.setVisibility(View.GONE);
                submittedByText.setVisibility(View.GONE);
            }

            // 6: AMOUNT OF CHEATS BY MEMBER
            if ((member != null) && (member.getCheatSubmissionCount() > 0)) {
                totalCheatsByMemberTitle.setVisibility(View.VISIBLE);
                totalCheatsByMemberText.setVisibility(View.VISIBLE);
                showAllCheatsByMember.setVisibility(View.VISIBLE);

                totalCheatsByMemberText.setText(String.valueOf(member.getCheatSubmissionCount()));
                showAllCheatsByMember.setText("[" + context.getString(R.string.meta_show_all_cheats) + "]");
                showAllCheatsByMember.setOnClickListener(CheatMetaDialog.this);

                divider5.setVisibility(View.VISIBLE);
            } else {
                totalCheatsByMemberTitle.setVisibility(View.GONE);
                totalCheatsByMemberText.setVisibility(View.GONE);
                showAllCheatsByMember.setVisibility(View.GONE);
            }

            // 7: WEBSITE
            if ((member.getWebsite() != null) && (member.getWebsite().length() > 3) && (member.getUsername() != null)) {
                websiteTitle.setVisibility(View.VISIBLE);
                websiteText.setVisibility(View.VISIBLE);

                websiteText.setText(member.getWebsite());
                websiteTitle.setText(member.getUsername() + "'s " + context.getString(R.string.meta_member_homepage));
                websiteText.setOnClickListener(CheatMetaDialog.this);

                divider6.setVisibility(View.VISIBLE);
            } else {
                websiteTitle.setVisibility(View.GONE);
                websiteText.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "updateUI Error: " + e.getLocalizedMessage());
        }

        outerLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        if (v == showAllCheatsByMember) {
            Intent explicitIntent = new Intent(context, CheatsByMemberListActivity.class);
            explicitIntent.putExtra("memberObj", member);
            context.startActivity(explicitIntent);
        } else if (v == websiteText) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(member.getWebsite()));
            context.startActivity(intent);
        }
    }

    private void bind(View dialogView) {
        outerLayout = dialogView.findViewById(R.id.outer_layout);
        averageRatingTitle = dialogView.findViewById(R.id.average_rating_title);
        averageRatingText = dialogView.findViewById(R.id.average_rating_text);
        lifetimeViewsTitle = dialogView.findViewById(R.id.lifetime_views_title);
        lifetimeViewsText = dialogView.findViewById(R.id.lifetime_views_text);
        submissionDateTitle = dialogView.findViewById(R.id.submission_date_title);
        submissionDateText = dialogView.findViewById(R.id.submission_date_text);
        submittedByTitle = dialogView.findViewById(R.id.submitted_by_title);
        submittedByText = dialogView.findViewById(R.id.submitted_by_text);
        viewsTodayTitle = dialogView.findViewById(R.id.views_today_title);
        viewsTodayText = dialogView.findViewById(R.id.views_today_text);
        totalCheatsByMemberTitle = dialogView.findViewById(R.id.total_cheats_by_member_title);
        totalCheatsByMemberText = dialogView.findViewById(R.id.total_cheats_by_member_text);
        showAllCheatsByMember = dialogView.findViewById(R.id.show_all_cheats_by_member);
        websiteTitle = dialogView.findViewById(R.id.website_title);
        websiteText = dialogView.findViewById(R.id.website_text);
        divider3 = dialogView.findViewById(R.id.divider_3);
        divider4 = dialogView.findViewById(R.id.divider_4);
        divider5 = dialogView.findViewById(R.id.divider_5);
        divider6 = dialogView.findViewById(R.id.divider_6);
    }
}
