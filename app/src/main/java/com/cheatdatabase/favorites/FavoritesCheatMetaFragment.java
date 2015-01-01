package com.cheatdatabase.favorites;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cheatdatabase.CheatDetailTabletFragment;
import com.cheatdatabase.MemberCheatListActivity;
import com.cheatdatabase.R;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.pojo.Cheat;
import com.cheatdatabase.pojo.Member;
import com.google.gson.Gson;

@SuppressLint("SimpleDateFormat")
public class FavoritesCheatMetaFragment extends Fragment implements OnClickListener {

	public static final String ARG_ITEM_ID = "item_id";

	private Cheat cheatObj;
	private Member submittingMember;

	private TextView textCheatTitle;
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

	private ImageButton btnRateCheat;
	private ImageButton btnMetaInfo;
	private ImageButton btnForum;
	private ImageButton btnReport;
	private ImageButton btnViewCheat;

	private LinearLayout llBuffer4;
	private LinearLayout llBuffer5;
	private LinearLayout llBuffer6;

	private View rootView;

	public AlertDialog.Builder builder;
	public AlertDialog alert;

	private FavoriteCheatListActivity ca;
	private FavoritesDetailsFragment favoritesDetailsFragment;
	private FavoritesCheatForumFragment favoritesCheatForumFragment;

	private Typeface latoFontLight;
	private Typeface latoFontBold;

	public FavoritesCheatMetaFragment() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ca = (FavoriteCheatListActivity) getActivity();
		Reachability.registerReachability(ca.getApplicationContext());

		Bundle element = this.getArguments();
		cheatObj = (Cheat) element.getSerializable("cheatObj");
		favoritesDetailsFragment = new Gson().fromJson(element.getString("favoritesDetailsFragment"), FavoritesDetailsFragment.class);
		favoritesCheatForumFragment = new Gson().fromJson(element.getString("favoritesCheatForumFragment"), FavoritesCheatForumFragment.class);

