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
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.appbrain.AppBrain;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.fragments.ContactFormFragment;
import com.cheatdatabase.fragments.FavoriteGamesListFragment;
import com.cheatdatabase.fragments.NewsFragment;
import com.cheatdatabase.fragments.SubmitCheatFragment;
import com.cheatdatabase.fragments.SystemListFragment;
import com.cheatdatabase.fragments.TopMembersFragment;
import com.cheatdatabase.helpers.Group;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.navigationdrawer.CustomDrawerAdapter;
import com.cheatdatabase.navigationdrawer.DrawerItem;
import com.cheatdatabase.search.SearchSuggestionProvider;
import com.google.analytics.tracking.android.Tracker;
import com.google.gson.Gson;
import com.mopub.mobileads.MoPubView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity implements ActionBar.OnNavigationListener {

    private static Typeface latoFontBold;

    private Tracker tracker;

    // Navigation Drawer
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    // private String[] mNavigationDrawerListEntries;
    List<DrawerItem> dataList;
    CustomDrawerAdapter mAdapter;

    // more efficient than HashMap for mapping integers to objects
    SparseArray<Group> groups = new SparseArray<Group>();

    private Member member;

    // private String[] allSystems;
    // private String[] allSystemsPlusEmpty;

    private SharedPreferences settings;
    private Editor editor;

    private MoPubView mAdView;

    private static final String SCREEN_LABEL = "Main Activity";
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);

        init();

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.page_indicator_background));

        // Create Drawer
        createNavigationDrawer(savedInstanceState);
    }

    /**
     * Custom adapter dropdown menu with all the game systems.
     *
     * @author Dominik
     */
    public class GameSystemsAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final String[] values;

        public GameSystemsAdapter(Context context, String[] values) {
            super(context, R.layout.gamesystem_dropdown_menu_item, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.gamesystem_dropdown_menu_item, parent, false);

            TextView textView = (TextView) rowView.findViewById(R.id.system_name);
            textView.setTypeface(latoFontBold);
            textView.setText(values[position]);

            return rowView;
        }
    }

    private void init() {
        Reachability.registerReachability(this.getApplicationContext());

        settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mAdView = new MoPubView(this);

        Tools.initToolbarBase(this, mToolbar);

        // FIXME ads werden nicht angezeigt...
        mAdView = Tools.initMoPubAdView(this, mAdView);
        Tools.initGA(MainActivity.this, tracker, SCREEN_LABEL, "Main Activity", "Cheat-Database Main Activity");

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.app_icon_fox);
        AppBrain.init(this);

        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);

        latoFontBold = Tools.getFont(getAssets(), Konstanten.FONT_BOLD);

        // TODO FIXME - find out where this part was before and re-add it.
        FragmentManager frgManager = getFragmentManager();
        frgManager.beginTransaction().replace(R.id.content_frame, new SystemListFragment()).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // selectItem(0); // FIXME here maybe preserving fragment ID
        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
    }

    // http://developer.android.com/training/implementing-navigation/nav-drawer.html#Init
    private void createNavigationDrawer(Bundle savedInstanceState) {
        mTitle = mDrawerTitle = getTitle();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        dataList.add(new DrawerItem(getString(R.string.contactform_title), R.drawable.ic_contact));
        dataList.add(new DrawerItem(getString(R.string.rate_us), R.drawable.ic_rate));

        mAdapter = new CustomDrawerAdapter(this, R.layout.custom_drawer_item, dataList);
        mDrawerList.setAdapter(mAdapter);

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            @Override
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
                syncState();
            }

            /** Called when a drawer has settled in a completely open state. */
            @Override
            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);

                // creates call to onPrepareOptionsMenu()
                invalidateOptionsMenu();
                syncState();
            }
        };
        mDrawerToggle.syncState();
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            int position = settings.getInt(Konstanten.PREFERENCES_SELECTED_DRAWER_FRAGMENT_ID, 99);
            selectItem(position);
        }
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
        getMenuInflater().inflate(R.menu.search_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

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
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
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

        // SELECTION OF SYSTEM IN DROPDOWN MENU
        // if (position > 0) {
        // // List all Games from selected System
        // Intent explicitIntent = new Intent(this,
        // GamesBySystemActivity.class);
        // explicitIntent.putExtra("systemObj",
        // Tools.getSystemObjectByName(this, allSystems[position - 1]));
        // startActivity(explicitIntent);
        // } else {
        Fragment fragment = new SystemListFragment();
        //getSupportActionBar().setTitle(R.string.app_name);

        FragmentManager frgManager = getFragmentManager();
        frgManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        mDrawerList.setItemChecked(position, true);
        setTitle(dataList.get(position).getItemName());
        mDrawerLayout.closeDrawer(mDrawerList);
        // }

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
                Tools.logout(MainActivity.this, settings.edit());
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
    public static final int DRAWER_CONTACT = 7;
    public static final int DRAWER_RATE_APP = 8;

    // update the main content by replacing fragments
    private void selectItem(int position) {
        Fragment fragment = null;
        Bundle args = new Bundle();
        boolean isFragment = false;

        //ActionBar actionBar = getActionBar();

        switch (position) {
            case DRAWER_MAIN:
                fragment = new SystemListFragment();
                args.putString(SystemListFragment.ITEM_NAME, dataList.get(position).getItemName());
                args.putInt(SystemListFragment.IMAGE_RESOURCE_ID, dataList.get(position).getImgResID());
                // actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                // actionBar.setListNavigationCallbacks(new
                // GameSystemsAdapter(getActionBarThemedContextCompat(),
                // allSystemsPlusEmpty), this);
                mToolbar.setTitle(R.string.app_name);
                isFragment = true;
                break;
            case DRAWER_NEWS:
                fragment = new NewsFragment();
                // Remove System-Select Drop-Down
//                actionBar.setListNavigationCallbacks(null, null);
//                actionBar.setNavigationMode(0);
                mToolbar.setTitle(R.string.news_title);
                isFragment = true;
                break;
            case DRAWER_FAVORITES:
                fragment = new FavoriteGamesListFragment();
                args.putString(FavoriteGamesListFragment.ITEM_NAME, dataList.get(position).getItemName());
                args.putInt(FavoriteGamesListFragment.IMAGE_RESOURCE_ID, dataList.get(position).getImgResID());
                // Remove System-Select Drop-Down
//                actionBar.setListNavigationCallbacks(null, null);
//                actionBar.setNavigationMode(0);
                mToolbar.setTitle(R.string.favorites);
                isFragment = true;
                break;
            case DRAWER_TOP_MEMBERS:
                fragment = new TopMembersFragment();
                args.putString(TopMembersFragment.ITEM_NAME, dataList.get(position).getItemName());
                args.putInt(TopMembersFragment.IMAGE_RESOURCE_ID, dataList.get(position).getImgResID());
//                actionBar.setNavigationMode(0);
//                // Remove System-Select Drop-Down
//                actionBar.setListNavigationCallbacks(null, null);
                mToolbar.setTitle(R.string.top_members_top_helping);
                isFragment = true;
                break;
            case DRAWER_SUBMIT_CHEAT:
                fragment = new SubmitCheatFragment();
                args.putString(SubmitCheatFragment.ITEM_NAME, dataList.get(position).getItemName());
                args.putInt(SubmitCheatFragment.IMAGE_RESOURCE_ID, dataList.get(position).getImgResID());
//                actionBar.setNavigationMode(0);
//                // Remove System-Select Drop-Down
//                actionBar.setListNavigationCallbacks(null, null);
                mToolbar.setTitle(R.string.submit_cheat_short);
                isFragment = true;
                break;
            case DRAWER_CONTACT:
                fragment = new ContactFormFragment();
                args.putString(ContactFormFragment.ITEM_NAME, dataList.get(position).getItemName());
                args.putInt(ContactFormFragment.IMAGE_RESOURCE_ID, dataList.get(position).getImgResID());
//                actionBar.setNavigationMode(0);
//                // Remove System-Select Drop-Down
//                actionBar.setListNavigationCallbacks(null, null);
                mToolbar.setTitle(R.string.contactform_title);
                isFragment = true;
                break;
            case DRAWER_MORE_APPS:
                Uri uri = Uri.parse(Konstanten.URL_MORE_APPS);
                Intent intentMoreApps = new Intent(Intent.ACTION_VIEW, uri);
                if (intentMoreApps.resolveActivity(getPackageManager()) != null) {
                    startActivity(intentMoreApps);
                } else {
                    Toast.makeText(MainActivity.this, R.string.err_other_problem, Toast.LENGTH_LONG).show();
                }
                break;
            case DRAWER_RATE_APP:
                Uri appUri = Uri.parse(Konstanten.GOOGLE_PLAY_URL);
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

            mDrawerList.setItemChecked(position, true);
            setTitle(dataList.get(position).getItemName());
            mDrawerLayout.closeDrawer(mDrawerList);
        }

    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        //getActionBar().setTitle(mTitle);
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
            editor.putInt(Konstanten.PREFERENCES_SELECTED_DRAWER_FRAGMENT_ID, position);
            editor.commit();
            selectItem(position);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        editor.putInt(Konstanten.PREFERENCES_SELECTED_DRAWER_FRAGMENT_ID, 0);
        editor.commit();
        AppBrain.getAds().maybeShowInterstitial(this);
        finish();
    }

}
