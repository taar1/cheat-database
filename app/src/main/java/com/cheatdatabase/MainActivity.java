package com.cheatdatabase;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.appbrain.AppBrain;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.fragments.ContactFormFragment_;
import com.cheatdatabase.fragments.FavoriteGamesListFragment_;
import com.cheatdatabase.fragments.NewsFragment_;
import com.cheatdatabase.fragments.SubmitCheatFragment_;
import com.cheatdatabase.fragments.SystemListFragment_;
import com.cheatdatabase.fragments.TopMembersFragment_;
import com.cheatdatabase.helpers.DistinctValues;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.navigationdrawer.CustomDrawerAdapter;
import com.cheatdatabase.navigationdrawer.DrawerItem;
import com.cheatdatabase.search.SearchSuggestionProvider;
import com.google.android.gms.analytics.HitBuilders;
import com.google.gson.Gson;
import com.mopub.mobileads.MoPubView;
import com.splunk.mint.Mint;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_main)
public class MainActivity extends ActionBarActivity implements ActionBar.OnNavigationListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String DRAWER_ITEM_ID = "drawerId";
    public static final String DRAWER_ITEM_NAME = "drawerName";

    // Navigation Drawer
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle;
    private CharSequence mDrawerTitle;

    List<DrawerItem> dataList;
    CustomDrawerAdapter mAdapter;

    private Member member;

    private SharedPreferences settings;
    private Editor editor;

    private SearchManager searchManager;
    private MenuItem searchItem;
    private SearchView searchView;

//    private Tracker tracker;

    private static final String SCREEN_LABEL = "Main Activity";

    @ViewById(R.id.adview)
    MoPubView mAdView;

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    @ViewById(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @Bean
    Tools tools;

    //    @InstanceState
    @Extra
    int mFragmentId;


    @AfterViews
    public void createView() {
        setTitle(R.string.app_name);

        Reachability.registerReachability(this);
        Mint.initAndStartSession(this, Konstanten.SPLUNK_MINT_API_KEY);

        settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.app_icon_fox);

        tools.loadAd(mAdView, getString(R.string.screen_type));
//        tools.initGA(MainActivity.this, tracker, SCREEN_LABEL, "Main Activity", "Cheat-Database Main Activity");

        // Log setting open event with category="ui", action="open", and label="settings"
        CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "loaded").setLabel("activity").build());

        AppBrain.init(this);

//        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);

        // TODO FIXME - find out where this part was before and re-add it.
        FragmentManager frgManager = getFragmentManager();
        frgManager.beginTransaction().replace(R.id.content_frame, SystemListFragment_.builder().build()).commit();


        Log.d(TAG, "XXXX mFragmentId: " + mFragmentId);

        // Create Drawer
        // TODO
        // TODO
        // TODO
        // TODO
        // TODO hier noch savedInstance speichern/holen (die ID vom drawerr item beim anklicken)
        // damit das zuletzt aktive fragment wieder angezeigt wird, wenn man auf "back" klickt.
        createNavigationDrawer();
    }

//    @Override
//    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
//        // Saving the currently active fragment ID to the Bundle object
//        outState.putInt(Konstanten.PREFERENCES_SELECTED_DRAWER_FRAGMENT_ID, mFragmentId);
//        super.onSaveInstanceState(outState, outPersistentState);
//    }

//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        mFragmentId = savedInstanceState.getInt(Konstanten.PREFERENCES_SELECTED_DRAWER_FRAGMENT_ID);
////        createNavigationDrawer(savedInstanceState);
//    }
//
//    @Override
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//        // Save the user's current game state
////        savedInstanceState.putInt("hhh", 3333);
//        savedInstanceState.putInt(Konstanten.PREFERENCES_SELECTED_DRAWER_FRAGMENT_ID, mFragmentId);
//
//        // Always call the superclass so it can save the view hierarchy state
//        super.onSaveInstanceState(savedInstanceState);
//    }
//
//    @Override
//    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
//        super.onRestoreInstanceState(savedInstanceState, persistentState);
//    }

    @Override
    public void onPause() {
        super.onPause();
        Reachability.unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "ON RESUME");
