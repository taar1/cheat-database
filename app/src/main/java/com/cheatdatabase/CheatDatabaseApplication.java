package com.cheatdatabase;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;

import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.helpers.Konstanten;
import com.crashlytics.android.Crashlytics;

import org.androidannotations.annotations.EApplication;

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


    public  final String ACHIEVEMENTS = "achievements";
    public  final String NO_ACHIEVEMENTS = "noAchievements";

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
        Fabric.with(this, new Crashlytics());
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
     * @return
     */
    public TreeMap<String, TreeMap<String, Game[]>> getGamesBySystemCached() {
        return gamesBySystemCached;
    }

    /**
     * Naming convention: systemId, "achievements/noAchievements", Game[]
     * @param gamesBySystemCached
     */
    public void setGamesBySystemCached(TreeMap<String, TreeMap<String, Game[]>> gamesBySystemCached) {
        this.gamesBySystemCached = gamesBySystemCached;
    }

    /**
     * Naming convention: gameId, "achievements/noAchievements", Cheat[]
     * @return
     */
    public TreeMap<String, TreeMap<String, Cheat[]>> getCheatsByGameCached() {
        return cheatsByGameCached;
    }

    /**
     * Naming convention: gameId, "achievements/noAchievements", Cheat[]
     * @param cheatsByGameCached
     */
    public void setCheatsByGameCached(TreeMap<String, TreeMap<String, Cheat[]>> cheatsByGameCached) {
        this.cheatsByGameCached = cheatsByGameCached;
    }
}
