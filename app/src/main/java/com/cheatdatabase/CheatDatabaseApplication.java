package com.cheatdatabase;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.util.Log;

import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.helpers.Konstanten;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;

import org.androidannotations.annotations.EApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import io.fabric.sdk.android.Fabric;


/**
 * Created by Dominik on 06.06.2015.
 */
@EApplication
public class CheatDatabaseApplication extends Application {

    private static final String TAG = CheatDatabaseApplication.class.getSimpleName();

    //    private static EventBus sEventBus;
    private static Context sAppContext;

    TreeMap<String, TreeMap<String, Game[]>> gamesBySystemCached = new TreeMap<>();
    TreeMap<String, TreeMap<String, Cheat[]>> cheatsByGameCached = new TreeMap<>();


    public final String ACHIEVEMENTS = "achievements";
    public final String NO_ACHIEVEMENTS = "noAchievements";

    private static CheatDatabaseApplication currentApplicationInstance;
    private Tracker googleAnalyticsTracker;
    //    private FirebaseAnalytics firebaseAnalytics;
    private ConnectivityManager connectivityManager;

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
     * @return CinemanApplication
     */
    public static CheatDatabaseApplication getCurrentAppInstance() {
        return currentApplicationInstance;
    }


    // GOOGLE ANALYTICS EXAMPLE CODE
    // https://github.com/googleanalytics/hello-world-android-app/blob/master/app/src/main/java/com/example/googleanalyticshelloworld/MyApp.java

    /**
     * The Analytics singleton. The field is set in onCreate method override when the application
     * class is initially created.
     */
//    private static GoogleAnalytics analytics;

    /**
     * The default app tracker. The field is from onCreate callback when the application is
     * initially created.
     */
//    private static Tracker tracker;

    /**
     * Access to the global Analytics singleton. If this method returns null you forgot to either
     * set android:name="&lt;this.class.name&gt;" attribute on your application element in
     * AndroidManifest.xml or you are not setting this.analytics field in onCreate method override.
     */
//    public static GoogleAnalytics analytics() {
//        return analytics;
//    }

    /**
     * The default app tracker. If this method returns null you forgot to either set
     * android:name="&lt;this.class.name&gt;" attribute on your application element in
     * AndroidManifest.xml or you are not setting this.tracker field in onCreate method override.
     */
//    public static Tracker tracker() {
//        return tracker;
//    }
    @Override
    public void onCreate() {
        super.onCreate();
        currentApplicationInstance = this;
        Fabric.with(this, new Crashlytics());

        // Set firebase logging
//        firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());

//        analytics = GoogleAnalytics.getInstance(this);
//        // TODO: Replace the tracker-id with your app one from https://www.google.com/analytics/web/
//        tracker = analytics.newTracker(Konstanten.GOOGLE_ANALYTICS_ID);
//
//        // Provide unhandled exceptions reports. Do that first after creating the tracker
//        tracker.enableExceptionReporting(true);
//
//        // Enable Remarketing, Demographics & Interests reports
//        // https://developers.google.com/analytics/devguides/collection/android/display-features
//        tracker.enableAdvertisingIdCollection(true);
//
//        // Enable automatic activity tracking for your app
//        tracker.enableAutoActivityTracking(true);

        sAppContext = getApplicationContext();

//        initMopub();
    }

    private void initMopub() {
        // A list of rewarded video adapters to initialize
        List<String> networksToInit = new ArrayList<>();
        networksToInit.add("com.mopub.mobileads.VungleRewardedVideo");
        networksToInit.add("com.mopub.mobileads.AdColonyRewardedVideo");
        networksToInit.add("com.mopub.mobileads.FacebookRewardedVideo");
        networksToInit.add("com.mopub.mobileads.FacebookBanner");

//        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(Konstanten.MOPUB_PHONE_UNIT_ID)
//                .withNetworksToInit(networksToInit)
//                .build();

        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(Konstanten.MOPUB_PHONE_UNIT_ID)
                .build();

        MoPub.initializeSdk(this, sdkConfiguration, initSdkListener());
    }

    private SdkInitializationListener initSdkListener() {
        return new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {
                Log.d(TAG, "XXXXX MoPub SDK initialized");
           /* MoPub SDK initialized.
           Check if you should show the consent dialog here, and make your ad requests. */
            }
        };
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
