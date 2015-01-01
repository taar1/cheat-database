package com.cheatdatabase.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cheatdatabase.R;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;

public class SubmitCheatFragment extends Fragment {

	ImageView ivIcon;
	TextView tvItemName;
	private Activity ca;
	private Typeface latoFontLight;
	private Typeface latoFontBold;
	private TextView title;
	private TextView subtitle;
	private Button searchButton;

	public static final String IMAGE_RESOURCE_ID = "iconResourceID";
	public static final String ITEM_NAME = "itemName";

	public SubmitCheatFragment() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ca = getActivity();

		Reachability.registerReachability(ca.getApplicationContext());

		latoFontLight = Tools.getFont(ca.getAssets(), "Lato-Light.ttf");
		latoFontBold = Tools.getFont(ca.getAssets(), "Lato-Bold.ttf");

		ca.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		// Update action bar menu items?
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.activity_submit_cheat_game_selection, container, false);

		title = (TextView) rootView.findViewById(R.id.title);
		title.setTypeface(latoFontBold);
		subtitle = (TextView) rootView.findViewById(R.id.subtitle);
		subtitle.setTypeface(latoFontLight);

		searchButton = (Button) rootView.findViewById(R.id.search_button);
		searchButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ca.onSearchRequested();
			}

		});
		searchButton.setTypeface(latoFontBold);

		return rootView;
	}

}