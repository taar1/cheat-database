package com.cheatdatabase;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
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
import android.widget.RatingBar;
import android.widget.TextView;

import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.Member;
import com.google.analytics.tracking.android.Tracker;
import com.google.gson.Gson;

/**
 * A list fragment representing a list of Cheats. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link CheatDetail%Fragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link com.cheatdatabase.CheatListFragment.Callbacks}
 * interface.
 */
public class CheatListFragment extends ListFragment {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(int position);
	}

	/**
	 * A dummy implementation of the {@link com.cheatdatabase.CheatListFragment.Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(int id) {
		}
	};

	public Game gameObj;

	private ArrayList<Cheat> cheatsArrayList = new ArrayList<Cheat>();

	private Cheat[] cheats;

	protected Tracker tracker;

	private CheatAdapter cheatAdapter;

	private Typeface latoFontRegular;

	private CheatListActivity cheatListActivity;

	private SharedPreferences settings;

	private Member member;

	private Editor editor;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public CheatListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		cheatListActivity = (CheatListActivity) getActivity();
		latoFontRegular = Tools.getFont(getActivity().getAssets(), "Lato-Regular.ttf");

		settings = cheatListActivity.getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
		editor = settings.edit();

		// if (gameObj == null) {
		// gameObj = (Game)
		// cheatListActivity.getIntent().getSerializableExtra("gameObj");
		// }
		// if (gameObj == null) {
		// gameObj = new
		// Gson().fromJson(settings.getString(Konstanten.PREFERENCES_TEMP_GAME_OBJECT_VIEW,
		// null), Game.class);
		// }

		if (member == null) {
			member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
		}

		gameObj = (Game) cheatListActivity.getIntent().getSerializableExtra("gameObj");
		if (gameObj == null) {
			new GetCheatsTask().execute(new Game());
		} else {
			new GetCheatsTask().execute(gameObj);
		}
	}

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
			if (member == null) {
				cheats = Webservice.getCheatList(gameObj, 0);
			} else {
				cheats = Webservice.getCheatList(gameObj, member.getMid());
			}
			cheatsArrayList = new ArrayList<Cheat>();

			if (cheats != null) {
				for (int j = 0; j < cheats.length; j++) {
					cheatsArrayList.add(cheats[j]);
				}
			} else {
				Log.e("CheatListActivity()", "Webservice.getCheatList() == null");
			}

			for (int i = 0; i < cheats.length; i++) {
				Log.d("cheats", cheats[i].getCheatTitle());
			}

			gameObj.setCheats(cheats);

			// Put game object to local storage for large games like Pokemon
			editor.putString(Konstanten.PREFERENCES_TEMP_GAME_OBJECT_VIEW, new Gson().toJson(gameObj));
			editor.commit();

		} catch (Exception ex) {
			Log.e(getClass().getName(), "Error executing getCheats()", ex);
		}

		return cheats;
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
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException("Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		// mCallbacks.onItemSelected(DummyContent.ITEMS.get(position).id);
		mCallbacks.onItemSelected(position);
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

	private class CheatAdapter extends ArrayAdapter<Cheat> {

		private final ArrayList<Cheat> items;

		public CheatAdapter(Context context, int textViewResourceId, ArrayList<Cheat> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.cheatlist_item, null);
			}

			try {
				Cheat cheat = items.get(position);
				if (cheat != null) {

					TextView tt = (TextView) v.findViewById(R.id.cheat_title);
					tt.setText(cheat.getCheatTitle());
					tt.setTypeface(latoFontRegular);

					// Durchschnittsrating (nicht Member-Rating)
					RatingBar ratingBar = (RatingBar) v.findViewById(R.id.small_ratingbar);
					ratingBar.setNumStars(5);
					ratingBar.setRating(cheat.getRatingAverage() / 2);

					ImageView flag_newaddition = (ImageView) v.findViewById(R.id.newaddition);
					if (cheat.getDayAge() < Konstanten.CHEAT_DAY_AGE_SHOW_NEWADDITION_ICON) {
						flag_newaddition.setImageResource(R.drawable.flag_new);
						flag_newaddition.setVisibility(View.VISIBLE);
					} else {
						flag_newaddition.setVisibility(View.GONE);
					}

					ImageView flag_screenshots = (ImageView) v.findViewById(R.id.screenshots);
					if (cheat.isScreenshots()) {
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

					// FOR TESTING ONLY
					// if ((modulo % 4 == 1) || (modulo % 4 == 2) || (modulo % 4
					// == 3)) {
					// flag_newaddition.setVisibility(View.VISIBLE);
					// flag_newaddition.setImageResource(R.drawable.flag_new);
					// }
					//
					// if ((modulo % 4 == 2) || (modulo % 4 == 3)) {
					// flag_screenshots.setVisibility(View.VISIBLE);
					// flag_screenshots.setImageResource(R.drawable.flag_img);
					// }
					//
					// if (modulo % 4 == 3) {
					// flag_german.setVisibility(View.VISIBLE);
					// flag_german.setImageResource(R.drawable.flag_german);
					// }
					// modulo++;
				}
			} catch (Exception e) {
				Log.e(getClass().getName() + ".getView ERROR:", e.getMessage());
			}
			return v;
		}
	}
}