//        selectItem(settings.getInt(Konstanten.PREFERENCES_SELECTED_DRAWER_FRAGMENT_ID, 1));
        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        // TODO  loesung finden wie man das gespeicherte fragment ID wieder auf 1 setzen kann
        // TODO ohne, dass das schon geschieht, wenn man von MainActivity weg geht
        Log.d(TAG, "ON DESTROY");
//        editor.putInt(Konstanten.PREFERENCES_SELECTED_DRAWER_FRAGMENT_ID, 1);
//        editor.commit();
        super.onDestroy();
    }

    // http://developer.android.com/training/implementing-navigation/nav-drawer.html#Init
    private void createNavigationDrawer() {
        mTitle = mDrawerTitle = getTitle();

        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.page_indicator_background));
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();

        LinearLayout drawerHeaderLayout = new LinearLayout(this);
        drawerHeaderLayout.setPadding(0, 40, 0, 60);
        drawerHeaderLayout.setGravity(Gravity.CENTER);

        ImageView drawerHeaderLogo = new ImageView(this);
        drawerHeaderLogo.setImageResource(R.drawable.logo_full_small);
        drawerHeaderLayout.addView(drawerHeaderLogo);

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.addHeaderView(drawerHeaderLayout);
        mDrawerList.setHeaderDividersEnabled(true);

        // set custom shadow that overlays main content when drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // Add Drawer Item to dataList
        dataList = new ArrayList<>();
        dataList.add(new DrawerItem(getString(R.string.goto_games_and_cheats), R.drawable.ic_home));
        dataList.add(new DrawerItem(getString(R.string.goto_news), R.drawable.ic_info));
        dataList.add(new DrawerItem(getString(R.string.favorites), R.drawable.ic_favorite));
        dataList.add(new DrawerItem(getString(R.string.top_members_title), R.drawable.ic_topmembers));
        dataList.add(new DrawerItem(getString(R.string.submit_cheat_title), R.drawable.ic_submit));
        dataList.add(new DrawerItem(getString(R.string.menu_more_apps), R.drawable.ic_otherapps));
        dataList.add(new DrawerItem(getString(R.string.rate_us), R.drawable.ic_rate));
        dataList.add(new DrawerItem(getString(R.string.contactform_title), R.drawable.ic_contact));

        mAdapter = new CustomDrawerAdapter(this, R.layout.custom_drawer_item, dataList);
        mDrawerList.setAdapter(mAdapter);

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            @Override
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
                syncState();
            }

            /** Called when a drawer has settled in a completely open state. */
            @Override
            public void onDrawerOpened(View drawerView) {
                // creates call to onPrepareOptionsMenu()
                invalidateOptionsMenu();
                syncState();
            }
        };
        mDrawerToggle.syncState();
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

