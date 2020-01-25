package com.cheatdatabase;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.cheatdatabase.activity.MainActivity;
import com.cheatdatabase.di.DaggerAppComponent;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.TrackingUtils;
import com.cheatdatabase.model.Cheat;
import com.cheatdatabase.model.Game;
import com.crashlytics.android.Crashlytics;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.inmobi.sdk.InMobiSdk;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.TreeMap;

import dagger.android.AndroidInjector;
import dagger.android.HasAndroidInjector;
import dagger.android.support.DaggerApplication;
import io.fabric.sdk.android.Fabric;

public class CheatDatabaseApplication extends DaggerApplication implements HasAndroidInjector, Application.ActivityLifecycleCallbacks {

    private static final String TAG = CheatDatabaseApplication.class.getSimpleName();

    private static Context sAppContext;

    static TreeMap<String, TreeMap<String, List<Game>>> gamesBySystemCached = new TreeMap<>();
    static TreeMap<String, TreeMap<String, List<Cheat>>> cheatsByGameCached = new TreeMap<>();

    private static CheatDatabaseApplication currentApplicationInstance;
    private Tracker googleAnalyticsTracker;
    private FirebaseAnalytics firebaseAnalytics;
    private ConnectivityManager connectivityManager;

    private boolean isActivityVisible = false;

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerAppComponent.builder().build();
//        return null;
    }

//    @Override
//    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
//        // TODO FIXME ???
//        return null;
//    }

    /**
     * Gets the default {@link Tracker} for this {@link CheatDatabaseApplication}.
     *
     * @return Tracker
     */
    synchronized public Tracker getGoogleAnalyticsTracker() {
        if (googleAnalyticsTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            googleAnalyticsTracker = analytics.newTracker(Konstanten.GOOGLE_ANALYTICS_ID);
        }
        return googleAnalyticsTracker;
    }

    /**
     * Get the current cineman cheatDatabaseApplication instance
     *
     * @return CheatDatabaseApplication
     */
    public static CheatDatabaseApplication getCurrentAppInstance() {
        return currentApplicationInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        currentApplicationInstance = this;
        sAppContext = getApplicationContext();

        init();
    }


    private void init() {
        Fabric.with(this, new Crashlytics());

        // Setup lifecycle callbacks
        registerActivityLifecycleCallbacks(this);

        // Set firebase logging
        firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());

        // Set the connectivity manager
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Facebook Native Ads
        // Initialize the Audience Network SDK
        AudienceNetworkAds.initialize(this);

        // Init InMobi Ads
        JSONObject consentObject = new JSONObject();
        try {
            // Provide correct consent value to sdk which is obtained by User
            consentObject.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE, true);
            // Provide 0 if GDPR is not applicable and 1 if applicable
            consentObject.put("gdpr", "0");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        InMobiSdk.init(this, Konstanten.INMOBI_CHEATDATABASE_APP_ID, consentObject);
        InMobiSdk.setAgeGroup(InMobiSdk.AgeGroup.BETWEEN_18_AND_24);
        InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (activity.getClass().equals(MainActivity.class)) {
            isActivityVisible = true;
        }
    }

    /**
     * {@link Application.ActivityLifecycleCallbacks#onActivityStarted(Activity)}
     */
    @Override
    public void onActivityStarted(Activity activity) {
        if (activity.getClass().equals(MainActivity.class)) {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity.getClass().equals(MainActivity.class)) {
            isActivityVisible = true;
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (activity.getClass().equals(MainActivity.class)) {
            isActivityVisible = false;
            TrackingUtils.getInstance().reset();
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public static Context getAppContext() {
        return sAppContext;
    }

    public static Typeface getFontBold() {
        return Typeface.createFromAsset(getAppContext().getAssets(), Konstanten.FONT_BOLD);
    }

    public static Typeface getFontLight() {
        return Typeface.createFromAsset(getAppContext().getAssets(), Konstanten.FONT_LIGHT);
    }

    public static Typeface getFontRegular() {
        return Typeface.createFromAsset(getAppContext().getAssets(), Konstanten.FONT_REGULAR);
    }

    /**
     * Naming convention: systemId, "achievements/noAchievements", Game[]
     *
     * @return
     */
    public static TreeMap<String, TreeMap<String, List<Game>>> getGamesBySystemCached() {
        return gamesBySystemCached;
    }

    /**
     * Naming convention: systemId, "achievements/noAchievements", Game[]
     *
     * @param gamesBySystemCachedx
     */
    public static void setGamesBySystemCached(TreeMap<String, TreeMap<String, List<Game>>> gamesBySystemCachedx) {
        gamesBySystemCached = gamesBySystemCachedx;
    }

    /**
     * Naming convention: gameId, "achievements/noAchievements", Cheat[]
     *
     * @return
     */
    public TreeMap<String, TreeMap<String, List<Cheat>>> getCheatsByGameCached() {
        return cheatsByGameCached;
    }

    /**
     * Naming convention: gameId, "achievements/noAchievements", Cheat[]
     *
     * @param cheatsByGameCachedx
     */
    public void setCheatsByGameCached(TreeMap<String, TreeMap<String, List<Cheat>>> cheatsByGameCachedx) {
        cheatsByGameCached = cheatsByGameCachedx;
    }
}
