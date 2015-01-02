package com.cheatdatabase.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.R;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.businessobjects.WelcomeMessage;

public class NewsFragment extends Fragment {

	private ProgressBar progressBar;
	private ImageView reloadView;
	private TextView welcomeTitle;
	private TextView createdTitle;
	private TextView welcomeText;
	public WelcomeMessage wm;
	private Typeface latoFontBold;
	private Typeface latoFontLight;
	private Activity parentActivity;

	public static final String ARG_SECTION_NUMBER = "section_number";

	public static final String IMAGE_RESOURCE_ID = "iconResourceID";
	public static final String ITEM_NAME = "itemName";

	public NewsFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_welcome_view, container, false);
		parentActivity = getActivity();

		latoFontBold = Tools.getFont(parentActivity.getAssets(), "Lato-Bold.ttf");
		latoFontLight = Tools.getFont(parentActivity.getAssets(), "Lato-Light.ttf");

		progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
		progressBar.setVisibility(View.VISIBLE);
		createdTitle = (TextView) rootView.findViewById(R.id.text_created);
		createdTitle.setTypeface(latoFontLight);
		welcomeTitle = (TextView) rootView.findViewById(R.id.text_welcome_title);
		welcomeTitle.setTypeface(latoFontBold);
		welcomeText = (TextView) rootView.findViewById(R.id.text_welcome_text);
		welcomeText.setTypeface(latoFontLight);
		// mAdView = (MoPubView) rootView.findViewById(R.id.adview);
		// Tools.getAds(mAdView, parentActivity);

		reloadView = (ImageView) rootView.findViewById(R.id.reload);
		reloadView.setVisibility(View.INVISIBLE);
		reloadView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				reloadView.setVisibility(View.INVISIBLE);
				progressBar.setVisibility(View.VISIBLE);
				new GetWelcomeMessageTask().execute();
			}
		});

		new GetWelcomeMessageTask().execute(parentActivity);

		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.clear();
	}

	private class GetWelcomeMessageTask extends AsyncTask<Activity, Void, WelcomeMessage> {

		@Override
		protected WelcomeMessage doInBackground(Activity... params) {

			if (Reachability.reachability.isReachable) {
				wm = Webservice.getWelcomeMessage();
			}

			return wm;
		}

		@Override
		protected void onPostExecute(WelcomeMessage result) {
			super.onPostExecute(result);

			// TODO local store message for a few days
			if (wm != null) {
				welcomeTitle.setText(wm.getTitle());
				welcomeTitle.setVisibility(View.VISIBLE);

				createdTitle.setText(Tools.convertDateToLocaleDateFormat(wm.getCreated(), parentActivity));

				Spanned spanned = Html.fromHtml(wm.getWelcomeMessage());
				welcomeText.setText(spanned);
				welcomeText.setVisibility(View.VISIBLE);
				Linkify.addLinks(welcomeText, Linkify.ALL);
				reloadView.setVisibility(View.GONE);
			} else {
				Toast.makeText(parentActivity, R.string.no_internet, Toast.LENGTH_SHORT).show();
				reloadView.setVisibility(View.VISIBLE);
			}
			progressBar.setVisibility(View.GONE);

		}
	}

}