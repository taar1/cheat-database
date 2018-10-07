package com.cheatdatabase;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.LinearLayout;
import android.widget.Toast;

import com.appbrain.AppBrain;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.dialogs.RateAppDialog;
import com.cheatdatabase.events.GenericEvent;
import com.cheatdatabase.fragments.ContactFormFragment_;
import com.cheatdatabase.fragments.FavoriteGamesListFragment;
import com.cheatdatabase.fragments.FavoriteGamesListFragment_;
import com.cheatdatabase.fragments.SubmitCheatFragment;
import com.cheatdatabase.fragments.SubmitCheatFragment_;
import com.cheatdatabase.fragments.SystemListFragment_;
import com.cheatdatabase.fragments.TopMembersFragment;
import com.cheatdatabase.helpers.DistinctValues;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.TrackingUtils;
import com.cheatdatabase.search.SearchSuggestionProvider;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private int mFragmentId;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.add_new_cheat_button)
    FloatingActionButton fab;
    @BindView(R.id.banner_container)
    LinearLayout facebookBanner;
    private AdView adView;

    public static final String DRAWER_ITEM_ID = "drawerId";
    public static final String DRAWER_ITEM_NAME = "drawerName";

    private ActionBarDrawerToggle actionBarDrawerToggle;

    private Member member;

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    private SearchManager searchManager;
    private SearchView searchView;

