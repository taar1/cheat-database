package com.cheatdatabase;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.appbrain.AppBrain;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.dialogs.RateAppDialog;
import com.cheatdatabase.events.GenericEvent;
import com.cheatdatabase.events.RemoteConfigLoadedEvent;
import com.cheatdatabase.fragments.ContactFormFragment_;
import com.cheatdatabase.fragments.FavoriteGamesListFragment_;
import com.cheatdatabase.fragments.SubmitCheatFragment_;
import com.cheatdatabase.fragments.SystemListFragment_;
import com.cheatdatabase.fragments.TopMembersFragment_;
import com.cheatdatabase.helpers.DistinctValues;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.MyPrefs_;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.TrackingUtils;
import com.cheatdatabase.navigationdrawer.CustomDrawerAdapter;
import com.cheatdatabase.navigationdrawer.DrawerItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.mopub.mobileads.MoPubView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Bean
    Tools tools;
    @Pref
    MyPrefs_ myPrefs;

    @Extra
    int mFragmentId;

    @ViewById(R.id.adview)
    MoPubView mAdView;
    @ViewById(R.id.toolbar)
    Toolbar mToolbar;
    @ViewById(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @ViewById(R.id.nav_view)
    NavigationView navigationView;
    @ViewById(R.id.add_new_cheat_button)
    FloatingActionButton fab;

    public static final String DRAWER_ITEM_ID = "drawerId";
    public static final String DRAWER_ITEM_NAME = "drawerName";

    // Navigation Drawer
    private ListView mDrawerList;

    private ActionBarDrawerToggle mDrawerToggle;

    List<DrawerItem> dataList;
    CustomDrawerAdapter mAdapter;

    private Member member;

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    private SearchManager searchManager;
    private SearchView searchView;


    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    // Remote Config keys
    private static final String REMOTE_CONFIG_HACKS_ENABLED_KEY = "hacks_enabled";
    private static final String REMOTE_CONFIG_IOS_ENABLED_KEY = "ios_enabled";
    private static final String REMOTE_CONFIG_ANDROID_ENABLED_KEY = "android_enabled";

    @AfterViews
    public void createView() {
        init();
        showAchievementsDialog();
        remoteConfigStuff();

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.app_icon_fox);

        tools.loadAd(mAdView, getString(R.string.screen_type));

        // TODO FIXME - find out where this part was before and re-add it.
        FragmentManager frgManager = getFragmentManager();
        frgManager.beginTransaction().replace(R.id.content_frame, SystemListFragment_.builder().build()).commit();
//        frgManager.beginTransaction().replace(R.id.content_frame, systemListFragment).commit();

        // Create Drawer
        // damit das zuletzt aktive fragment wieder angezeigt wird, wenn man auf "back" klickt.
//        createNavigationDrawer();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void init() {
        settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();

        // Initialize the Tracking Utils
        TrackingUtils.getInstance().init(this);

        AppBrain.init(this);
    }

    // https://firebase.google.com/docs/remote-config/use-config-android
    private void remoteConfigStuff() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);

        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        long cacheExpiration = 604800; // 1 week in seconds.
        // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
        // retrieve values from the service.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "XXXXX Fetch Succeeded");
                            Log.d(TAG, "XXXXX2: " + mFirebaseRemoteConfig.getBoolean(REMOTE_CONFIG_HACKS_ENABLED_KEY));

                            // TODO die remote config noch in den sharedpreferences speichern, damit man später drauf zugreifen kann
                            // TODO die remote config noch in den sharedpreferences speichern, damit man später drauf zugreifen kann
                            // TODO die remote config noch in den sharedpreferences speichern, damit man später drauf zugreifen kann
                            // TODO die remote config noch in den sharedpreferences speichern, damit man später drauf zugreifen kann
                            // TODO die remote config noch in den sharedpreferences speichern, damit man später drauf zugreifen kann
                            // TODO die remote config noch in den sharedpreferences speichern, damit man später drauf zugreifen kann

                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                            Log.d(TAG, "XXXXX3: " + mFirebaseRemoteConfig.getBoolean(REMOTE_CONFIG_HACKS_ENABLED_KEY));

                            // Tell SystemListFragment that the remote config has been loaded and reload the recyclerlistview
                            EventBus.getDefault().post(new RemoteConfigLoadedEvent());
                        } else {
                            Log.d(TAG, "XXXXX Fetch Failed");
                        }
                    }
                });
    }

    @Click(R.id.add_new_cheat_button)
    void clickedAddNewCheatFloatingButton() {
        FragmentManager annotationFragmentManager = getFragmentManager();
        mToolbar.setTitle(R.string.submit_cheat_short);
        annotationFragmentManager.beginTransaction().replace(R.id.content_frame, SubmitCheatFragment_.builder().build()).commit();
        fab.setVisibility(View.GONE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("mFragmentId", mFragmentId);
        editor.putInt(Konstanten.PREFERENCES_SELECTED_DRAWER_FRAGMENT_ID, mFragmentId);
        editor.apply();
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        Reachability.unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(this);
        }
        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        editor.putInt(Konstanten.PREFERENCES_SELECTED_DRAWER_FRAGMENT_ID, 0);
        editor.apply();
        super.onDestroy();
    }

    // http://developer.android.com/training/implementing-navigation/nav-drawer.html#Init
