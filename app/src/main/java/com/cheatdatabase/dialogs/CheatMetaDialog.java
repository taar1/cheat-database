package com.cheatdatabase.dialogs;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cheatdatabase.MemberCheatListActivity;
import com.cheatdatabase.R;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Member;

@SuppressLint("SimpleDateFormat")
public class CheatMetaDialog extends Dialog implements OnClickListener {

	private Context context;

	private Cheat cheat;
	private Member member;

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

		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_cheatview_meta);
		setTitle(R.string.meta_info);

		member = cheat.getSubmittingMember();

		new MetaDataLoader().execute(cheat);

		tvAverageRatingText = (TextView) findViewById(R.id.tvAverageRatingText);

		tvTotalViewsText = (TextView) findViewById(R.id.tvLifetimeViewsText);
		tvTotalViewsText.setOnClickListener(this);

		tvViewsTodayText = (TextView) findViewById(R.id.tvViewsTodayText);
		tvViewsTodayText.setOnClickListener(this);

		tvTotalSubmissionCountMemberText = (TextView) findViewById(R.id.tvTotalSubmissionsMemberText);
		tvTotalSubmissionCountMemberText.setOnClickListener(this);

		tvTotalSubmissionShowAll = (TextView) findViewById(R.id.tvTotalSubmissionsShow);
		tvTotalSubmissionShowAll.setOnClickListener(this);

		tvSubmissionDateText = (TextView) findViewById(R.id.tvSubmissionDateText);
		tvSubmissionDateText.setOnClickListener(this);

		llRating = (LinearLayout) findViewById(R.id.llRating);
		llRating.setVisibility(View.GONE);
		llSubmittedBy = (LinearLayout) findViewById(R.id.llSubmittedBy);
		llSubmittedBy.setVisibility(View.GONE);
		llCountSubmissions = (LinearLayout) findViewById(R.id.llCountSubmissions);
		llCountSubmissions.setVisibility(View.GONE);
		llCountSubmissions2 = (LinearLayout) findViewById(R.id.llCountSubmissions2);
		llCountSubmissions2.setVisibility(View.GONE);
		llMemberHomepage = (LinearLayout) findViewById(R.id.llMemberHomepage);
		llMemberHomepage.setVisibility(View.GONE);

		llBuffer3 = (LinearLayout) findViewById(R.id.llBuffer3);
		llBuffer3.setVisibility(View.GONE);
		llBuffer4 = (LinearLayout) findViewById(R.id.llBuffer4);
		llBuffer4.setVisibility(View.GONE);
		llBuffer5 = (LinearLayout) findViewById(R.id.llBuffer5);
		llBuffer5.setVisibility(View.GONE);
		llBuffer6 = (LinearLayout) findViewById(R.id.llBuffer6);
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
			} finally {
				metaCheat = null;
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

					String averageRatingText = String.format(context.getString(R.string.meta_average_rating1, Math.round(cheat.getRatingAverage())));
					averageRatingText += " " + String.format(context.getString(R.string.meta_average_rating2, String.valueOf(cheat.getVotes())));
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
						tvMemberHomepageText = (TextView) findViewById(R.id.tvMemberHomepageText);
						tvMemberHomepageText.setText(member.getWebsite());
						tvMemberHomepageText.setOnClickListener(CheatMetaDialog.this);
					} else {
						llBuffer6.setVisibility(View.INVISIBLE);
					}
					tvAuthorText = (TextView) findViewById(R.id.tvSubmittedByText);
					tvMemberHomepageTitle = (TextView) findViewById(R.id.tvMemberHomepageTitle);
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
				// do nothing
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v == tvTotalSubmissionShowAll) {
			Intent explicitIntent = new Intent(context, MemberCheatListActivity.class);
			explicitIntent.putExtra("memberObj", member);
			context.startActivity(explicitIntent);
		} else if (v == tvMemberHomepageText) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(member.getWebsite()));
			context.startActivity(intent);
		}
	}

}
