package com.cheatdatabase.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cheatdatabase.CheatsByMemberListActivity;
import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;

import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressLint("SimpleDateFormat")
public class CheatMetaDialog extends Dialog implements OnClickListener {

    private final Typeface latoFontBold;
    private final Typeface latoFontLight;
    private Context context;

    private Cheat cheat;
    private Member member;

    private TextView title_cheat_details;
    private TextView submission_date_title;
    private TextView lifetime_views_title;
    private TextView views_today_title;
    private TextView tvAverageRatingTitle;
    private TextView tvSubmittedByTitle;
    private TextView tvTotalSubmissionsMemberTitle;
    private TextView tvAverageRatingText;
    private TextView tvTotalViewsText;
    private TextView tvSubmissionDateText;
    private TextView tvAuthorText;
    private TextView tvViewsTodayText;
    private TextView tvTotalSubmissionCountMemberText;
    private TextView tvTotalSubmissionShowAll;
    private TextView tvMemberHomepageTitle;
    private TextView tvMemberHomepageText;
    private LinearLayout llSubmittedBy;
    private LinearLayout llCountSubmissions;
    private LinearLayout llCountSubmissions2;
    private LinearLayout llMemberHomepage;
    private LinearLayout llRating;
    private LinearLayout llBuffer3;
    private LinearLayout llBuffer4;
    private LinearLayout llBuffer5;
    private LinearLayout llBuffer6;

    public CheatMetaDialog(Context context, Cheat cheat) {
        super(context);
        this.context = context;
        this.cheat = cheat;

        latoFontBold = Tools.getFont(context.getAssets(), Konstanten.FONT_BOLD);
        latoFontLight = Tools.getFont(context.getAssets(), Konstanten.FONT_LIGHT);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_cheatview_meta_dialog);

        member = cheat.getSubmittingMember();

        new MetaDataLoader().execute(cheat);

        title_cheat_details = findViewById(R.id.title_cheat_details);
        title_cheat_details.setTypeface(latoFontBold);
        submission_date_title = findViewById(R.id.submission_date_title);
        submission_date_title.setTypeface(latoFontBold);
        lifetime_views_title = findViewById(R.id.lifetime_views_title);
        lifetime_views_title.setTypeface(latoFontBold);
        views_today_title = findViewById(R.id.views_today_title);
        views_today_title.setTypeface(latoFontBold);
        tvAverageRatingTitle = findViewById(R.id.tvAverageRatingTitle);
        tvAverageRatingTitle.setTypeface(latoFontBold);
        tvSubmittedByTitle = findViewById(R.id.tvSubmittedByTitle);
        tvSubmittedByTitle.setTypeface(latoFontBold);
        tvTotalSubmissionsMemberTitle = findViewById(R.id.tvTotalSubmissionsMemberTitle);
        tvTotalSubmissionsMemberTitle.setTypeface(latoFontBold);

        tvAverageRatingText = findViewById(R.id.tvAverageRatingText);
        tvAverageRatingText.setTypeface(latoFontLight);

        tvTotalViewsText = findViewById(R.id.lifetime_views_text);
        tvTotalViewsText.setOnClickListener(this);
        tvTotalViewsText.setTypeface(latoFontLight);

        tvViewsTodayText = findViewById(R.id.views_today_text);
        tvViewsTodayText.setOnClickListener(this);
        tvViewsTodayText.setTypeface(latoFontLight);

        tvTotalSubmissionCountMemberText = findViewById(R.id.tvTotalSubmissionsMemberText);
        tvTotalSubmissionCountMemberText.setOnClickListener(this);
        tvTotalSubmissionCountMemberText.setTypeface(latoFontLight);

        tvTotalSubmissionShowAll = findViewById(R.id.tvTotalSubmissionsShow);
        tvTotalSubmissionShowAll.setOnClickListener(this);
        tvTotalSubmissionShowAll.setTypeface(latoFontLight);

        tvSubmissionDateText = findViewById(R.id.submission_date_text);
        tvSubmissionDateText.setOnClickListener(this);
        tvSubmissionDateText.setTypeface(latoFontLight);

        llRating = findViewById(R.id.llRating);
        llRating.setVisibility(View.GONE);
        llSubmittedBy = findViewById(R.id.llSubmittedBy);
        llSubmittedBy.setVisibility(View.GONE);
        llCountSubmissions = findViewById(R.id.llCountSubmissions);
        llCountSubmissions.setVisibility(View.GONE);
        llCountSubmissions2 = findViewById(R.id.llCountSubmissions2);
        llCountSubmissions2.setVisibility(View.GONE);
        llMemberHomepage = findViewById(R.id.llMemberHomepage);
        llMemberHomepage.setVisibility(View.GONE);

        llBuffer3 = findViewById(R.id.llBuffer3);
        llBuffer3.setVisibility(View.GONE);
        llBuffer4 = findViewById(R.id.llBuffer4);
        llBuffer4.setVisibility(View.GONE);
        llBuffer5 = findViewById(R.id.llBuffer5);
        llBuffer5.setVisibility(View.GONE);
        llBuffer6 = findViewById(R.id.llBuffer6);
        llBuffer6.setVisibility(View.GONE);
    }

    private class MetaDataLoader extends AsyncTask<Cheat, Void, Void> {

        @Override
        protected Void doInBackground(Cheat... tmpCheat) {
            Cheat metaCheat;
            try {
                metaCheat = Webservice.getCheatMetaById(tmpCheat[0].getCheatId());
                cheat.setViewsTotal(metaCheat.getViewsTotal());
                cheat.setViewsToday(metaCheat.getViewsToday());
                cheat.setAuthorName(metaCheat.getAuthor());
                cheat.setCreated(metaCheat.getCreatedDate());
                cheat.setRatingAverage(metaCheat.getRatingAverage());
                cheat.setVotes(metaCheat.getVotes());
                cheat.setMember(metaCheat.getSubmittingMember());
            } catch (Exception e) {
                // do nothing
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... progress) {
        }

        @Override
        protected void onPostExecute(Void result) {
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
                        tvMemberHomepageText = findViewById(R.id.tvMemberHomepageText);
                        tvMemberHomepageText.setText(member.getWebsite());
                        tvMemberHomepageText.setOnClickListener(CheatMetaDialog.this);
                        tvMemberHomepageText.setTypeface(latoFontLight);
                    } else {
                        llBuffer6.setVisibility(View.INVISIBLE);
                    }
                    tvAuthorText = findViewById(R.id.tvSubmittedByText);
                    tvAuthorText.setTypeface(latoFontLight);
                    tvMemberHomepageTitle = findViewById(R.id.tvMemberHomepageTitle);
                    tvMemberHomepageTitle.setTypeface(latoFontBold);
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
                e.getStackTrace();
            }
        }
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
