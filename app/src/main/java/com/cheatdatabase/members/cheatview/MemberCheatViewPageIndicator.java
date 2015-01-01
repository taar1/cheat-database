package com.cheatdatabase.members.cheatview;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.cheatdatabase.CheatForumActivity;
import com.cheatdatabase.LoginActivity;
import com.cheatdatabase.R;
import com.cheatdatabase.SubmitCheatActivity;
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
import com.cheatdatabase.pojo.Cheat;
import com.cheatdatabase.pojo.Game;
import com.cheatdatabase.pojo.Member;
import com.google.analytics.tracking.android.Tracker;
import com.google.gson.Gson;
import com.mopub.mobileads.MoPubView;
import com.viewpagerindicator.UnderlinePageIndicator;

/**
 * Horizontal sliding through cheats submitted by member.
 * 
 * @version 1.0
 * @author Dominik Erbsland
 * 
 */
public class MemberCheatViewPageIndicator extends FragmentActivity implements ReportCheatDialogListener, RateCheatDialogListener {

	private Intent intent;

	private View viewLayout;
	private int pageSelected;

	public ArrayList<String[]> al_images;

	private Cheat[] cheatObj;
	private Cheat visibleCheat;
	private Game gameObj;

	private MoPubView mAdView;

	private SharedPreferences settings;
	private Editor editor;

	private Member member;

	public AlertDialog.Builder builder;
	public AlertDialog alert;

	private MemberCheatViewFragmentAdapter mAdapter;
	private ViewPager mPager;
	private UnderlinePageIndicator mIndicator;
	private Tracker tracker;

	private int activePage;

	private static final String SCREEN_LABEL = "Member CheatView PI Screen";

	private ConnectivityManager cm;

	private String cheatShareTitle;
	private String cheatShareBody;

	private ShareActionProvider mShare;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Reachability.registerReachability(this.getApplicationContext());

		LayoutInflater inflater = LayoutInflater.from(this);
		intent = getIntent();

		viewLayout = inflater.inflate(R.layout.activity_cheatview_pager, null);
		setContentView(viewLayout);

		settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
		editor = settings.edit();

		try {
			cheatObj = new Gson().fromJson(settings.getString(Konstanten.PREFERENCES_TEMP_CHEAT_ARRAY_OBJECT_VIEW, null), Cheat[].class);
			if (cheatObj == null) {
				cheatObj = new Gson().fromJson(intent.getStringExtra("cheatsObj"), Cheat[].class);
			}
			pageSelected = intent.getIntExtra("selectedPage", 0);
			activePage = pageSelected;

			visibleCheat = cheatObj[pageSelected];
			gameObj = new Game();
			gameObj.setCheat(visibleCheat);
			gameObj.setGameId(visibleCheat.getGameId());
			gameObj.setGameName(visibleCheat.getGameName());
			gameObj.setSystemId(visibleCheat.getSystemId());
			gameObj.setSystemName(visibleCheat.getSystemName());

			member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);

			Tools.styleActionbar(this);
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setTitle(visibleCheat.getGameName());
			getActionBar().setSubtitle(visibleCheat.getSystemName());

