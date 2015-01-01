package com.cheatdatabase.favorites;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cheatdatabase.R;
import com.cheatdatabase.helpers.CheatDatabaseAdapter;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.pojo.Cheat;
import com.cheatdatabase.pojo.Game;
import com.cheatdatabase.pojo.Member;
import com.google.gson.Gson;

/**
 * A list fragment representing a list of Favorites. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link FavoriteCheatDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the
 * {@link com.cheatdatabase.favorites.FavoriteCheatListFragment.ElementsListClickHandler} interface.
 */
public class FavoriteCheatListFragment extends ListFragment {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private ElementsListClickHandler handler = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	private FavoriteCheatListActivity ca;

	private CheatDatabaseAdapter db;

	private SharedPreferences settings;

	private Editor editor;

	private Intent intent;

	private Game gameObj;

	private ArrayList<Cheat> cheatsArray;

//	private CheatAdapter mAdapter;
	private ArrayList<Cheat> cheatsArrayList = new ArrayList<Cheat>();

	private Cheat[] cheats;

	private ProgressDialog mProgressDialog;

	private Typeface latoFontRegular;

	private Member member;
	private CheatAdapter cheatAdapter;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface ElementsListClickHandler {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(int id);
	}

	/**
	 * A dummy implementation of the {@link com.cheatdatabase.favorites.FavoriteCheatListFragment.ElementsListClickHandler} interface
	 * that does nothing. Used only when this fragment is not attached to an
	 * activity.
	 */
	private static ElementsListClickHandler sDummyCallbacks = new ElementsListClickHandler() {
		@Override
		public void onItemSelected(int id) {
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public FavoriteCheatListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ca = (FavoriteCheatListActivity) getActivity();
		latoFontRegular = Tools.getFont(getActivity().getAssets(), "Lato-Regular.ttf");

		settings = ca.getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);

		db = new CheatDatabaseAdapter(ca);
		db.open();

		if (member == null) {
			member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
		}

		gameObj = (Game) ca.getIntent().getSerializableExtra("gameObj");
		if (gameObj == null) {
			new GetCheatsTask().execute(new Game());
		} else {
			new GetCheatsTask().execute(gameObj);
		}

		// db = new CheatDatabaseAdapter(ca);
		// db.open();

		// intent = ca.getIntent();
		// // Game Object without the Cheat contents
		// gameObj = (Game) intent.getSerializableExtra("gameObj");
		//
		// ActionBar actionBar = ca.getActionBar();
		// actionBar.setDisplayHomeAsUpEnabled(true);
		// actionBar.setTitle(gameObj.getGameName());
		// actionBar.setSubtitle(gameObj.getSystemName());

		// TODO: replace with a real list adapter.
		// setListAdapter(new
		// ArrayAdapter<DummyContent.DummyItem>(getActivity(),
		// android.R.layout.simple_list_item_activated_1, android.R.id.text1,
		// DummyContent.ITEMS));

	}

	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	//
	// View rootView = inflater.inflate(R.layout.activity_favorite_list,
	// container, false);
	//
	// // ca = getActivity();
	// // latoFontRegular = Tools.getFont(getActivity().getAssets(),
	// // "Lato-Regular.ttf");
	//
	// // db = new CheatDatabaseAdapter(ca);
	// // db.open();
	//
	// // settings = ca.getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
	// // editor = settings.edit();
	//
	// // intent = ca.getIntent();
	// // // Game Object without the Cheat contents
	// // gameObj = (Game) intent.getSerializableExtra("gameObj");
	// //
	// // ActionBar actionBar = ca.getActionBar();
	// // actionBar.setDisplayHomeAsUpEnabled(true);
	// // actionBar.setTitle(gameObj.getGameName());
	// // actionBar.setSubtitle(gameObj.getSystemName());
	// //
	//
	// // db = new CheatDatabaseAdapter(ca);
	// // db.open();
	// // cheats = db.getAllFavoritedCheatsByGame(gameObj.getGameId());
	//
	// // populateList();
	//
	// return rootView;
	// }

