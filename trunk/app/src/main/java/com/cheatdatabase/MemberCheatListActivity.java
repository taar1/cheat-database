package com.cheatdatabase;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazon.device.ads.Ad;
import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdListener;
import com.amazon.device.ads.AdProperties;
import com.amazon.device.ads.AdRegistration;
import com.cheatdatabase.helpers.CheatDatabaseAdapter;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.members.cheatview.MemberCheatViewPageIndicator;
import com.cheatdatabase.pojo.Cheat;
import com.cheatdatabase.pojo.Member;
import com.google.gson.Gson;
import com.mopub.mobileads.MoPubView;

/**
 * Shows all cheats of one particular member.
 * 
 * @author Dominik
 * 
 */
@SuppressLint("NewApi")
public class MemberCheatListActivity extends ListActivity implements AdListener {

	private Cheat[] cheats;
	private Cheat selectedCheat;
	private Member memberToDisplayCheatsFrom;

	private ProgressDialog mProgressDialog = null;
	private CheatAdapter memberCheatsAdapter;
	// private Runnable viewOrders;

	private final int ADD_TO_FAVORITES = 0;

	// Lokale Settings
	private Typeface latoFontLight;
	private CheatDatabaseAdapter db;
	private MoPubView mAdView;
	private SharedPreferences settings;
	private Editor editor;
	private ImageView reloadView;

