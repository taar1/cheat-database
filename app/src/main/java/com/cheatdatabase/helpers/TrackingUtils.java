package com.cheatdatabase.helpers;


import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import com.cheatdatabase.CheatDatabaseApplication;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.text.Normalizer;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

/**
 * Helper class for tracking tasks
 */
public class TrackingUtils {
    private static final String LOG_TAG = "TrackingUtils";

    //    private FirebaseAnalytics firebaseAnalytics;
    private OkHttpClient okHttpClient;
    private static TrackingUtils _instance;
    private String lastPageID = null;

    /**
     * private constructor to avoid creating instances which are not the singleton instance
     */
    private TrackingUtils() {
    }

    /**
     * Get the instance of the Tracking Utils
     *
     * @return TrackingUtils
     */
    public static TrackingUtils getInstance() {
        if (_instance == null) {
            _instance = new TrackingUtils();
        }
        return _instance;
    }

    /**
     * Initialize the instance
     *
     * @param activity Activity
     */
    public void init(Activity activity) {
//        firebaseAnalytics = FirebaseAnalytics.getInstance(activity);
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool())
                .build();
    }

    /**
     * Helper function to reset the tracking controller when the app goes into background
     */
    public void reset() {
        lastPageID = null;
    }

    /**
     * Track a page view
     *
     * @param pageID   the name / id of the page
     * @param pageType the type of the page
     */
    public void trackPageView(String pageID, String pageType) {
        if (lastPageID != null && lastPageID.equals(pageID)) {
            return;
        }
        lastPageID = pageID;
        trackGoogleAnalyticsPageView(pageID);
        trackFirebasePageView(pageID, pageType, null);
    }

    /**
     * Track a page view without calling netmetrix
     *
     * @param pageID   the name / id of the page
     * @param pageType the type of the page
     */
    public void trackPageViewWithoutNetmetrix(String pageID, String pageType) {
        if (lastPageID != null && lastPageID.equals(pageID)) {
            return;
        }
        lastPageID = pageID;
        trackGoogleAnalyticsPageView(pageID);
        trackFirebasePageView(pageID, pageType, null);
    }

    /**
     * Track a page view
     *
     * @param pageID     the name / id of the page
     * @param pageType   the type of the page
     * @param entityName the name of the entity on the page
     */
    public void trackPageView(String pageID, String pageType, String entityName) {
        if (lastPageID != null && lastPageID.equals(pageID)) {
            return;
        }
        lastPageID = pageID;
        trackGoogleAnalyticsPageView(pageID);
        trackFirebasePageView(pageID, pageType, entityName);
    }

    /**
     * Track a page view without calling netmetrix
     *
     * @param pageID     the name / id of the page
     * @param pageType   the type of the page
     * @param entityName the name of the entity on the page
     */
    public void trackPageViewWithoutNetmetrix(String pageID, String pageType, String entityName) {
        if (lastPageID != null && lastPageID.equals(pageID)) {
            return;
        }
        lastPageID = pageID;
        trackGoogleAnalyticsPageView(pageID);
        trackFirebasePageView(pageID, pageType, entityName);
    }


    /**
     * Track a page view in Firebase
     *
     * @param pageID     the name / id of the page
     * @param pageType   the type of the page
     * @param entityName the name of the entity on the page
     */
    private void trackFirebasePageView(String pageID, String pageType, String entityName) {
        // Log the content view
        Bundle bundle = new Bundle();
//        if (pageID != null) {
//            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, pageID);
//        }
//        if (pageType != null) {
//            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, pageType);
//        }
//        if (entityName != null) {
//            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, entityName);
//        }
//
//        if ((pageID != null) && (pageType != null) && (entityName != null)) {
//            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
//        }
    }

    /**
     * Track a page view in Google Analytics
     *
     * @param pageID the name / id of the page
     */
    private void trackGoogleAnalyticsPageView(String pageID) {
        // Log the content view
        Tracker tracker = CheatDatabaseApplication.getCurrentAppInstance().getGoogleAnalyticsTracker();
        tracker.setScreenName(pageID);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }


    String slugify(String value, final String separator) {
        if (TextUtils.isEmpty(value)) {
            return "";
        }
        value = Normalizer.normalize(value.toLowerCase(Locale.FRANCE), Normalizer.Form.NFD).replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");

        final String[] splited = value.replaceAll(" ", separator)
                .replaceAll("\\\\t", "")
                .replaceAll("\\\\n", "")
                .replaceAll("\\\\r", "")
                .replaceAll("[^\\p{Alnum}]", separator)
                .split(separator);

        final StringBuilder result = new StringBuilder();
        boolean needSep = false;

        for (int i = 0; i < splited.length; i++) {
            if (!TextUtils.isEmpty(splited[i])) {
                if (needSep) {
                    result.append(separator);
                }
                result.append(splited[i]);
                needSep = true;
            }
        }
        return result.toString();
    }
}