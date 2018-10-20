package com.cheatdatabase;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
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

import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import needle.Needle;

public class CheatListFragment extends ListFragment {
    private static String TAG = CheatListFragment.class.getSimpleName();

    public Game gameObj;
    private List<Cheat> cheatList;
    private CheatAdapter cheatAdapter;
    private Typeface latoFontRegular;
    private CheatsByGameListActivity cheatsByGameListActivity;
    private SharedPreferences settings;
    private Editor editor;
    private Member member;


    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private CheatListClickCallbacks mCheatListClickCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface CheatListClickCallbacks {
        /**
         * Callback for when an item has been selected.
         */
        void onItemSelected(int position);
    }

    /**
     * A dummy implementation of the {@link com.cheatdatabase.CheatListFragment.CheatListClickCallbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static CheatListClickCallbacks sDummyCallbacks = new CheatListClickCallbacks() {
        @Override
        public void onItemSelected(int id) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CheatListFragment() {
        cheatList = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cheatsByGameListActivity = (CheatsByGameListActivity) getActivity();
        latoFontRegular = Tools.getFont(getActivity().getAssets(), Konstanten.FONT_REGULAR);

        settings = cheatsByGameListActivity.getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();

        if (member == null) {
            member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
        }

        gameObj = (Game) cheatsByGameListActivity.getIntent().getParcelableExtra("gameObj");
        getCheats();
    }

    /**
     * Get cheatList either from the object or load them online.
     */
    private void getCheats() {
        Needle.onBackgroundThread().execute(() -> {
            if ((gameObj == null) || (gameObj.getCheatList() == null) || (gameObj.getCheatList().size() < 1)) {
                cheatList = getCheatsNow();
            } else {
                cheatList = gameObj.getCheatList();
            }
            updateUI();
        });
    }

    private void updateUI() {
        Needle.onMainThread().execute(() -> {
            cheatAdapter = new CheatAdapter(getActivity(), R.layout.listrow_cheat_item, cheatList);
            setListAdapter(cheatAdapter);
        });
    }

    private List<Cheat> getCheatsNow() {
        try {
            if (member == null) {
                cheatList = Webservice.getCheatList(gameObj, 0, true);
            } else {
                cheatList = Webservice.getCheatList(gameObj, member.getMid(), true);
            }

            gameObj.setCheatList(cheatList);

            // Put game object to local storage for large games like Pokemon
            editor.putString(Konstanten.PREFERENCES_TEMP_GAME_OBJECT_VIEW, new Gson().toJson(gameObj));
            editor.commit();

        } catch (Exception ex) {
            Log.e(getClass().getName(), "Error executing getCheatList()", ex);
        }

        return cheatList;
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
        if (!(activity instanceof CheatListClickCallbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCheatListClickCallbacks = (CheatListClickCallbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Reset the active callbacks interface to the dummy implementation.
        mCheatListClickCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        Log.d(TAG, "onListItemClick: " + position);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        // mCheatListClickCallbacks.onItemSelected(DummyContent.ITEMS.get(position).id);
        mCheatListClickCallbacks.onItemSelected(position);
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
     * Turns on activate-on-click mode. When this mode is on, list cheatList will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give cheatList the 'activated' state when touched.
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

        private final List<Cheat> cheatList;

        public CheatAdapter(Context context, int textViewResourceId, List<Cheat> items) {
            super(context, textViewResourceId, items);
            this.cheatList = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.listrow_cheat_item, parent);
            }

            try {
                Cheat cheat = cheatList.get(position);
                if (cheat != null) {

                    TextView tt = v.findViewById(R.id.cheat_title);
                    tt.setText(cheat.getCheatTitle());
                    tt.setTypeface(latoFontRegular);

                    // Durchschnittsrating (nicht Member-Rating)
                    RatingBar ratingBar = v.findViewById(R.id.small_ratingbar);
                    ratingBar.setNumStars(5);
                    ratingBar.setRating(cheat.getRatingAverage() / 2);

                    ImageView flag_newaddition = v.findViewById(R.id.newaddition);
                    if (cheat.getDayAge() < Konstanten.CHEAT_DAY_AGE_SHOW_NEWADDITION_ICON) {
                        flag_newaddition.setImageResource(R.drawable.flag_new);
                        flag_newaddition.setVisibility(View.VISIBLE);
                    } else {
                        flag_newaddition.setVisibility(View.GONE);
                    }

                    ImageView flag_screenshots = v.findViewById(R.id.screenshots);
                    if (cheat.isScreenshots()) {
                        flag_screenshots.setVisibility(View.VISIBLE);
                        flag_screenshots.setImageResource(R.drawable.flag_img);
                    } else {
                        flag_screenshots.setVisibility(View.GONE);
                    }

                    ImageView flag_german = v.findViewById(R.id.flag);
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
                Log.e(getClass().getSimpleName() + ".getView ERROR:", e.getMessage());
            }
            return v;
        }
    }
}