	private ViewGroup adViewContainer;
	private com.amazon.device.ads.AdLayout amazonAdView;
	private boolean amazonAdEnabled;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_member_cheatlist);
		Reachability.registerReachability(this.getApplicationContext());

		settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
		editor = settings.edit();

		registerForContextMenu(getListView());

		latoFontLight = Tools.getFont(getAssets(), "Lato-Light.ttf");

		memberToDisplayCheatsFrom = (Member) getIntent().getSerializableExtra("memberObj");

		Tools.styleActionbar(this);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle(getString(R.string.members_cheats_title, memberToDisplayCheatsFrom.getUsername()));

		// Custom background
		// LinearLayout ol = (LinearLayout) findViewById(R.id.outerLayout);
		// ol.setBackgroundResource(R.drawable.bg_top_members);

		reloadView = (ImageView) findViewById(R.id.reload);
		if (Reachability.reachability.isReachable) {
			getThemCheats();
		} else {
			reloadView.setVisibility(View.VISIBLE);
			reloadView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (Reachability.reachability.isReachable) {
						getThemCheats();
					} else {
						Toast.makeText(MemberCheatListActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
					}
				}
			});
			Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
		}

		// Initialize ad views
		AdRegistration.setAppKey(Konstanten.AMAZON_API_KEY);
		amazonAdView = new com.amazon.device.ads.AdLayout(this, com.amazon.device.ads.AdSize.SIZE_320x50);
		amazonAdView.setListener(this);
		// Initialize view container
		adViewContainer = (ViewGroup) findViewById(R.id.adview);
		amazonAdEnabled = true;
		adViewContainer.addView(amazonAdView);
		amazonAdView.loadAd(new com.amazon.device.ads.AdTargetingOptions());

		mAdView = Tools.createMoPubAdView(this);
		// mAdView = (MoPubView) findViewById(R.id.adview);
		// Tools.getAds(mAdView, this);
	}

	private void getThemCheats() {
		reloadView.setVisibility(View.GONE);
		memberCheatsAdapter = new CheatAdapter(this, R.layout.layout_list);
		setListAdapter(memberCheatsAdapter);
		mProgressDialog = ProgressDialog.show(MemberCheatListActivity.this, getString(R.string.please_wait) + "...", getString(R.string.retrieving_data) + "...", true);

		getCheats();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		selectedCheat = cheats[Integer.parseInt(String.valueOf(info.id))];
		menu.setHeaderTitle(R.string.context_menu_title);
		menu.add(0, ADD_TO_FAVORITES, 1, R.string.add_one_favorite);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Search
		// getMenuInflater().inflate(R.menu.search_menu, menu);
		//
		// // Associate searchable configuration with the SearchView
		// SearchManager searchManager = (SearchManager)
		// getSystemService(Context.SEARCH_SERVICE);
		// SearchView searchView = (SearchView)
		// menu.findItem(R.id.search).getActionView();
		// searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ADD_TO_FAVORITES:
			db = new CheatDatabaseAdapter(this);
			db.open();
			int retVal = db.insertFavorite(selectedCheat);

			if (retVal > 0) {
				Toast.makeText(MemberCheatListActivity.this, R.string.add_favorite_ok, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(MemberCheatListActivity.this, R.string.favorite_error, Toast.LENGTH_SHORT).show();
			}
			return true;
		}

		return super.onContextItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		if (Reachability.reachability.isReachable) {

			Intent explicitIntent = new Intent(MemberCheatListActivity.this, MemberCheatViewPageIndicator.class);

			// Delete Walkthrough texts (otherwise runs into a timeout)
			for (int i = 0; i < cheats.length; i++) {
				if (cheats[i].isWalkthroughFormat()) {
					cheats[i].setCheatText("");
				}
			}
			explicitIntent.putExtra("cheatsObj", new Gson().toJson(cheats));
			explicitIntent.putExtra("selectedPage", position);
			startActivity(explicitIntent);
		} else {
			Toast.makeText(MemberCheatListActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
		}
	}

	private void getCheats() {

		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					cheats = Webservice.getCheatsByMemberId(memberToDisplayCheatsFrom.getMid());
					new SaveToLocalStorageTask().execute(cheats);

					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							if (cheats != null && cheats.length > 0) {
								memberCheatsAdapter.notifyDataSetChanged();
								for (int i = 0; i < cheats.length; i++) {
									memberCheatsAdapter.add(cheats[i]);
								}
								mProgressDialog.dismiss();
								memberCheatsAdapter.notifyDataSetChanged();
							} else {
								error(R.string.err_data_not_accessible);
							}
						}
					});
				} catch (Exception e) {
					Log.e("MemberCheatList:getCheats()", "Webservice.getCheatList() == null");
					error(R.string.err_no_member_data);
				}

			}
		}).start();

	}

	private class SaveToLocalStorageTask extends AsyncTask<Cheat[], Void, Void> {

		@Override
		protected Void doInBackground(Cheat[]... params) {
			editor.putString(Konstanten.PREFERENCES_TEMP_CHEAT_ARRAY_OBJECT_VIEW, new Gson().toJson(params[0]));
			editor.commit();
			return null;
		}
	}

	private void error(int msg) {
		new AlertDialog.Builder(MemberCheatListActivity.this).setIcon(R.drawable.ic_action_warning).setTitle(getString(R.string.err)).setMessage(msg).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				finish();
			}
		}).create().show();
	}

	private class CheatAdapter extends ArrayAdapter<Cheat> {

		public CheatAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.member_cheatlist_item, null);
			}

			/*
			 * Dies ist bereis ein Loop der durch die ArrayList geht!
			 */
			try {
				Cheat cheat = cheats[position];

				TextView tvGameName = (TextView) v.findViewById(R.id.gamename);
				tvGameName.setText(cheat.getGameName() + " (" + cheat.getSystemName() + ")");

				TextView tvCheatTitle = (TextView) v.findViewById(R.id.cheattitle);
				tvCheatTitle.setTypeface(latoFontLight);
				if (tvCheatTitle != null) {
					tvCheatTitle.setText(cheat.getCheatTitle());
				}

				// TODO die paddings so anpassen, dass die flags immer
				// rechtsbuendig sind
				ImageView screenshotFlag = (ImageView) v.findViewById(R.id.ivMap);
				if (cheat.isScreenshots()) {
					screenshotFlag.setImageResource(R.drawable.flag_img);
				} else {
					screenshotFlag.setVisibility(View.GONE);
				}

				ImageView germanFlag = (ImageView) v.findViewById(R.id.ivFlag);
				if (cheat.getLanguageId() == Konstanten.GERMAN) {
					germanFlag.setImageResource(R.drawable.flag_german);
				} else {
					germanFlag.setVisibility(View.GONE);
				}
			} catch (Exception e) {
				Log.e("MemberCheatList.getView ERROR:", e.getMessage());
				error(R.string.err_no_member_data);
			}

			return v;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// http://developer.android.com/design/patterns/navigation.html#up-vs-back
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			// Dominik: This is actually not needed because I am saving the
			// selected Fragment ID in the local storage.
			Intent upIntent = NavUtils.getParentActivityIntent(this);
			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
				// This activity is NOT part of this app's task, so create a new
				// task when navigating up, with a synthesized back stack.
				// Add all of this activity's parents to the back stack
				TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
			} else {
				// This activity is part of this app's task, so simply
				// navigate up to the logical parent activity.
				NavUtils.navigateUpTo(this, upIntent);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onAdCollapsed(Ad arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onAdDismissed(Ad arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onAdExpanded(Ad arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onAdFailedToLoad(Ad arg0, AdError arg1) {
		Log.d("AMAZON ADS", "onAdFailedToLoad");
		// Call AdMob SDK for backfill
		if (amazonAdEnabled) {
			amazonAdEnabled = false;
			adViewContainer.removeView(amazonAdView);
			adViewContainer.addView(mAdView);
		}

		mAdView.loadAd();
	}

	@Override
	public void onAdLoaded(Ad arg0, AdProperties arg1) {
		Log.d("AMAZON ADS", "onAdLoaded");
		if (!amazonAdEnabled) {
			amazonAdEnabled = true;
			adViewContainer.removeView(mAdView);
			adViewContainer.addView(amazonAdView);
		}
	}

	public void refreshAd() {
		amazonAdView.loadAd(new com.amazon.device.ads.AdTargetingOptions());
	}
}