			Tools.initGA(this, tracker, SCREEN_LABEL, visibleCheat.getGameName() + " (" + visibleCheat.getSystemName() + ")", visibleCheat.getCheatTitle());
			mAdView = (MoPubView) viewLayout.findViewById(R.id.adview);
			Tools.getAds(mAdView, this);
			initialisePaging();
		} catch (Exception e) {
			Log.e(MemberCheatViewPageIndicator.class.getName(), e.getMessage());
		}

	}

	private void initialisePaging() {

		String[] cheatTitles = new String[cheatObj.length];
		for (int i = 0; i < cheatObj.length; i++) {
			cheatTitles[i] = cheatObj[i].getCheatTitle();
		}

		try {
			mAdapter = new MemberCheatViewFragmentAdapter(getSupportFragmentManager(), cheatObj, cheatTitles);

			mPager = (ViewPager) viewLayout.findViewById(R.id.pager);
			mPager.setAdapter(mAdapter);

			mIndicator = (UnderlinePageIndicator) viewLayout.findViewById(R.id.indicator);
//			mIndicator.setSelectedColor(color.page_indicator);
			mIndicator.setViewPager(mPager);
			mIndicator.notifyDataSetChanged();
			mIndicator.setCurrentItem(pageSelected);

			mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
				@Override
				public void onPageSelected(int position) {

					// Save selected page
					editor.putInt(Konstanten.PREFERENCES_PAGE_SELECTED, position);
					editor.commit();

					activePage = position;

					try {
						visibleCheat = cheatObj[position];
						setShareText(visibleCheat);
						invalidateOptionsMenu();

						gameObj = new Game();
						gameObj.setCheat(visibleCheat);
						gameObj.setGameId(visibleCheat.getGameId());
						gameObj.setGameName(visibleCheat.getGameName());
						gameObj.setSystemId(visibleCheat.getSystemId());
						gameObj.setSystemName(visibleCheat.getSystemName());

						getActionBar().setTitle(visibleCheat.getGameName());
						getActionBar().setSubtitle(visibleCheat.getSystemName());
					} catch (Exception e) {
						Toast.makeText(MemberCheatViewPageIndicator.this, R.string.err_somethings_wrong, Toast.LENGTH_SHORT).show();
					}
				}

				@Override
				public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				}

				@Override
				public void onPageScrollStateChanged(int state) {
				}
			});

		} catch (Exception e2) {
			Log.e("initialisePaging() ERROR: ", getPackageName() + "/" + getTitle() + "... " + e2.getMessage());
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
					Toast.makeText(MemberCheatViewPageIndicator.this, R.string.register_thanks, Toast.LENGTH_LONG).show();
				} else if ((member != null) && intentReturnCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
					Toast.makeText(MemberCheatViewPageIndicator.this, R.string.login_ok, Toast.LENGTH_LONG).show();
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if ((visibleCheat != null) && (visibleCheat.getMemberRating() > 0)) {
			getMenuInflater().inflate(R.menu.handset_cheatview_rating_on_menu, menu);
		} else {
			getMenuInflater().inflate(R.menu.handset_cheatview_rating_off_menu, menu);
		}

		if (member != null) {
			getMenuInflater().inflate(R.menu.signout_menu, menu);
		} else {
			getMenuInflater().inflate(R.menu.signin_menu, menu);
		}

		// Locate MenuItem with ShareActionProvider
		MenuItem item = menu.findItem(R.id.action_share);

		// Sharing
		mShare = (ShareActionProvider) item.getActionProvider();
		setShareText(visibleCheat);

		// Search
		getMenuInflater().inflate(R.menu.search_menu, menu);

		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if ((visibleCheat != null) && (visibleCheat.getMemberRating() > 0)) {
			getMenuInflater().inflate(R.menu.handset_cheatview_rating_on_menu, menu);
		} else {
			getMenuInflater().inflate(R.menu.handset_cheatview_rating_off_menu, menu);
		}

		if (member != null) {
			getMenuInflater().inflate(R.menu.signout_menu, menu);
		} else {
			getMenuInflater().inflate(R.menu.signin_menu, menu);
		}

		// Locate MenuItem with ShareActionProvider
		MenuItem item = menu.findItem(R.id.action_share);

		// Sharing
		mShare = (ShareActionProvider) item.getActionProvider();
		setShareText(visibleCheat);

		// Search
		getMenuInflater().inflate(R.menu.search_menu, menu);

		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		return super.onPrepareOptionsMenu(menu);
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
		case R.id.action_submit_cheat:
			Intent explicitIntent = new Intent(MemberCheatViewPageIndicator.this, SubmitCheatActivity.class);
			explicitIntent.putExtra("gameObj", gameObj);
			startActivity(explicitIntent);
			return true;
		case R.id.action_rate:
			showRatingDialog();
			return true;
		case R.id.action_forum:
			Intent forumIntent = new Intent(MemberCheatViewPageIndicator.this, CheatForumActivity.class);
			forumIntent.putExtra("cheatObj", visibleCheat);
			startActivity(forumIntent);
			return true;
		case R.id.action_add_to_favorites:
			Toast.makeText(MemberCheatViewPageIndicator.this, R.string.favorite_adding, Toast.LENGTH_SHORT).show();
			Helper.addFavorite(MemberCheatViewPageIndicator.this, visibleCheat);
			return true;
		case R.id.action_report:
			showReportDialog();
			return true;
		case R.id.action_metainfo:
			CheatMetaDialog cmDialog = new CheatMetaDialog(MemberCheatViewPageIndicator.this, visibleCheat);
			cmDialog.show();
			return true;
		case R.id.action_login:
			Intent loginIntent = new Intent(MemberCheatViewPageIndicator.this, LoginActivity.class);
			startActivityForResult(loginIntent, Konstanten.LOGIN_REGISTER_OK_RETURN_CODE);
			return true;
		case R.id.action_logout:
			member = null;
			Tools.logout(MemberCheatViewPageIndicator.this, editor);
			invalidateOptionsMenu();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
		invalidateOptionsMenu();
	}

	@Override
	protected void onDestroy() {
		if (mAdView != null) {
			mAdView.destroy();
		}
		super.onDestroy();
	}

	public ConnectivityManager getConnectivityManager() {
		if (cm == null) {
			cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		}
		return cm;
	}

	public void showReportDialog() {
		if ((member == null) || (member.getMid() == 0)) {
			Toast.makeText(MemberCheatViewPageIndicator.this, R.string.error_login_required, Toast.LENGTH_SHORT).show();
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
			args.putSerializable("cheatObj", visibleCheat);

			FragmentManager fm = getSupportFragmentManager();
			RateCheatDialog ratingCheatDialog = new RateCheatDialog();
			ratingCheatDialog.setArguments(args);
			ratingCheatDialog.show(fm, "fragment_rating_cheat");
		}
	}

	@Override
	public void onFinishRateCheatDialog(int selectedRating) {
		visibleCheat.setMemberRating(selectedRating);
		cheatObj[activePage].setMemberRating(selectedRating);
		invalidateOptionsMenu();
		Toast.makeText(this, R.string.rating_inserted, Toast.LENGTH_SHORT).show();
	}

	private class ReportCheatTask extends AsyncTask<String, Boolean, Boolean> {

		@Override
		protected Boolean doInBackground(String... reason) {

			try {
				Webservice.reportCheat(visibleCheat.getCheatId(), member.getMid(), reason[0]);
				return true;
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean success) {
			if (success) {
				Toast.makeText(MemberCheatViewPageIndicator.this, R.string.thanks_for_reporting, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(MemberCheatViewPageIndicator.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void setShareText(Cheat visibleCheat) {
		cheatShareTitle = String.format(getString(R.string.share_email_subject), visibleCheat.getGameName());
		cheatShareBody = visibleCheat.getGameName() + " (" + visibleCheat.getSystemName() + "): " + visibleCheat.getCheatTitle() + "\n";
		cheatShareBody += Konstanten.BASE_URL + "display/switch.php?id=" + visibleCheat.getCheatId() + "\n\n";

		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_SUBJECT, cheatShareTitle);
		shareIntent.putExtra(Intent.EXTRA_TEXT, cheatShareBody);
		setShareIntent(shareIntent);
	}

	public void setRating(int position, float rating) {
		cheatObj[position].setMemberRating(rating);
		invalidateOptionsMenu();
	}
}