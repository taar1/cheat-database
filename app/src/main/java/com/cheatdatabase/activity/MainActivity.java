package com.cheatdatabase.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.appbrain.AppBrain;
import com.cheatdatabase.R;
import com.cheatdatabase.activity.ui.mycheats.UnpublishedCheatsRepositoryKotlin;
import com.cheatdatabase.data.RetrofitClientInstance;
import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.dialogs.RateAppDialog;
import com.cheatdatabase.fragments.ContactFormFragment;
import com.cheatdatabase.fragments.FavoriteGamesListFragment;
import com.cheatdatabase.fragments.MyCheatsFragment;
import com.cheatdatabase.fragments.SystemListFragment;
import com.cheatdatabase.fragments.TopMembersFragment;
import com.cheatdatabase.helpers.AeSimpleMD5;
import com.cheatdatabase.helpers.DistinctValues;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.TrackingUtils;
import com.cheatdatabase.rest.RestApi;
import com.cheatdatabase.search.SearchSuggestionProvider;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.inmobi.ads.InMobiBanner;
import com.inmobi.sdk.InMobiSdk;

import java.security.NoSuchAlgorithmException;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private int mFragmentId;

    private AdView adView;

    private ActionBarDrawerToggle actionBarDrawerToggle;

    private Member member;
    private UnpublishedCheatsRepositoryKotlin.MyCheatsCount myCheatsCount;

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    private SearchManager searchManager;
    private SearchView searchView;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.add_new_cheat_button)
    FloatingActionButton floatingActionButton;
    @BindView(R.id.mixed_banner_container)
    LinearLayout mixedBannerContainer;
    @BindView(R.id.banner_container_facebook)
    LinearLayout bannerContainerFacebook;
    @BindView(R.id.banner_container_inmobi)
    LinearLayout bannerContainerInmobi;
    @BindView(R.id.inmobi_banner)
    InMobiBanner inMobiBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mFragmentId = getIntent().getIntExtra("mFragmentId", 0);

        init();
        prepareAdBanner();

        SystemListFragment fragment = SystemListFragment.newInstance(this);
        fragmentTransaction.replace(R.id.content_frame, fragment, SystemListFragment.class.getSimpleName()).commit();

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void init() {
        settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, MODE_PRIVATE);
        editor = settings.edit();

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        TrackingUtils.getInstance().init(this);

        AppBrain.init(this);
    }

    private void updateMyCheatsDrawerNavigationItemCount() {
        Menu menu = navigationView.getMenu();
        MenuItem navMyCheats = menu.findItem(R.id.nav_my_cheats);
        TextView myCheatsNavDrawerCounter = navMyCheats.getActionView().findViewById(R.id.nav_drawer_item_counter);

        if (myCheatsCount != null) {
            int allUnpublishedCheats = myCheatsCount.getUncheckedCheats() + myCheatsCount.getRejectedCheats();

            if (allUnpublishedCheats > 0) {
                myCheatsNavDrawerCounter.setText(getString(R.string.braces_with_text_in_the_middle, allUnpublishedCheats));
            } else {
                myCheatsNavDrawerCounter.setText("");
            }
        } else {
            myCheatsNavDrawerCounter.setText("");
        }

        refreshMyCheatsFragment();
    }

    private void refreshMyCheatsFragment() {
        // If you log out we are updating the text in "MyCheatsFragment" so we have to inform the fragment that the login-state has changed.
        Fragment myCheatsFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (myCheatsFragment != null) {
            if (myCheatsFragment instanceof MyCheatsFragment) {
                MyCheatsFragment mmyCheatsFragment = (MyCheatsFragment) myCheatsFragment;
                mmyCheatsFragment.setMyCheatsCount(myCheatsCount);
                mmyCheatsFragment.updateText();
            }
        }
    }

    /**
     * Display either InMobi or Facebook Audience Network banner (randomly)
     */
    private void prepareAdBanner() {
        Random r = new Random();
        int randomNumber = r.nextInt((1) + 1);

        if (randomNumber == 0) {
            Log.d(TAG, "Banner: Using InMobi Version: " + InMobiSdk.getVersion());

            inMobiBanner.setEnableAutoRefresh(true);
            inMobiBanner.load(this);

            bannerContainerFacebook.setVisibility(View.GONE);
            bannerContainerInmobi.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "Banner: Using Facebook Audience Network");

            bannerContainerInmobi.setVisibility(View.GONE);
            bannerContainerFacebook.setVisibility(View.VISIBLE);

            adView = new AdView(MainActivity.this, Konstanten.FACEBOOK_AUDIENCE_NETWORK_NATIVE_BANNER_ID, AdSize.BANNER_HEIGHT_50);
            bannerContainerFacebook.addView(adView);
            adView.loadAd();
        }
    }

    private void testCrash() {
        throw new RuntimeException("This is a crash");
    }

    @OnClick(R.id.add_new_cheat_button)
    void clickedAddNewCheatFloatingButton() {
        Intent explicitIntent = new Intent(this, SubmitCheatSelectGameActivity.class);
        startActivity(explicitIntent);
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
        countMyCheats();
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
            searchView = (SearchView) searchItem.getActionView();
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

                countMyCheats();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);

        if (resultCode == Konstanten.LOGIN_SUCCESS_RETURN_CODE) {
            Toast.makeText(MainActivity.this, R.string.login_ok, Toast.LENGTH_LONG).show();
        } else if (resultCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
            Toast.makeText(MainActivity.this, R.string.register_thanks, Toast.LENGTH_LONG).show();
        }

        countMyCheats();
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
        void showContactFormFragmentCallback();
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        EventBus.getDefault().register(this);
//    }
//
//    @Override
//    protected void onStop() {
//        EventBus.getDefault().unregister(this);
//        super.onStop();
//    }
//
//    @Subscribe
//    public void onEvent(GenericEvent event) {
//        Log.d(TAG, "XXXXX onEvent: 2222" );
//        if (event.getAction() == GenericEvent.Action.CLICK_CHEATS_DRAWER) {
//            showGameSystemsFragment();
//        }
//    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_gamesystems) {
            showGameSystemsFragment();
        } else if (id == R.id.nav_favorites) {
            fragmentTransaction.addToBackStack(FavoriteGamesListFragment.class.getSimpleName());
            fragmentManager.beginTransaction().replace(R.id.content_frame, FavoriteGamesListFragment.newInstance(), FavoriteGamesListFragment.class.getSimpleName()).commit();

            mToolbar.setTitle(R.string.favorites);
            floatingActionButton.hide();
        } else if (id == R.id.nav_members) {

            TopMembersFragment topMembersFragment = TopMembersFragment.newInstance(this);

            fragmentTransaction.addToBackStack(TopMembersFragment.class.getSimpleName());
            fragmentManager.beginTransaction().replace(R.id.content_frame, topMembersFragment, TopMembersFragment.class.getSimpleName()).commit();

            mToolbar.setTitle(R.string.top_members_top_helping);
            floatingActionButton.hide();
        } else if (id == R.id.nav_rate) {
            new RateAppDialog(this, () -> showContactFormFragment());
            return true;
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
        } else if (id == R.id.nav_terms_of_use) {
            mDrawerLayout.closeDrawers();

            MaterialDialog md = new MaterialDialog.Builder(this)
                    .customView(R.layout.layout_cheat_content_table, true)
                    .theme(Theme.DARK)
                    .positiveText(R.string.close)
                    .cancelable(true)
                    .show();

            View dialogView = md.getCustomView();
            WebView webview = dialogView.findViewById(R.id.webview);
            webview.loadUrl("https://www.freeprivacypolicy.com/privacy/view/1ac30e371af5decb7631a29e7eed2d15");
        } else if (id == R.id.nav_my_cheats) {
            showMyCheatsFragment();
        } else {
            showGameSystemsFragment();
        }

        mDrawerLayout.closeDrawers();

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();
        }

        return true;
    }

    private void showMyCheatsFragment() {
        mToolbar.setTitle(Html.fromHtml(getString(R.string.drawer_my_cheats)));
        fragmentTransaction.addToBackStack(MyCheatsFragment.class.getSimpleName());

        MyCheatsFragment fragment = new MyCheatsFragment(this, settings, myCheatsCount);

        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, MyCheatsFragment.class.getSimpleName()).commit();

        floatingActionButton.hide();
        mDrawerLayout.closeDrawers();
    }

    private void showContactFormFragment() {
        mToolbar.setTitle(R.string.contactform_title);
        fragmentTransaction.addToBackStack(ContactFormFragment.class.getSimpleName());

        ContactFormFragment fragment = ContactFormFragment.newInstance();
        fragment.setMainActivity(this);

        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, ContactFormFragment.class.getSimpleName()).commit();

        mixedBannerContainer.setVisibility(View.GONE);

        floatingActionButton.hide();
        mDrawerLayout.closeDrawers();
    }

    private void showGameSystemsFragment() {
        mToolbar.setTitle(R.string.app_name);
        fragmentTransaction.addToBackStack(SystemListFragment.class.getSimpleName());

        SystemListFragment fragment = SystemListFragment.newInstance(this);
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, SystemListFragment.class.getSimpleName()).commit();

        floatingActionButton.show();
        mDrawerLayout.closeDrawers();
    }

    /**
     * Loads the member's unpublished, rejected and published cheats count.
     *
     * @return UnpublishedCheatsRepositoryKotlin.MyCheatsCount
     */
    private void countMyCheats() {
        if ((member != null) && (member.getMid() != 0)) {
            RestApi restApi = RetrofitClientInstance.getRetrofitInstance().create(RestApi.class);
            Call<UnpublishedCheatsRepositoryKotlin.MyCheatsCount> call;
            try {
                //Log.d(TAG, "XXXXX countMyCheats: " + AeSimpleMD5.MD5(member.getPassword()));
                call = restApi.countMyCheats(member.getMid(), AeSimpleMD5.MD5(member.getPassword()));
                call.enqueue(new Callback<UnpublishedCheatsRepositoryKotlin.MyCheatsCount>() {
                    @Override
                    public void onResponse(Call<UnpublishedCheatsRepositoryKotlin.MyCheatsCount> countValue, Response<UnpublishedCheatsRepositoryKotlin.MyCheatsCount> response) {
                        if (response.isSuccessful()) {
                            myCheatsCount = response.body();
                        } else {
                            myCheatsCount = null;
                        }

                        updateMyCheatsDrawerNavigationItemCount();
                    }

                    @Override
                    public void onFailure(Call<UnpublishedCheatsRepositoryKotlin.MyCheatsCount> call, Throwable e) {
                        Log.e(TAG, "countMyCheats onFailure: " + e.getLocalizedMessage());
                        myCheatsCount = null;
                        updateMyCheatsDrawerNavigationItemCount();
                    }
                });
            } catch (NoSuchAlgorithmException e) {
                myCheatsCount = null;
                updateMyCheatsDrawerNavigationItemCount();
            }

        } else {
            myCheatsCount = null;
            updateMyCheatsDrawerNavigationItemCount();
        }
    }
}

