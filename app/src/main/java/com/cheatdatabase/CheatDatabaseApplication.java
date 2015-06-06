package com.cheatdatabase;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import de.greenrobot.event.EventBus;

/**
 * Created by Dominik on 06.06.2015.
 */
public class CheatDatabaseApplication extends Application {

    private static final String TAG = CheatDatabaseApplication.class.getSimpleName();

    private static EventBus sEventBus;
    private static Context sAppContext;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate called");

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
