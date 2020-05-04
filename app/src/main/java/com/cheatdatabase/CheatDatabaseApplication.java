package com.cheatdatabase;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.cheatdatabase.activity.MainActivity;
import com.cheatdatabase.data.model.Cheat;
import com.cheatdatabase.data.model.Game;
import com.cheatdatabase.di.ApplicationModule;
import com.cheatdatabase.di.DaggerNetworkComponent;
import com.cheatdatabase.di.NetworkComponent;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.TrackingUtils;
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

import io.fabric.sdk.android.Fabric;

public class CheatDatabaseApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private NetworkComponent networkComponent;

    static TreeMap<String, TreeMap<String, List<Game>>> gamesBySystemCached = new TreeMap<>();
    static TreeMap<String, TreeMap<String, List<Cheat>>> cheatsByGameCached = new TreeMap<>();

    private static CheatDatabaseApplication currentApplicationInstance;
    private Tracker googleAnalyticsTracker;
    private FirebaseAnalytics firebaseAnalytics;


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

        networkComponent = DaggerNetworkComponent.builder().applicationModule(new ApplicationModule(this)).build();

        currentApplicationInstance = this;

        init();
    }

    public NetworkComponent getNetworkComponent() {
        return networkComponent;
    }

    private void init() {
        Fabric.with(this, new Crashlytics());

        // Setup lifecycle callbacks
        registerActivityLifecycleCallbacks(this);

        // Set firebase logging
        firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());

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
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (activity.getClass().equals(MainActivity.class)) {
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

    /**
     * Naming convention: systemId, "achievements/noAchievements", List<Game>
     *
     * @return
     */
    public static TreeMap<String, TreeMap<String, List<Game>>> getGamesBySystemCached() {
        return gamesBySystemCached;
    }

    /**
     * Naming convention: systemId, "achievements/noAchievements", List<Game>
     *
     * @param gamesBySystemCachedx
     */
    public static void setGamesBySystemCached(TreeMap<String, TreeMap<String, List<Game>>> gamesBySystemCachedx) {
        gamesBySystemCached = gamesBySystemCachedx;
    }

    /**
     * Naming convention: gameId, "achievements/noAchievements", List<Cheat>
     *
     * @return
     */
    public TreeMap<String, TreeMap<String, List<Cheat>>> getCheatsByGameCached() {
        return cheatsByGameCached;
    }

    /**
     * Naming convention: gameId, "achievements/noAchievements", List<Cheat>
     *
     * @param cheatsByGameCachedx
     */
    public void setCheatsByGameCached(TreeMap<String, TreeMap<String, List<Cheat>>> cheatsByGameCachedx) {
        cheatsByGameCached = cheatsByGameCachedx;
    }
}