//    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    // Remote Config keys
    private static final String REMOTE_CONFIG_HACKS_ENABLED_KEY = "hacks_enabled";
    private static final String REMOTE_CONFIG_IOS_ENABLED_KEY = "ios_enabled";
    private static final String REMOTE_CONFIG_ANDROID_ENABLED_KEY = "android_enabled";
    private android.support.v4.app.FragmentManager fragmentManager;
    private FragmentTransaction ft;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFragmentId = getIntent().getIntExtra("mFragmentId", 0);

        init();

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.app_icon_fox);

        adView = new AdView(MainActivity.this, Konstanten.FACEBOOK_AUDIENCE_NETWORK_NATIVE_BANNER_ID, AdSize.BANNER_HEIGHT_50);
        facebookBanner.addView(adView);
        adView.loadAd();

        fragmentManager.beginTransaction().replace(R.id.content_frame, SystemListFragment_.builder().build()).commit();

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void init() {
        settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();

        fragmentManager = getSupportFragmentManager();
        ft = fragmentManager.beginTransaction();

        TrackingUtils.getInstance().init(this);

        AppBrain.init(this);
    }

    private void testCrash() {
        throw new RuntimeException("This is a crash");
    }

    // https://firebase.google.com/docs/remote-config/use-config-android
    private void remoteConfigStuff() {
//        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
//        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
//                .setDeveloperModeEnabled(BuildConfig.DEBUG)
//                .build();
//        mFirebaseRemoteConfig.setConfigSettings(configSettings);
//
//        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
//
//        long cacheExpiration = 604800; // 1 week in seconds.
//        // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
//        // retrieve values from the service.
//        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
//            cacheExpiration = 0;
//        }
//
//        mFirebaseRemoteConfig.fetch(cacheExpiration)
//                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            Log.d(TAG, "XXXXX Fetch Succeeded");
//                            Log.d(TAG, "XXXXX2: " + mFirebaseRemoteConfig.getBoolean(REMOTE_CONFIG_HACKS_ENABLED_KEY));
//
//                            // TODO die remote config noch in den sharedpreferences speichern, damit man später drauf zugreifen kann
//                            // TODO die remote config noch in den sharedpreferences speichern, damit man später drauf zugreifen kann
//                            // TODO die remote config noch in den sharedpreferences speichern, damit man später drauf zugreifen kann
//                            // TODO die remote config noch in den sharedpreferences speichern, damit man später drauf zugreifen kann
//                            // TODO die remote config noch in den sharedpreferences speichern, damit man später drauf zugreifen kann
//                            // TODO die remote config noch in den sharedpreferences speichern, damit man später drauf zugreifen kann
//
//                            // After config data is successfully fetched, it must be activated before newly fetched
//                            // values are returned.
//                            mFirebaseRemoteConfig.activateFetched();
//                            Log.d(TAG, "XXXXX3: " + mFirebaseRemoteConfig.getBoolean(REMOTE_CONFIG_HACKS_ENABLED_KEY));
//
//                            // Tell SystemListFragment that the remote config has been loaded and reload the recyclerlistview
//                            EventBus.getDefault().post(new RemoteConfigLoadedEvent());
//                        } else {
//                            Log.d(TAG, "XXXXX Fetch Failed");
//                        }
//                    }
//                });
    }

    @OnClick(R.id.add_new_cheat_button)
    void clickedAddNewCheatFloatingButton() {
        mToolbar.setTitle(R.string.submit_cheat_short);
        fragmentManager.beginTransaction().replace(R.id.content_frame, SubmitCheatFragment_.builder().build()).commit();
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
        if (adView != null) {
            adView.destroy();
        }
        editor.putInt(Konstanten.PREFERENCES_SELECTED_DRAWER_FRAGMENT_ID, 0);
        editor.apply();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
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
            Log.e(TAG, e.getLocalizedMessage());
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

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
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

//    @OnActivityResult(Konstanten.LOGIN_SUCCESS_RETURN_CODE)
//    void onResult(int resultCode, Intent data) {
//        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
//        Toast.makeText(MainActivity.this, R.string.login_ok, Toast.LENGTH_LONG).show();
//    }
//
//    @OnActivityResult(Konstanten.REGISTER_SUCCESS_RETURN_CODE)
//    void onResult(int resultCode) {
//        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
//        Toast.makeText(MainActivity.this, R.string.register_thanks, Toast.LENGTH_LONG).show();
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);

        if (resultCode == Konstanten.LOGIN_SUCCESS_RETURN_CODE) {
            Toast.makeText(MainActivity.this, R.string.login_ok, Toast.LENGTH_LONG).show();
        } else if (resultCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
            Toast.makeText(MainActivity.this, R.string.register_thanks, Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggle
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

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
            showGameSystemsFragment();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_gamesystems) {
            showGameSystemsFragment();
        } else if (id == R.id.nav_favorites) {
            mToolbar.setTitle(R.string.favorites);
            fragmentManager.beginTransaction().replace(R.id.content_frame, FavoriteGamesListFragment.builder().build()).commit();
            fab.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_members) {
            mToolbar.setTitle(R.string.top_members_top_helping);
            ft.addToBackStack(TopMembersFragment.class.getSimpleName());
            fragmentManager.beginTransaction().replace(R.id.content_frame, TopMembersFragment.newInstance(), TopMembersFragment.class.getSimpleName()).commit();

            fab.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_rate) {
            new RateAppDialog(this, new MainActivityCallbacks() {
                @Override
                public void showContactFormFrament() {
                    showContactFormFragment();
                }
            });
            return true;
        } else if (id == R.id.nav_submit) {
            mToolbar.setTitle(R.string.submit_cheat_short);
            fragmentManager.beginTransaction().replace(R.id.content_frame, SubmitCheatFragment.builder().build()).commit();
            fab.setVisibility(View.GONE);
        } else if (id == R.id.nav_contact) {
            showContactFormFragment();
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(MainActivity.this, AppPreferencesActivity.class));
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
            showGameSystemsFragment();
        }

        mDrawerLayout.closeDrawers();

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();
        }

        return true;
    }

    private void showContactFormFragment() {
        mToolbar.setTitle(R.string.contactform_title);
        fragmentManager.beginTransaction().replace(R.id.content_frame, ContactFormFragment_.builder().build()).commit();
        fab.setVisibility(View.GONE);
        mDrawerLayout.closeDrawers();
    }

    private void showGameSystemsFragment() {
        mToolbar.setTitle(R.string.app_name);
        fragmentManager.beginTransaction().replace(R.id.content_frame, SystemListFragment_.builder().build()).commit();
        fab.setVisibility(View.VISIBLE);
        mDrawerLayout.closeDrawers();
    }
}