//    private void createNavigationDrawer() {
//        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.page_indicator_background));
//        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
//        mDrawerLayout.setDrawerListener(mDrawerToggle);
//        mDrawerToggle.syncState();
//
//        LinearLayout drawerHeaderLayout = new LinearLayout(this);
//        drawerHeaderLayout.setPadding(0, 40, 0, 60);
//        drawerHeaderLayout.setGravity(Gravity.CENTER);
//
//        ImageView drawerHeaderLogo = new ImageView(this);
//        drawerHeaderLogo.setImageResource(R.drawable.logo_full_small);
//        drawerHeaderLayout.addView(drawerHeaderLogo);
//
//        mDrawerList = findViewById(R.id.left_drawer);
//        mDrawerList.addHeaderView(drawerHeaderLayout);
//        mDrawerList.setHeaderDividersEnabled(true);
//
//        // set custom shadow that overlays main content when drawer opens
//        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
//
//
//        // Add Drawer Item to dataList
//        dataList = new ArrayList<>();
//        dataList.add(new DrawerItem(getString(R.string.goto_games_and_cheats), R.drawable.ic_drawer_cheats));
//        dataList.add(new DrawerItem(getString(R.string.favorites), R.drawable.ic_drawer_favorites));
//        dataList.add(new DrawerItem(getString(R.string.top_members_title), R.drawable.ic_drawer_members));
//        dataList.add(new DrawerItem(getString(R.string.submit_cheat_title), R.drawable.ic_drawer_upload));
//        dataList.add(new DrawerItem(getString(R.string.menu_more_apps), R.drawable.ic_drawer_more));
//        dataList.add(new DrawerItem(getString(R.string.rate_us), R.drawable.ic_drawer_rate));
//        dataList.add(new DrawerItem(getString(R.string.contactform_title), R.drawable.ic_drawer_contact));
//        dataList.add(new DrawerItem(getString(R.string.action_settings), R.drawable.ic_drawer_settings));
//
//        mAdapter = new CustomDrawerAdapter(this, R.layout.custom_drawer_item, dataList);
//        mDrawerList.setAdapter(mAdapter);
//
//        // Set the list's click listener
//        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
//
//        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
//
//            /** Called when a drawer has settled in a completely closed state. */
//            @Override
//            public void onDrawerClosed(View view) {
//                invalidateOptionsMenu();
//                syncState();
//            }
//
//            /** Called when a drawer has settled in a completely open state. */
//            @Override
//            public void onDrawerOpened(View drawerView) {
//                // creates call to onPrepareOptionsMenu()
//                invalidateOptionsMenu();
//                syncState();
//            }
//        };
//        mDrawerToggle.syncState();
//        // Set the drawer toggle as the DrawerListener
//        mDrawerLayout.setDrawerListener(mDrawerToggle);
//
//        int savedFragmentId = settings.getInt(Konstanten.PREFERENCES_SELECTED_DRAWER_FRAGMENT_ID, 0);
//        editor.putInt(Konstanten.PREFERENCES_SELECTED_DRAWER_FRAGMENT_ID, 0);
//        editor.apply();
//        if (savedFragmentId > 7) {
//            savedFragmentId = 0;
//        }
//        selectItem(savedFragmentId);
//    }

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

            MenuItem searchItem = menu.findItem(R.id.search);
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

        // If the nav drawer is open, hide action items related to the content view
