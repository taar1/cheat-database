package com.cheatdatabase;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazon.device.ads.Ad;
import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdListener;
import com.amazon.device.ads.AdProperties;
import com.amazon.device.ads.AdRegistration;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.SystemPlatform;
import com.cheatdatabase.helpers.ActionBarListActivity;
import com.cheatdatabase.helpers.Group;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.google.analytics.tracking.android.Tracker;
import com.mopub.mobileads.MoPubView;

import java.util.ArrayList;

public class GamesBySystemActivity extends ActionBarListActivity implements AdListener {

	SparseArray<Group> groups = new SparseArray<Group>();

	protected SystemPlatform systemObj;

	private Tracker tracker;

	private ProgressDialog gameListProgressDialog = null;

	private GameListAdapter gameListAdapter;

	private ArrayList<Game> gameArrayList = new ArrayList<Game>();

	private Game[] gamesFound;


	private Typeface latoFontLight;
	private Typeface latoFontRegular;

	private ImageView reloadView;

	private SharedPreferences settings;
	private Editor editor;

	private ViewGroup adViewContainer;
	private com.amazon.device.ads.AdLayout amazonAdView;
	private boolean amazonAdEnabled;
	private MoPubView mAdView;

	private static final String SCREEN_LABEL = "Game List By System ID Screen";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gamelist);
		Reachability.registerReachability(this.getApplicationContext());

		//Tools.styleActionbar(this);
		//getActionBar().setHomeButtonEnabled(true);

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

		latoFontLight = Tools.getFont(getAssets(), "Lato-Light.ttf");
		latoFontRegular = Tools.getFont(getAssets(), "Lato-Regular.ttf");

		handleIntent(getIntent());

		reloadView = (ImageView) findViewById(R.id.reload);
		if (Reachability.reachability.isReachable) {
			startGameListAdapter();
		} else {
			reloadView.setVisibility(View.VISIBLE);
			reloadView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (Reachability.reachability.isReachable) {
						startGameListAdapter();
					} else {
						Toast.makeText(GamesBySystemActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
					}
				}
			});
			Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
		}

		// mAdView = (MoPubView) findViewById(R.id.adview);
		// Tools.getAds(mAdView, this);

		// APP FLOOD USAGE TESTING
		// AppFlood.initialize(this, Konstanten.APPFLOOD_APP_KEY,
		// Konstanten.APPFLOOD_SECRET_KEY, AppFlood.AD_ALL);
		// AppFlood.showFullScreen(this);
		// AppFlood.showInterstitial(this);
		// AppFlood.showList(this, 1);
	}

	private void startGameListAdapter() {
		reloadView.setVisibility(View.GONE);
		gameListAdapter = new GameListAdapter(this, R.layout.activity_gamelist, gameArrayList);
		setListAdapter(gameListAdapter);

		gameListProgressDialog = ProgressDialog.show(GamesBySystemActivity.this, getString(R.string.please_wait) + "...", getString(R.string.retrieving_data) + "...", true);

		new Thread(new Runnable() {
			@Override
			public void run() {
				getGames();
			}
		}).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Search
		getMenuInflater().inflate(R.menu.search_menu, menu);

		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

		return super.onCreateOptionsMenu(menu);
	}

	private void handleIntent(final Intent intent) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				systemObj = (SystemPlatform) intent.getSerializableExtra("systemObj");

				// int titleId =
				// getResources().getIdentifier("action_bar_title", "id",
				// "android");
				// TextView yourTextView = (TextView) findViewById(titleId);
				// yourTextView.setTextColor(getResources().getColor(R.color.white));
				// yourTextView.setTypeface(Tools.getFont(getAssets(),
				// "Lato-Bold.ttf"));


                //getActionBar().setTitle(systemObj.getSystemName());

				Tools.initGA(GamesBySystemActivity.this, tracker, SCREEN_LABEL, "Game List", systemObj.getSystemName());
			}
		}).start();

	}

	private void getGames() {
		gamesFound = Webservice.getGameListBySystemId(systemObj.getSystemId());
		gameArrayList = new ArrayList<Game>();
		for (int i = 0; i < gamesFound.length; i++) {
			gameArrayList.add(gamesFound[i]);
		}
		runOnUiThread(runFillListView);

	}

	private Runnable runFillListView = new Runnable() {

		@Override
		public void run() {

			try {
				if (gameArrayList != null && gameArrayList.size() > 0) {
					gameListAdapter.notifyDataSetChanged();
					for (int i = 0; i < gamesFound.length; i++) {
						gameListAdapter.add(gamesFound[i]);
					}
					gameListProgressDialog.dismiss();
					gameListAdapter.notifyDataSetChanged();
				} else {
					error();
				}
			} catch (Exception e) { 
				error();
			}

			// FIXME wenn keine internetverbindung muss der Fehler
			// abgefangen werden...
			// gameListProgressDialog =
			// ProgressDialog.show(GamesBySystemActivity.this,
			// getString(R.string.please_wait) + "...",
			// getString(R.string.retrieving_data) + "...", true);
		}

	};

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		if (Reachability.reachability.isReachable) {
			Game tmpGame = new Game();
			tmpGame.setGameId(gamesFound[position].getGameId());
			tmpGame.setGameName(gamesFound[position].getGameName());
			tmpGame.setSystemName(systemObj.getSystemName());
			tmpGame.setSystemId(systemObj.getSystemId());

			Intent explicitIntent = new Intent(this, CheatListActivity.class);
			explicitIntent.putExtra("gameObj", tmpGame); 
			startActivity(explicitIntent);
		} else {
			Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
		}
	}

	private void error() {
		Log.e("error()", "caught error: " + getPackageName() + "/" + getTitle());
		new AlertDialog.Builder(GamesBySystemActivity.this).setIcon(R.drawable.ic_action_warning).setTitle(getString(R.string.err)).setMessage(R.string.err_data_not_accessible).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				finish();
			}
		}).create().show();
	}

	private class GameListAdapter extends ArrayAdapter<Game> {

		private ArrayList<Game> gameArrayList;

		public GameListAdapter(Context context, int textViewResourceId, ArrayList<Game> gameArrayList) {
			super(context, textViewResourceId, gameArrayList);
			this.gameArrayList = gameArrayList;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.listrow_gamelist, null);
			}

			Game singleGame = gameArrayList.get(position);

			if (singleGame != null) {
				TextView tt = (TextView) v.findViewById(R.id.cheat_title);
				tt.setTypeface(latoFontRegular);
				TextView bt = (TextView) v.findViewById(R.id.cheats_count);
				bt.setTypeface(latoFontLight);
				if (tt != null) {
					tt.setText(singleGame.getGameName());
				}
				if (bt != null) {
					if (singleGame.getCheatsCount() == 1) {
						bt.setText(singleGame.getCheatsCount() + " Cheat");
					} else {
						bt.setText(singleGame.getCheatsCount() + " Cheats");
					}
				}
			}
			return v;
		}
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