//        if (savedInstanceState == null) {
////         (if used method parameter is needed: createNavigationDrawer(Bundle savedInstanceState))
//            int position = settings.getInt(Konstanten.PREFERENCES_SELECTED_DRAWER_FRAGMENT_ID, 0);
//            selectItem(position);
//        } else {
//            selectItem(savedInstanceState.getInt(Konstanten.PREFERENCES_SELECTED_DRAWER_FRAGMENT_ID));
//        }

        // TODO SAVEDINSTANCE SPEICHERN GEHT NOCH NICHT (https://github.com/excilys/androidannotations/wiki/Save-instance-state)
        if (mFragmentId > 7) {
            mFragmentId = 0;
        }
        selectItem(mFragmentId);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // menu.clear();
        if (member != null) {
            getMenuInflater().inflate(R.menu.signout_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.signin_menu, menu);
        }

        getMenuInflater().inflate(R.menu.clear_search_history_menu, menu);

        // Search
        // Associate searchable configuration with the SearchView
        try {
            getMenuInflater().inflate(R.menu.search_menu, menu);
            searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

            searchItem = menu.findItem(R.id.search);
            searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        } catch (Exception e) {
            Log.e("MainActivity", e.getLocalizedMessage());
        }

        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (member != null) {
            getMenuInflater().inflate(R.menu.signout_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.signin_menu, menu);
        }
        getMenuInflater().inflate(R.menu.clear_search_history_menu, menu);

        // Search
        // Associate searchable configuration with the SearchView
        getMenuInflater().inflate(R.menu.search_menu, menu);
        searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        // If the nav drawer is open, hide action items related to the content
        // view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.search).setVisible(!drawerOpen);

        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Backward-compatible version of {@link android.app.ActionBar#getThemedContext()} that
     * simply returns the {@link android.app.Activity} if
     * <code>getThemedContext</code> is unavailable.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private Context getActionBarThemedContextCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return getActionBar().getThemedContext();
        } else {
            return this;
        }
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
//        Fragment fragment = new SystemListFragment();

        FragmentManager frgManager = getFragmentManager();
        frgManager.beginTransaction().replace(R.id.content_frame, SystemListFragment_.builder().build()).commit();

        mDrawerList.setItemChecked(position, true);
        setTitle(dataList.get(position).getItemName());
        mDrawerLayout.closeDrawer(mDrawerList);

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // Return result code. Login success, Register success etc.
            int intentReturnCode = data.getIntExtra("result", Konstanten.LOGIN_REGISTER_FAIL_RETURN_CODE);

            if (intentReturnCode == Konstanten.LOGIN_SUCCESS_RETURN_CODE) {
                member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
                Toast.makeText(MainActivity.this, R.string.login_ok, Toast.LENGTH_LONG).show();
            } else if (intentReturnCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
                member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
                Toast.makeText(MainActivity.this, R.string.register_thanks, Toast.LENGTH_LONG).show();
            }
            invalidateOptionsMenu();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle action buttons
        switch (item.getItemId()) {
            case R.id.action_clear_search_history:
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
                suggestions.clearHistory();
                Toast.makeText(MainActivity.this, R.string.search_history_cleared, Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_login:
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(loginIntent, Konstanten.LOGIN_REGISTER_OK_RETURN_CODE);
                return true;
            case R.id.action_logout:
                member = null;
                tools.logout(MainActivity.this, settings.edit());
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // See strings.xml for menu list. Don't change the order!
    public static final int DRAWER_MAIN = 1;
    public static final int DRAWER_NEWS = 2;
    public static final int DRAWER_FAVORITES = 3;
    public static final int DRAWER_TOP_MEMBERS = 4;
    public static final int DRAWER_SUBMIT_CHEAT = 5;
    public static final int DRAWER_MORE_APPS = 6;
    public static final int DRAWER_RATE_APP = 7;
    public static final int DRAWER_CONTACT = 8;

    // update the main content by replacing fragments
    private void selectItem(int position) {
        Fragment fragment = null;
        Bundle args = new Bundle();
        boolean isFragment = false;

        setMainTitle(position);


        FragmentManager annotationFragmentManager = getFragmentManager();
        switch (position) {
            case DRAWER_MAIN:
                // Log setting open event with category="ui", action="open", and label="settings"
                CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "open").setLabel("main").build());
                annotationFragmentManager.beginTransaction().replace(R.id.content_frame, SystemListFragment_.builder().mDrawerId(position).mDrawerName(dataList.get(position).getItemName()).build()).commit();
                isFragment = false;
                break;
            case DRAWER_NEWS:
                CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "open").setLabel("news").build());
                annotationFragmentManager.beginTransaction().replace(R.id.content_frame, NewsFragment_.builder().mDrawerId(position).mDrawerName(dataList.get(position).getItemName()).build()).commit();
                isFragment = false;
                break;
            case DRAWER_FAVORITES:
                CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "open").setLabel("favorites").build());
                annotationFragmentManager.beginTransaction().replace(R.id.content_frame, FavoriteGamesListFragment_.builder().mDrawerId(position).mDrawerName(dataList.get(position).getItemName()).build()).commit();
                isFragment = false;
                break;
            case DRAWER_TOP_MEMBERS:
                CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "open").setLabel("top_members").build());
                annotationFragmentManager.beginTransaction().replace(R.id.content_frame, TopMembersFragment_.builder().mDrawerId(position).mDrawerName(dataList.get(position).getItemName()).build()).commit();
                isFragment = false;
                break;
            case DRAWER_SUBMIT_CHEAT:
                CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "open").setLabel("submit_cheat").build());
                annotationFragmentManager.beginTransaction().replace(R.id.content_frame, SubmitCheatFragment_.builder().mDrawerId(position).mDrawerName(dataList.get(position).getItemName()).build()).commit();
                isFragment = false;
                break;
            case DRAWER_CONTACT:
                // If position = 8 -> out of bounds error. no idea why... it works like this here.
                position = 7;
                CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "open").setLabel("contact").build());
                annotationFragmentManager.beginTransaction().replace(R.id.content_frame, ContactFormFragment_.builder().mDrawerId(position).mDrawerName(dataList.get(position).getItemName()).build()).commit();
                isFragment = false;
                break;
            case DRAWER_MORE_APPS:
                CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "open").setLabel("more_apps").build());
                Uri uri = Uri.parse(DistinctValues.URL_MORE_APPS);
                Intent intentMoreApps = new Intent(Intent.ACTION_VIEW, uri);
                if (intentMoreApps.resolveActivity(getPackageManager()) != null) {
                    startActivity(intentMoreApps);
                } else {
                    Toast.makeText(MainActivity.this, R.string.err_other_problem, Toast.LENGTH_LONG).show();
                }
                break;
            case DRAWER_RATE_APP:
                CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "open").setLabel("rate_app").build());
                Uri appUri = Uri.parse(DistinctValues.GOOGLE_PLAY_URL);
                Intent intentRateApp = new Intent(Intent.ACTION_VIEW, appUri);
                if (intentRateApp.resolveActivity(getPackageManager()) != null) {
                    startActivity(intentRateApp);
                } else {
                    Toast.makeText(MainActivity.this, R.string.err_other_problem, Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }

        if (isFragment) {
            fragment.setArguments(args);
            FragmentManager frgManager = getFragmentManager();
            frgManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        }

        mDrawerList.setItemChecked(position, true);
        setTitle(dataList.get(position).getItemName());
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private void setMainTitle(int position) {
        switch (position) {
            case DRAWER_MAIN:
                mToolbar.setTitle(R.string.app_name);
                break;
            case DRAWER_NEWS:
                mToolbar.setTitle(R.string.news_title);
                break;
            case DRAWER_FAVORITES:
                mToolbar.setTitle(R.string.favorites);
                break;
            case DRAWER_TOP_MEMBERS:
                mToolbar.setTitle(R.string.top_members_top_helping);
                break;
            case DRAWER_SUBMIT_CHEAT:
                mToolbar.setTitle(R.string.submit_cheat_short);
                break;
            case DRAWER_CONTACT:
                mToolbar.setTitle(R.string.contactform_title);
                break;
            default:
                mToolbar.setTitle(R.string.app_name);
                break;
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
//		getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
//		mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.i("DRAWER", position + "");
            mFragmentId = position;
//            editor.putInt(Konstanten.PREFERENCES_SELECTED_DRAWER_FRAGMENT_ID, position);
//            editor.commit();
            selectItem(position);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        editor.putInt(Konstanten.PREFERENCES_SELECTED_DRAWER_FRAGMENT_ID, 0);
//        editor.commit();
        AppBrain.getAds().maybeShowInterstitial(this);
        finish();
    }

}
