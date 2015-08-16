package com.cheatdatabase;

import android.app.Application;
import android.content.Context;

import com.cheatdatabase.helpers.Konstanten;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import de.greenrobot.event.EventBus;

/**
 * Created by Dominik on 06.06.2015.
 */
public class CheatDatabaseApplication extends Application {

    private static final String TAG = CheatDatabaseApplication.class.getSimpleName();

    private static EventBus sEventBus;
    private static Context sAppContext;

    // GOOGLE ANALYTICS EXAMPLE CODE
    // https://github.com/googleanalytics/hello-world-android-app/blob/master/app/src/main/java/com/example/googleanalyticshelloworld/MyApp.java

    /**
     * The Analytics singleton. The field is set in onCreate method override when the application
     * class is initially created.
     */
    private static GoogleAnalytics analytics;

    /**
     * The default app tracker. The field is from onCreate callback when the application is
     * initially created.
     */
    private static Tracker tracker;

    /**
     * Access to the global Analytics singleton. If this method returns null you forgot to either
     * set android:name="&lt;this.class.name&gt;" attribute on your application element in
     * AndroidManifest.xml or you are not setting this.analytics field in onCreate method override.
     */
    public static GoogleAnalytics analytics() {
        return analytics;
    }

    /**
     * The default app tracker. If this method returns null you forgot to either set
     * android:name="&lt;this.class.name&gt;" attribute on your application element in
     * AndroidManifest.xml or you are not setting this.tracker field in onCreate method override.
     */
    public static Tracker tracker() {
        return tracker;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        analytics = GoogleAnalytics.getInstance(this);
        // TODO: Replace the tracker-id with your app one from https://www.google.com/analytics/web/
        tracker = analytics.newTracker(Konstanten.GOOGLE_ANALYTICS_ID);

        // Provide unhandled exceptions reports. Do that first after creating the tracker
        tracker.enableExceptionReporting(true);

        // Enable Remarketing, Demographics & Interests reports
        // https://developers.google.com/analytics/devguides/collection/android/display-features
        tracker.enableAdvertisingIdCollection(true);

        // Enable automatic activity tracking for your app
        tracker.enableAutoActivityTracking(true);

        sAppContext = getApplicationContext();
        sEventBus = EventBus.builder().throwSubscriberException(BuildConfig.DEBUG).installDefaultEventBus();
    }

    public static Context getAppContext() {
        return sAppContext;
    }

    public static EventBus getEventBus() {
        return sEventBus;
    }

}