//        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
//        menu.findItem(R.id.search).setVisible(!drawerOpen);

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

//    @Override
//    public boolean onNavigationItemSelected(int position, long id) {
//        FragmentManager frgManager = getFragmentManager();
//        frgManager.beginTransaction().replace(R.id.content_frame, SystemListFragment_.builder().build()).commit();
//
//        mDrawerList.setItemChecked(position, true);
//        setTitle(dataList.get(position).getItemName());
//        mDrawerLayout.closeDrawer(mDrawerList);
//
//        return true;
//    }

    @OnActivityResult(Konstanten.LOGIN_SUCCESS_RETURN_CODE)
    void onResult(int resultCode, Intent data) {
        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
        Toast.makeText(MainActivity.this, R.string.login_ok, Toast.LENGTH_LONG).show();
    }

    @OnActivityResult(Konstanten.REGISTER_SUCCESS_RETURN_CODE)
    void onResult(int resultCode) {
        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
        Toast.makeText(MainActivity.this, R.string.register_thanks, Toast.LENGTH_LONG).show();
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // The action bar home/up action should open or close the drawer.
//        // ActionBarDrawerToggle will take care of this.
//        if (mDrawerToggle.onOptionsItemSelected(item)) {
//            return true;
//        }
//
//        // Handle action buttons
//        switch (item.getItemId()) {
//            case R.id.action_clear_search_history:
//                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
//                suggestions.clearHistory();
//                Toast.makeText(MainActivity.this, R.string.search_history_cleared, Toast.LENGTH_LONG).show();
//                return true;
//            case R.id.action_login:
//                Intent loginIntent = new Intent(MainActivity.this, LoginActivity_.class);
//                startActivityForResult(loginIntent, Konstanten.LOGIN_REGISTER_OK_RETURN_CODE);
//                return true;
//            case R.id.action_logout:
//                member = null;
//                tools.logout(MainActivity.this, settings.edit());
//                invalidateOptionsMenu();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    // See strings.xml for menu list. Don't change the order!
//    public static final int DRAWER_MAIN = 1;
//    public static final int DRAWER_FAVORITES = 2;
//    public static final int DRAWER_TOP_MEMBERS = 3;
//    public static final int DRAWER_SUBMIT_CHEAT = 4;
//    public static final int DRAWER_MORE_APPS = 5;
//    public static final int DRAWER_RATE_APP = 6;
//    public static final int DRAWER_CONTACT = 7;
//    public static final int DRAWER_SETTINGS = 8;

    // update the main content by replacing fragments
//    public void selectItem(int position) {
//        Fragment fragment = null;
//        Bundle args = new Bundle();
//        boolean isFragment = false;
//
//        setMainTitle(position);
//
//        FragmentManager annotationFragmentManager = getFragmentManager();
//        switch (position) {
//            case DRAWER_MAIN:
//                // Log setting open event with category="ui", action="open", and label="settings"
//                //CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "open").setLabel("main").build());
//                annotationFragmentManager.beginTransaction().replace(R.id.content_frame, SystemListFragment_.builder().mDrawerId(position).mDrawerName(dataList.get(position).getItemName()).build()).commit();
//                isFragment = false;
//                break;
//            case DRAWER_FAVORITES:
//                //CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "open").setLabel("favorites").build());
//                annotationFragmentManager.beginTransaction().replace(R.id.content_frame, FavoriteGamesListFragment_.builder().mDrawerId(position).mDrawerName(dataList.get(position).getItemName()).build()).commit();
//                isFragment = false;
//                break;
//            case DRAWER_TOP_MEMBERS:
//                //CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "open").setLabel("top_members").build());
//                annotationFragmentManager.beginTransaction().replace(R.id.content_frame, TopMembersFragment_.builder().mDrawerId(position).mDrawerName(dataList.get(position).getItemName()).build()).commit();
//                isFragment = false;
//                break;
//            case DRAWER_SUBMIT_CHEAT:
//                //CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "open").setLabel("submit_cheat").build());
//                annotationFragmentManager.beginTransaction().replace(R.id.content_frame, SubmitCheatFragment_.builder().mDrawerId(position).mDrawerName(dataList.get(position).getItemName()).build()).commit();
//                isFragment = false;
//                break;
//            case DRAWER_CONTACT:
//                // If position = 8 -> out of bounds error. no idea why... it works like this here.
//                position = 6;
//                //CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "open").setLabel("contact").build());
//                annotationFragmentManager.beginTransaction().replace(R.id.content_frame, ContactFormFragment_.builder().mDrawerId(position).mDrawerName(dataList.get(position).getItemName()).build()).commit();
//                isFragment = false;
//                break;
//            case DRAWER_MORE_APPS:
//                //CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "open").setLabel("more_apps").build());
//                Uri uri = Uri.parse(DistinctValues.URL_MORE_APPS);
//                Intent intentMoreApps = new Intent(Intent.ACTION_VIEW, uri);
//                if (intentMoreApps.resolveActivity(getPackageManager()) != null) {
//                    startActivity(intentMoreApps);
//                } else {
//                    Toast.makeText(MainActivity.this, R.string.err_other_problem, Toast.LENGTH_LONG).show();
//                }
//                break;
//            case DRAWER_RATE_APP:
//                new RateAppDialog(this, new MainActivityCallbacks() {
//                    @Override
//                    public void showContactFormFrament(int drawerId) {
//                        selectItem(drawerId);
//                    }
//                });
//                break;
//            case DRAWER_SETTINGS:
//                SettingsActivity_.intent(this).start();
//                break;
//            default:
//                break;
//        }
//
//        if (isFragment) {
//            fragment.setArguments(args);
//            FragmentManager frgManager = getFragmentManager();
//            frgManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
//
//        }
//
//        int positionFixed = position - 1;
//        if (position - 1 < 0) {
//            positionFixed = 0;
//        }
//        mDrawerList.setItemChecked(positionFixed, true);
//        mDrawerLayout.closeDrawer(mDrawerList);
//    }

//    private void setMainTitle(int position) {
//        switch (position) {
//            case DRAWER_MAIN:
//                mToolbar.setTitle(R.string.app_name);
//                break;
//            case DRAWER_FAVORITES:
//                mToolbar.setTitle(R.string.favorites);
//                mToolbar.setTitle(R.string.favorites);
//                break;
//            case DRAWER_TOP_MEMBERS:
//                mToolbar.setTitle(R.string.top_members_top_helping);
//                break;
//            case DRAWER_RATE_APP:
//                break;
//            case DRAWER_SUBMIT_CHEAT:
//                mToolbar.setTitle(R.string.submit_cheat_short);
//                break;
//            case DRAWER_CONTACT:
//                mToolbar.setTitle(R.string.contactform_title);
//                break;
//            case DRAWER_SETTINGS:
//                mToolbar.setTitle(R.string.action_settings);
//                break;
//            default:
//                mToolbar.setTitle(R.string.app_name);
//                break;
//        }
//    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


//    private class DrawerItemClickListener implements ListView.OnItemClickListener {
//        @Override
//        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            Log.i("DRAWER", position + "");
//            mFragmentId = position;
//            selectItem(position);
//        }
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AppBrain.getAds().showOfferWall(this);
        finish();
    }

    public interface MainActivityCallbacks {
        void showContactFormFrament();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void onEvent(GenericEvent event) {
        if (event.getAction() == GenericEvent.Action.CLICK_CHEATS_DRAWER) {
            // TODO
            //selectItem(DRAWER_MAIN);
        }
    }

    @UiThread
    private void showAchievementsDialog() {
        // TODO text überarbeiten
        if (!myPrefs.isSeenAchievementsDialog().getOr(false)) {
            MaterialDialog md = new MaterialDialog.Builder(this)
                    .title(R.string.new_feature)
                    .content(R.string.disable_achievements_text)
                    .positiveText(R.string.ok)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            myPrefs.edit().isSeenAchievementsDialog().put(true).apply();
                        }
                    })
                    .theme(Theme.DARK)
                    .cancelable(false)
                    .show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        FragmentManager annotationFragmentManager = getFragmentManager();

        if (id == R.id.nav_gamesystems) {
            mToolbar.setTitle(R.string.app_name);
            annotationFragmentManager.beginTransaction().replace(R.id.content_frame, SystemListFragment_.builder().build()).commit();
            fab.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_favorites) {
            mToolbar.setTitle(R.string.favorites);
            annotationFragmentManager.beginTransaction().replace(R.id.content_frame, FavoriteGamesListFragment_.builder().build()).commit();
            fab.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_members) {
            mToolbar.setTitle(R.string.top_members_top_helping);
            annotationFragmentManager.beginTransaction().replace(R.id.content_frame, TopMembersFragment_.builder().build()).commit();
            fab.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_rate) {
            new RateAppDialog(this, new MainActivityCallbacks() {
                @Override
                public void showContactFormFrament() {
                    showContactFormFragment();
                }
            });
            mDrawerLayout.closeDrawers();
            return true;
        } else if (id == R.id.nav_submit) {
            mToolbar.setTitle(R.string.submit_cheat_short);
            annotationFragmentManager.beginTransaction().replace(R.id.content_frame, SubmitCheatFragment_.builder().build()).commit();
            fab.setVisibility(View.GONE);
        } else if (id == R.id.nav_contact) {
            showContactFormFragment();
        } else if (id == R.id.nav_settings) {
            SettingsActivity_.intent(this).start();
            mDrawerLayout.closeDrawers();
        } else if (id == R.id.nav_more_apps) {
            Uri uri = Uri.parse(DistinctValues.URL_MORE_APPS);
            Intent intentMoreApps = new Intent(Intent.ACTION_VIEW, uri);
            if (intentMoreApps.resolveActivity(getPackageManager()) != null) {
                startActivity(intentMoreApps);
            } else {
                Tools.showSnackbar(mDrawerLayout, getResources().getString(R.string.err_other_problem));
            }
            mDrawerLayout.closeDrawers();
        } else {
            mToolbar.setTitle(R.string.app_name);
            annotationFragmentManager.beginTransaction().replace(R.id.content_frame, SystemListFragment_.builder().build()).commit();
            fab.setVisibility(View.VISIBLE);
        }

        mDrawerLayout.closeDrawers();


        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();
        }

        return true;
    }

    private void showContactFormFragment() {
        FragmentManager annotationFragmentManager = getFragmentManager();
        mToolbar.setTitle(R.string.contactform_title);
        annotationFragmentManager.beginTransaction().replace(R.id.content_frame, ContactFormFragment_.builder().build()).commit();
        fab.setVisibility(View.GONE);
    }
}
