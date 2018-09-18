package com.cheatdatabase;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;

import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.TrackingUtils;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.androidannotations.annotations.EApplication;

import java.util.TreeMap;

import io.fabric.sdk.android.Fabric;

@EApplication
public class CheatDatabaseApplication extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = CheatDatabaseApplication.class.getSimpleName();

    private static Context sAppContext;

    TreeMap<String, TreeMap<String, Game[]>> gamesBySystemCached = new TreeMap<>();
    TreeMap<String, TreeMap<String, Cheat[]>> cheatsByGameCached = new TreeMap<>();

    public final String ACHIEVEMENTS = "achievements";
    public final String NO_ACHIEVEMENTS = "noAchievements";

    private static CheatDatabaseApplication currentApplicationInstance;
    private Tracker googleAnalyticsTracker;
    private FirebaseAnalytics firebaseAnalytics;
    private ConnectivityManager connectivityManager;

    private boolean isActivityVisible = false;

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
     * Get the current cineman app instance
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
    public TreeMap<String, TreeMap<String, Game[]>> getGamesBySystemCached() {
        return gamesBySystemCached;
    }

    /**
     * Naming convention: systemId, "achievements/noAchievements", Game[]
     *
     * @param gamesBySystemCached
     */
    public void setGamesBySystemCached(TreeMap<String, TreeMap<String, Game[]>> gamesBySystemCached) {
        this.gamesBySystemCached = gamesBySystemCached;
    }

    /**
     * Naming convention: gameId, "achievements/noAchievements", Cheat[]
     *
     * @return
     */
    public TreeMap<String, TreeMap<String, Cheat[]>> getCheatsByGameCached() {
        return cheatsByGameCached;
    }

    /**
     * Naming convention: gameId, "achievements/noAchievements", Cheat[]
     *
     * @param cheatsByGameCached
     */
    public void setCheatsByGameCached(TreeMap<String, TreeMap<String, Cheat[]>> cheatsByGameCached) {
        this.cheatsByGameCached = cheatsByGameCached;
    }
}