		latoFontLight = Tools.getFont(getActivity().getAssets(), "Lato-Light.ttf");
		latoFontBold = Tools.getFont(getActivity().getAssets(), "Lato-Bold.ttf");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_cheat_meta, container, false);

		textCheatTitle = (TextView) rootView.findViewById(R.id.text_cheat_title);
		textCheatTitle.setVisibility(View.VISIBLE);
		textCheatTitle.setTypeface(latoFontBold);
		textCheatTitle.setText(cheatObj.getCheatTitle());

		tvAverageRatingText = (TextView) rootView.findViewById(R.id.tvAverageRatingText);
		tvAverageRatingText.setTypeface(latoFontLight);

		tvTotalViewsText = (TextView) rootView.findViewById(R.id.tvLifetimeViewsText);
		tvTotalViewsText.setTypeface(latoFontLight);
		tvTotalViewsText.setOnClickListener(this);

		tvViewsTodayText = (TextView) rootView.findViewById(R.id.tvViewsTodayText);
		tvViewsTodayText.setTypeface(latoFontLight);
		tvViewsTodayText.setOnClickListener(this);

		tvTotalSubmissionCountMemberText = (TextView) rootView.findViewById(R.id.tvTotalSubmissionsMemberText);
		tvTotalSubmissionCountMemberText.setTypeface(latoFontLight);
		tvTotalSubmissionCountMemberText.setOnClickListener(this);

		tvTotalSubmissionShowAll = (TextView) rootView.findViewById(R.id.tvTotalSubmissionsShow);
		tvTotalSubmissionShowAll.setTypeface(latoFontLight);
		tvTotalSubmissionShowAll.setOnClickListener(this);

		tvSubmissionDateText = (TextView) rootView.findViewById(R.id.tvSubmissionDateText);
		tvSubmissionDateText.setTypeface(latoFontLight);
		tvSubmissionDateText.setOnClickListener(this);

		llRating = (LinearLayout) rootView.findViewById(R.id.llRating);
		llRating.setVisibility(View.GONE);
		llSubmittedBy = (LinearLayout) rootView.findViewById(R.id.llSubmittedBy);
		llSubmittedBy.setVisibility(View.GONE);
		llCountSubmissions = (LinearLayout) rootView.findViewById(R.id.llCountSubmissions);
		llCountSubmissions.setVisibility(View.GONE);
		llCountSubmissions2 = (LinearLayout) rootView.findViewById(R.id.llCountSubmissions2);
		llCountSubmissions2.setVisibility(View.GONE);
		llMemberHomepage = (LinearLayout) rootView.findViewById(R.id.llMemberHomepage);
		llMemberHomepage.setVisibility(View.GONE);

		btnViewCheat = (ImageButton) rootView.findViewById(R.id.btn_view_cheat);
		btnViewCheat.setOnClickListener(this);
		btnMetaInfo = (ImageButton) rootView.findViewById(R.id.btn_meta_info);
		btnMetaInfo.setOnClickListener(this);
		btnForum = (ImageButton) rootView.findViewById(R.id.btn_forum);
		btnForum.setOnClickListener(this);
		btnReport = (ImageButton) rootView.findViewById(R.id.btn_report);
		btnReport.setVisibility(View.GONE);
		btnRateCheat = (ImageButton) rootView.findViewById(R.id.btn_rate_cheat);
		btnRateCheat.setOnClickListener(this);
		if (cheatObj.getMemberRating() > 0) {
			highlightRatingIcon(true);
		}

		llBuffer4 = (LinearLayout) rootView.findViewById(R.id.llBuffer4);
		llBuffer4.setVisibility(View.GONE);
		llBuffer5 = (LinearLayout) rootView.findViewById(R.id.llBuffer5);
		llBuffer5.setVisibility(View.GONE);
		llBuffer6 = (LinearLayout) rootView.findViewById(R.id.llBuffer6);
		llBuffer6.setVisibility(View.GONE);

		try {
			if (cheatObj.getViewsTotal() == 0) {
				new MetaDataLoaderTask().execute(cheatObj);
			} else {
				fillWithContent();
			}
		} catch (NullPointerException e) {
			new MetaDataLoaderTask().execute(cheatObj);
		}

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

	private class MetaDataLoaderTask extends AsyncTask<Cheat, Void, Void> {

		@Override
		protected Void doInBackground(Cheat... tmpCheat) {
			Cheat metaCheat;
			try {
				metaCheat = Webservice.getCheatMetaById(tmpCheat[0].getCheatId());
				cheatObj.setViewsTotal(metaCheat.getViewsTotal());
				cheatObj.setViewsToday(metaCheat.getViewsToday());
				cheatObj.setAuthorName(metaCheat.getAuthor());
				cheatObj.setCreated(metaCheat.getCreatedDate());
				cheatObj.setRatingAverage(metaCheat.getRatingAverage());
				cheatObj.setVotes(metaCheat.getVotes());
				cheatObj.setMember(metaCheat.getSubmittingMember());

				submittingMember = cheatObj.getSubmittingMember();
			} catch (Exception e) {
				Log.e("MetaDataLoader EXCEPTION", e.getMessage() + "");
			} finally {
				metaCheat = null;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			fillWithContent();
		}
	}

	private void fillWithContent() {
		try {

			if (cheatObj.getVotes() > 0) {
				llRating.setVisibility(View.VISIBLE);
				llBuffer4.setVisibility(View.VISIBLE);

				String averageRatingText = String.format(getActivity().getString(R.string.meta_average_rating1, Math.round(cheatObj.getRatingAverage())));
				averageRatingText += " " + String.format(getActivity().getString(R.string.meta_average_rating2, String.valueOf(cheatObj.getVotes())));
				tvAverageRatingText.setText(averageRatingText);
			}

			tvTotalViewsText.setText(String.valueOf(cheatObj.getViewsTotal()));
			tvViewsTodayText.setText(String.valueOf(cheatObj.getViewsToday()));

			if ((submittingMember != null) && (submittingMember.getCheatSubmissionCount() > 0)) {
				tvTotalSubmissionCountMemberText.setText(String.valueOf(submittingMember.getCheatSubmissionCount()));
				tvTotalSubmissionShowAll.setText(" [" + ca.getString(R.string.meta_show_all_cheats) + "]");
			} else {
				llCountSubmissions.setVisibility(View.GONE);
				llCountSubmissions2.setVisibility(View.GONE);
				llBuffer6.setVisibility(View.GONE);
			}

			/*
			 * Bei Member-ID 1 werden Teile ausgeblendet
			 */
			if ((submittingMember == null) || (submittingMember.getMid() <= 1)) {
				llSubmittedBy.setVisibility(View.GONE);
				llCountSubmissions.setVisibility(View.GONE);
				llCountSubmissions2.setVisibility(View.GONE);
				llMemberHomepage.setVisibility(View.GONE);
				llBuffer5.setVisibility(View.GONE);
				llBuffer6.setVisibility(View.GONE);
			} else {
				llSubmittedBy.setVisibility(View.VISIBLE);
				llCountSubmissions.setVisibility(View.VISIBLE);
				llCountSubmissions2.setVisibility(View.VISIBLE);
				llBuffer5.setVisibility(View.VISIBLE);
				llBuffer6.setVisibility(View.VISIBLE);

				if (submittingMember.getWebsite().length() > 3) {
					llMemberHomepage.setVisibility(View.VISIBLE);
					tvMemberHomepageText = (TextView) rootView.findViewById(R.id.tvMemberHomepageText);
					tvMemberHomepageText.setText(submittingMember.getWebsite());
					tvMemberHomepageText.setOnClickListener(FavoritesCheatMetaFragment.this);
				} else {
					llMemberHomepage.setVisibility(View.GONE);
					llBuffer6.setVisibility(View.INVISIBLE);
				}
				tvAuthorText = (TextView) rootView.findViewById(R.id.tvSubmittedByText);
				tvMemberHomepageTitle = (TextView) rootView.findViewById(R.id.tvMemberHomepageTitle);
				if (submittingMember.getUsername().length() > 1) {
					tvAuthorText.setText(submittingMember.getUsername());
					tvMemberHomepageTitle.setText(submittingMember.getUsername() + "'s " + getActivity().getString(R.string.meta_member_homepage));
				}
				tvAuthorText.setOnClickListener(FavoritesCheatMetaFragment.this);
			}

			Date dateObj = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(cheatObj.getCreatedDate());

			java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity().getApplicationContext());
			String newDateStr = dateFormat.format(dateObj);
			tvSubmissionDateText.setText(newDateStr);
		} catch (Exception e) {
			Log.e("MetaDataLoader onPostExecute EXCEPTION", e.getMessage() + "");
		}
	}

	@Override
	public void onClick(View v) {
		Bundle arguments = new Bundle();
		arguments.putInt(CheatDetailTabletFragment.ARG_ITEM_ID, 1);
		arguments.putSerializable("cheatObj", cheatObj);
		arguments.putString("favoritesDetailsFragment", new Gson().toJson(favoritesDetailsFragment));
		arguments.putString("favoritesCheatForumFragment", new Gson().toJson(favoritesCheatForumFragment));
		arguments.putString("favoritesCheatMetaFragment", new Gson().toJson(FavoritesCheatMetaFragment.class));

		if (v == btnViewCheat) {
			Log.d("onClick", "btnViewCheat");
			favoritesDetailsFragment.setArguments(arguments);
			getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.favorite_detail_container, favoritesDetailsFragment).commit();
		} else if (v == btnMetaInfo) {
			Log.d("onClick", "btnMetaInfo");
			// do nothing
		} else if (v == btnForum) {
			Log.d("onClick", "btnForum");
			favoritesCheatForumFragment.setArguments(arguments);
			getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.favorite_detail_container, favoritesCheatForumFragment).commit();
		} else if (v == btnRateCheat) {
			Log.d("onClick", "btnRateCheat");
			ca.showRatingDialog();
		} else if (v == tvTotalSubmissionShowAll) {
			Intent explicitIntent = new Intent(ca, MemberCheatListActivity.class);
			explicitIntent.putExtra("memberObj", submittingMember);
			ca.startActivity(explicitIntent);
		} else if (v == tvMemberHomepageText) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(submittingMember.getWebsite()));
			ca.startActivity(intent);
		}

	}

}