	// @Override
	// public void onActivityCreated(Bundle savedInstanceState) {
	// super.onActivityCreated(savedInstanceState);
	//
	// // Bundle elements = this.getArguments();
	// // // cheats = (Cheat[]) elements.getSerializable("cheats");
	// // gameObj = (Game) elements.getSerializable("gameObj");
	//
	// // ActionBar actionBar = ca.getActionBar();
	// // actionBar.setDisplayHomeAsUpEnabled(true);
	// // actionBar.setTitle(gameObj.getGameName());
	// // actionBar.setSubtitle(gameObj.getSystemName());
	//
	// populateList();
	//
	// }

//	private void populateList() {
//
//		mProgressDialog = ProgressDialog.show(ca, getString(R.string.please_wait) + "...", getString(R.string.retrieving_data) + "...", true);
//
//		cheatsArray = new ArrayList<Cheat>();
//		mAdapter = new CheatAdapter(ca, R.layout.cheatlist_item, cheatsArray);
//		setListAdapter(mAdapter);
//
//		db = new CheatDatabaseAdapter(ca);
//		db.open();
//		cheats = db.getAllFavoritedCheatsByGame(gameObj.getGameId());
//
//		getViewContent();
//	}
//
//	private void getViewContent() {
//		if (cheats != null) {
//			if (cheats.length <= 0) {
//				ca.finish();
//			}
//			cheatsArray = new ArrayList<Cheat>();
//
//			for (int i = 0; i < cheats.length; i++) {
//				Cheat cheat = cheats[i];
//				cheatsArray.add(cheat);
//			}
//
//			ca.runOnUiThread(returnRes);
//		} else {
//			new AlertDialog.Builder(ca).setMessage(R.string.favorite_no_cheats).show();
//		}
//
//	}

	// @Override
	// public void onResume() {
	// super.onResume();
	// setListAdapter(mAdapter);
	// getViewContent();
	// mAdapter.notifyDataSetChanged();
	// }

//	private final Runnable returnRes = new Runnable() {
//
//		@Override
//		public void run() {
//			if (cheatsArray != null && cheatsArray.size() > 0) {
//				mAdapter.notifyDataSetChanged();
//				for (int i = 0; i < cheatsArray.size(); i++) {
//					mAdapter.add(cheatsArray.get(i));
//				}
//				mProgressDialog.dismiss();
//				mAdapter.notifyDataSetChanged();
//			} else {
//				new AlertDialog.Builder(ca).setIcon(R.drawable.ic_action_warning).setTitle(getString(R.string.err)).setMessage(R.string.err_data_not_accessible).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int whichButton) {
//						ca.finish();
//					}
//				}).create().show();
//			}
//		}
//	};

	private class GetCheatsTask extends AsyncTask<Game, Void, Void> {

		@Override
		protected Void doInBackground(Game... params) {

			if (params[0].getCheats() == null) {
				cheats = getCheatsNow();
			} else {
				cheats = params[0].getCheats();
			}
			

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			cheatAdapter = new CheatAdapter(getActivity(), R.layout.cheatlist_item, cheatsArrayList);
			setListAdapter(cheatAdapter);
		}
	}

	private Cheat[] getCheatsNow() {

		try {
			cheats = db.getAllFavoritedCheatsByGame(gameObj.getGameId());
			cheatsArrayList = new ArrayList<Cheat>();

			if (cheats != null) {
				for (int j = 0; j < cheats.length; j++) {
					cheatsArrayList.add(cheats[j]);
				}
			} else {
				Log.e("FavoriteCheatListActivity()", "db.getAllFavoritedCheatsByGame() == null");
			}

			gameObj.setCheats(cheats);

		} catch (Exception ex) {
			Log.e(getClass().getName(), "Error executing getCheats()", ex);
		}

		return cheats;
	}

	private class CheatAdapter extends ArrayAdapter<Cheat> {

		private final ArrayList<Cheat> arr_cheats;

		public CheatAdapter(Context context, int textViewResourceId, ArrayList<Cheat> items) {
			super(context, textViewResourceId, items);
			this.arr_cheats = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) ca.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.cheatlist_item, null);
			}

			Cheat cheat = arr_cheats.get(position);
			if (cheat != null) {
				TextView tt = (TextView) v.findViewById(R.id.cheat_title);
				tt.setText(cheat.getCheatTitle());
				tt.setTypeface(latoFontRegular);

				ImageView flag_new = (ImageView) v.findViewById(R.id.newaddition);
				flag_new.setVisibility(View.GONE);

				ImageView flag_screenshots = (ImageView) v.findViewById(R.id.screenshots);
				if (cheat.hasScreenshotOnSd()) {
					flag_screenshots.setVisibility(View.VISIBLE);
					flag_screenshots.setImageResource(R.drawable.flag_img);
				} else {
					flag_screenshots.setVisibility(View.GONE);
				}

				ImageView flag_german = (ImageView) v.findViewById(R.id.flag);
				if (cheat.getLanguageId() == 2) { // 2 = Deutsch
					flag_german.setVisibility(View.VISIBLE);
					flag_german.setImageResource(R.drawable.flag_german);
				} else {
					flag_german.setVisibility(View.GONE);
				}

			}
			return v;
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof ElementsListClickHandler)) {
			throw new IllegalStateException("Activity must implement fragment's callbacks.");
		}

		handler = (ElementsListClickHandler) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		handler = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		// mCallbacks.onItemSelected(DummyContent.ITEMS.get(position).id);
		handler.onItemSelected(position);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != AdapterView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(activateOnItemClick ? AbsListView.CHOICE_MODE_SINGLE : AbsListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == AdapterView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}

}
