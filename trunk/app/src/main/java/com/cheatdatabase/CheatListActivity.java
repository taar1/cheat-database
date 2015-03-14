package com.cheatdatabase;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.dialogs.RateCheatDialog;
import com.cheatdatabase.dialogs.RateCheatDialog.RateCheatDialogListener;
import com.cheatdatabase.dialogs.ReportCheatDialog;
import com.cheatdatabase.dialogs.ReportCheatDialog.ReportCheatDialogListener;
import com.cheatdatabase.handset.cheatview.CheatViewPageIndicator;
import com.cheatdatabase.helpers.CheatDatabaseAdapter;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.google.analytics.tracking.android.Tracker;
import com.google.gson.Gson;
import com.mopub.mobileads.MoPubView;

/**
 * An activity representing a list of Cheats. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link "CheatDetailActivity"} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link CheatListFragment} and the item details (if present) is a
 * {@link "CheatDetailFragment"}.
 * <p/>
 * This activity also implements the required
 * {@link CheatListFragment.Callbacks} interface to listen for item selections.
 */
public class CheatListActivity extends ActionBarActivity implements CheatListFragment.Callbacks, ReportCheatDialogListener, RateCheatDialogListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private static final String SCREEN_LABEL = "Cheat List By Game ID Screen";
    protected Tracker tracker;

    private SharedPreferences settings;
    private Editor editor;
    private Member member;
    public Game gameObj;

    // private ArrayList<Cheat> cheatsArrayList = new ArrayList<Cheat>();

    private Cheat[] cheats = null;
    private Cheat visibleCheat;
    private int lastPosition;
    private Game lastGameObj;

    private ProgressDialog cheatProgressDialog = null;

    private CheatDetailTabletFragment cheatDetailFragment;
    private CheatForumFragment cheatForumFragment;
    private CheatDetailMetaFragment cheatDetailMetaFragment;

    private ShareActionProvider mShareActionProvider;

    private ImageView reloadView;

    private ViewGroup adViewContainer;
    private MoPubView mAdView;
    private Toolbar toolbar;

    public ShareActionProvider getmShareActionProvider() {
        return mShareActionProvider;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheat_list);
        Reachability.registerReachability(this.getApplicationContext());
        //Tools.styleActionbar(this);

        // TODO FIXME toolbar ist hier NULL... kann sie nicht finden im layout XML
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // MATERIAL DESIGN: ERROR. CRASHT MIT NULLPOINTER HIER... WARUM? MAINACTIVITY.JAVA ALS VORLAGE NEHMEN.
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setHomeButtonEnabled(true);

        settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();

        Tools.initMoPubAdView(this, mAdView);

        if (member == null) {
            member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
        }

        reloadView = (ImageView) findViewById(R.id.reload);
        if (Reachability.reachability.isReachable) {
            getCheatList();
        } else {
            reloadView.setVisibility(View.VISIBLE);
            reloadView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (Reachability.reachability.isReachable) {
                        getCheatList();
                    } else {
                        Toast.makeText(CheatListActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            Toast.makeText(CheatListActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }

        handleIntent(getIntent());

        if (findViewById(R.id.cheat_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((CheatListFragment) getSupportFragmentManager().findFragmentById(R.id.cheat_list)).setActivateOnItemClick(true);

            // cheatListFragment scheint nicht gebraucht zu werden hier?!
            // Bundle args = new Bundle();
            // args.putSerializable("gameObj", gameObj)
            // cheatListFragment = ((CheatListFragment)
            // getSupportFragmentManager().findFragmentById(R.id.cheat_list));
            // cheatListFragment.setActivateOnItemClick(true);
            // cheatListFragment.setArguments(args);
        }
        cheatProgressDialog.dismiss();
        // TODO: If exposing deep links into your app, handle intents here.

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // schauen wie man status persistent halten kann von fragment
        outState.putSerializable("gameObj", gameObj);
    }

    private void handleIntent(final Intent intent) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                // gameObj = new
                // Gson().fromJson(settings.getString(Konstanten.PREFERENCES_TEMP_GAME_OBJECT_VIEW,
                // null), Game.class);
                // if (gameObj == null) {
                // gameObj = (Game) intent.getSerializableExtra("gameObj");
                // }
                gameObj = (Game) intent.getSerializableExtra("gameObj");

                Tools.initGA(CheatListActivity.this, tracker, SCREEN_LABEL, "Cheat List", gameObj.getGameName());
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(gameObj.getGameName());
                getSupportActionBar().setSubtitle(gameObj.getSystemName());
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                    }

                });

            }
        }).start();

    }

    private void getCheatList() {
        reloadView.setVisibility(View.INVISIBLE);

        cheatProgressDialog = ProgressDialog.show(CheatListActivity.this, getString(R.string.please_wait) + "...", getString(R.string.retrieving_data) + "...", true);
        // try {
        // if (member == null) {
        // cheats = Webservice.getCheatList(gameObj, 0);
        // } else {
        // cheats = Webservice.getCheatList(gameObj, member.getMid());
        // }
        // cheatsArrayList = new ArrayList<Cheat>();
        //
        // if (cheats != null) {
        // for (int j = 0; j < cheats.length; j++) {
        // cheatsArrayList.add(cheats[j]);
        // }
        // } else {
        // Log.e("CheatListActivity()", "Webservice.getCheatList() == null");
        // }
        //
        // for (int i = 0; i < cheats.length; i++) {
        // Log.d("cheats", cheats[i].getCheatTitle());
        // }
        //
        // gameObj.setCheats(cheats);
        //
        // // Put game object to local storage for large games like Pokemon
        // editor.putString(Konstanten.PREFERENCES_TEMP_GAME_OBJECT_VIEW, new
        // Gson().toJson(gameObj));
        // editor.commit();
        //
        // } catch (Exception ex) {
        // Log.e(getClass().getName(), "Error executing getCheats()", ex);
        // }
        reloadView.setVisibility(View.GONE);
    }

    // @Override
    // protected void onDestroy() {
    // editor.remove(Konstanten.PREFERENCES_TEMP_GAME_OBJECT_VIEW);
    // editor.commit();
    // super.onDestroy();
    // }

    public void showReportDialog() {
        if ((member == null) || (member.getMid() == 0)) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_LONG).show();
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
        cheatDetailFragment.updateMemberCheatRating(selectedRating);

        // FIXME make the star to highlighton all fragments
        cheatDetailFragment.highlightRatingIcon(true);
        cheatDetailMetaFragment.highlightRatingIcon(true);
        cheatForumFragment.highlightRatingIcon(true);

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
                Toast.makeText(CheatListActivity.this, R.string.thanks_for_reporting, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(CheatListActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
            }
        }

    }

    /**
     * Callback method from {@link CheatListFragment.Callbacks} indicating that
     * the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(int id) {
        this.lastPosition = id;
        this.lastGameObj = gameObj;
        this.visibleCheat = gameObj.getCheats()[id];

        if (mTwoPane) {

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            // Bundle arguments = new Bundle();
            // arguments.putInt(YyyDetailFragment.ARG_ITEM_ID, id);
            // YyyDetailFragment fragment = new YyyDetailFragment();
            // fragment.setArguments(arguments);
            // getSupportFragmentManager().beginTransaction().replace(R.id.cheat_detail_container,
            // fragment).commit();

            // visibleCheat = cheats[id];

            cheatForumFragment = new CheatForumFragment();
            cheatDetailMetaFragment = new CheatDetailMetaFragment();

            // VIEW FOR TABLETS
            Bundle arguments = new Bundle();
            arguments.putInt(CheatDetailTabletFragment.ARG_ITEM_ID, id);
            arguments.putSerializable("cheatObj", visibleCheat);
            arguments.putString("cheatForumFragment", new Gson().toJson(cheatForumFragment));
            arguments.putString("cheatDetailMetaFragment", new Gson().toJson(cheatDetailMetaFragment));

            cheatDetailFragment = new CheatDetailTabletFragment();
            cheatDetailFragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.cheat_detail_container, cheatDetailFragment).commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            // Intent detailIntent = new Intent(this, YyyDetailActivity.class);
            // detailIntent.putExtra(YyyDetailFragment.ARG_ITEM_ID, id);
            // startActivity(detailIntent);

            // editor.putString(Konstanten.PREFERENCES_TEMP_GAME_OBJECT_VIEW,
            // new Gson().toJson(gameObj));
            editor.putInt(Konstanten.PREFERENCES_PAGE_SELECTED, id);
            editor.commit();

            if (Reachability.reachability.isReachable) {
                // Using local Preferences to pass data for large game objects
                // (instead of intent) such as Pokemon
                Intent explicitIntent = new Intent(CheatListActivity.this, CheatViewPageIndicator.class);
                // explicitIntent.putExtra("gameObj", gameObj);
                explicitIntent.putExtra("selectedPage", id);
                explicitIntent.putExtra("layoutResourceId", R.layout.activity_cheatview_pager);
                // explicitIntent.putExtra("pageIndicatorColor",
                // R.color.page_indicator);
                startActivity(explicitIntent);
            } else {
                Toast.makeText(CheatListActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        switch (item.getItemId()) {
            case android.R.id.home:
                editor.remove(Konstanten.PREFERENCES_TEMP_GAME_OBJECT_VIEW);
                editor.commit();
                Intent eIntent = new Intent(this, GamesBySystemActivity.class);
                eIntent.putExtra("systemObj", Tools.getSystemObjectByName(CheatListActivity.this, gameObj.getSystemName()));
                startActivity(eIntent);
                return true;
            case R.id.action_add_to_favorites:
                Toast.makeText(CheatListActivity.this, R.string.favorite_adding, Toast.LENGTH_SHORT).show();
                new AddCheatsToFavoritesTask().execute(gameObj);
                return true;
            case R.id.action_submit_cheat:
                Intent explicitIntent = new Intent(CheatListActivity.this, SubmitCheatActivity.class);
                explicitIntent.putExtra("gameObj", gameObj);
                startActivity(explicitIntent);
                return true;
            case R.id.action_login:
                Intent loginIntent = new Intent(CheatListActivity.this, LoginActivity.class);
                startActivityForResult(loginIntent, Konstanten.LOGIN_REGISTER_OK_RETURN_CODE);
                return true;
            case R.id.action_logout:
                member = null;
                Tools.logout(CheatListActivity.this, editor);
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cheats_by_game_menu, menu);

        if (member != null) {
            getMenuInflater().inflate(R.menu.signout_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.signin_menu, menu);
        }

        getMenuInflater().inflate(R.menu.handset_cheatview_rating_off_menu, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);

        // Sharing
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
//            ShareActionProvider mShare = (ShareActionProvider) item.getActionProvider();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        try {
            String result = String.format(getString(R.string.share_email_subject), visibleCheat.getGameName());
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, result);
            String fullBody = visibleCheat.getGameName() + " (" + visibleCheat.getSystemName() + "): " + visibleCheat.getCheatTitle() + "\n";
            fullBody += Konstanten.BASE_URL + "display/switch.php?id=" + visibleCheat.getCheatId() + "\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, fullBody);
        } catch (Exception ee) {
            // TODO FIXME bei einem wipe muss der visibleCheat hier geupdated
            // werden....
            // TODO icon unten rechts wird vom share icon überdeckt...
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "EMPTY SUBJECT");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "EMPTY BODY");
        }
        mShareActionProvider.setShareIntent(shareIntent);

        // Search
        getMenuInflater().inflate(R.menu.search_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        menu.clear();
        if (member == null) {
            getMenuInflater().inflate(R.menu.signin_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.signout_menu, menu);
        }

        getMenuInflater().inflate(R.menu.cheats_by_game_menu, menu);

        getMenuInflater().inflate(R.menu.search_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onPrepareOptionsMenu(menu);
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
                    Toast.makeText(CheatListActivity.this, R.string.register_thanks, Toast.LENGTH_LONG).show();
                } else if ((member != null) && intentReturnCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
                    Toast.makeText(CheatListActivity.this, R.string.login_ok, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private class AddCheatsToFavoritesTask extends AsyncTask<Game, Void, Void> {

        int returnValueCode;
        CheatDatabaseAdapter dbAdapter;

        @Override
        protected Void doInBackground(Game... params) {
            // TODO Favorite Icon animation anzeigen
            try {
                dbAdapter = new CheatDatabaseAdapter(CheatListActivity.this);
                dbAdapter.open();
                int retVal = dbAdapter.insertFavorites(params[0]);
                if (retVal > 0) {
                    returnValueCode = R.string.add_favorites_ok;
                } else {
                    returnValueCode = R.string.favorite_error;
                }
                dbAdapter.close();
            } catch (Exception e) {
                returnValueCode = R.string.favorite_error;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Favorite Icon animation abschalten
            Toast.makeText(CheatListActivity.this, returnValueCode, Toast.LENGTH_LONG).show();
        }

    }

    public void highlightRatingIcon(ImageButton btnRateCheat, boolean highlight) {
        if (highlight) {
            btnRateCheat.setImageResource(R.drawable.ic_action_star);
        } else {
            btnRateCheat.setImageResource(R.drawable.ic_action_not_important);
        }
    }

}
