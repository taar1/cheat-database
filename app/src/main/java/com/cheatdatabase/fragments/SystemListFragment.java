package com.cheatdatabase.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.GamesBySystemActivity;
import com.cheatdatabase.R;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.businessobjects.SystemPlatform;
import com.google.analytics.tracking.android.Tracker;

public class SystemListFragment extends ListFragment {

	private Typeface latoFontBold;
	private Typeface latoFontLight;
	private Activity parentActivity;
	private String[] allSystems;
	private SystemPlatform[] allSystemPlatforms;

	private SystemListAdapter systemListAdapter;

	public static final String ARG_SECTION_NUMBER = "section_number";

	public static final String IMAGE_RESOURCE_ID = "iconResourceID";
	public static final String ITEM_NAME = "itemName";

	private Tracker tracker;
	protected SystemPlatform[] cntrs;
	private static final String SCREEN_LABEL = SystemListFragment.class.getName();
	private static final String GA_TITLE = "Systems ListView";

	public SystemListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		parentActivity = getActivity();
		// Reachability.registerReachability(ca.getApplicationContext());

		latoFontLight = Tools.getFont(parentActivity.getAssets(), "Lato-Light.ttf");
		latoFontBold = Tools.getFont(parentActivity.getAssets(), "Lato-Bold.ttf");

		// Update action bar menu items?
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_systemlist, container, false);

		Tools.initGA(parentActivity, tracker, SCREEN_LABEL, GA_TITLE, "");

		// Initialize ad views
		// AdRegistration.setAppKey(Konstanten.AMAZON_API_KEY);
		// amazonAdView = new com.amazon.device.ads.AdLayout(parentActivity,
		// com.amazon.device.ads.AdSize.SIZE_320x50);
		// amazonAdView.setListener(this);
		// // Initialize view container
		// adViewContainer = (ViewGroup) rootView.findViewById(R.id.adview);
		// amazonAdEnabled = true;
		// adViewContainer.addView(amazonAdView);
		// amazonAdView.loadAd(new com.amazon.device.ads.AdTargetingOptions());
		// mAdView = Tools.createMoPubAdView(parentActivity);

		// mAdView = (MoPubView) rootView.findViewById(R.id.adview);
		// Tools.getAds(mAdView, ca);

		getCounters();

		return rootView;
	}

	private void getCounters() {

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					cntrs = Webservice.countGamesAndCheatsBySystem();
					parentActivity.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							systemListAdapter = new SystemListAdapter(parentActivity, R.layout.fragment_systemlist);
							setListAdapter(systemListAdapter);
							parseSystemsLocally();
						}
					});

				} catch (Exception e) {
					Log.e("SystemListFragment", "count failed: " + e.getLocalizedMessage());
				}
			}
		}).start();
	}

	private void parseSystemsLocally() {
		allSystems = Tools.getSystemNames(parentActivity);
		allSystemPlatforms = new SystemPlatform[allSystems.length];

		for (int j = 0; j < allSystems.length; j++) {
			systemListAdapter.notifyDataSetChanged();
			SystemPlatform tmpSystem = Tools.getSystemObjectByName(parentActivity, allSystems[j]);
			systemListAdapter.add(tmpSystem);
			allSystemPlatforms[j] = tmpSystem;
		}
		systemListAdapter.notifyDataSetChanged();

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		if (Reachability.reachability.isReachable) {
			Intent explicitIntent = new Intent(parentActivity, GamesBySystemActivity.class);
			explicitIntent.putExtra("systemObj", Tools.getSystemObjectByName(parentActivity, allSystems[position]));
			startActivity(explicitIntent);
		} else {
			Toast.makeText(parentActivity, R.string.no_internet, Toast.LENGTH_SHORT).show();
		}
	}

	private class SystemListAdapter extends ArrayAdapter<SystemPlatform> {

		public SystemListAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final SystemPlatform system = allSystemPlatforms[position];

			View cView = convertView;
			LayoutInflater vi = (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			cView = vi.inflate(R.layout.systemlist_item, null);
			cView.setDrawingCacheEnabled(true);

			// This is a loop!
			try {
				ImageView avatarImageView = (ImageView) cView.findViewById(R.id.system_icon);
				avatarImageView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

					}

				});

				TextView systemName = (TextView) cView.findViewById(R.id.system_name);
				systemName.setText(String.valueOf(system.getSystemName()));
				systemName.setTypeface(latoFontBold);

				TextView subTitle = (TextView) cView.findViewById(R.id.subtitle);
				subTitle.setTypeface(latoFontLight);

				for (int j = 0; j < cntrs.length; j++) {
					SystemPlatform tmpSys = cntrs[j];
					if (system.getSystemName().equalsIgnoreCase(tmpSys.getSystemName())) {
						subTitle.setText(tmpSys.getGameCount() + " Games");
						// subTitle.setText(tmpSys.getCheatCount() +
						// " cheats in " + tmpSys.getGameCount() + " games");
						break;
					}
				}

			} catch (Exception e) {
				Log.e("SystemListFragment.getView ERROR:", "on position: " + position + ": " + e.getLocalizedMessage());
			}
			return cView;
		}

	}

	// @Override
	// public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	// super.onCreateOptionsMenu(menu, inflater);
	// // menu.clear();
	// }
	//
	// @Override
	// public void onPrepareOptionsMenu(Menu menu) {
	// super.onPrepareOptionsMenu(menu);
	// // menu.clear();
	// }
	//
	// @Override
	// public void onAdCollapsed(Ad arg0) {
	// // TODO Auto-generated method stub
	// }
	//
	// @Override
	// public void onAdDismissed(Ad arg0) {
	// // TODO Auto-generated method stub
	// }
	//
	// @Override
	// public void onAdExpanded(Ad arg0) {
	// // TODO Auto-generated method stub
	// }
	//
	// @Override
	// public void onAdFailedToLoad(Ad arg0, AdError arg1) {
	// Log.d("AMAZON ADS", "onAdFailedToLoad");
	// // Call AdMob SDK for backfill
	// if (amazonAdEnabled) {
	// amazonAdEnabled = false;
	// adViewContainer.removeView(amazonAdView);
	// adViewContainer.addView(mAdView);
	// }
	//
	// mAdView.loadAd();
	// }
	//
	// @Override
	// public void onAdLoaded(Ad arg0, AdProperties arg1) {
	// Log.d("AMAZON ADS", "onAdLoaded");
	// if (!amazonAdEnabled) {
	// amazonAdEnabled = true;
	// adViewContainer.removeView(mAdView);
	// adViewContainer.addView(amazonAdView);
	// }
	// }
	//
	// public void refreshAd() {
	// amazonAdView.loadAd(new com.amazon.device.ads.AdTargetingOptions());
	// }
}