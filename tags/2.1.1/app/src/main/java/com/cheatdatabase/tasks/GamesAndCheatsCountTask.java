package com.cheatdatabase.tasks;

import android.util.Log;

import com.cheatdatabase.CheatDatabaseApplication;
import com.cheatdatabase.businessobjects.SystemPlatform;
import com.cheatdatabase.helpers.CheatDatabaseAdapter;
import com.cheatdatabase.helpers.SystemAndGameCountPrefs_;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.taskresults.GamesAndCheatsCountTaskResult;
import com.google.gson.Gson;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@EBean
public class GamesAndCheatsCountTask {

    private final String TAG = GamesAndCheatsCountTask.class.getSimpleName();

//    @Pref
//    SystemAndGameCountPrefs_ mPrefs;

    @Bean
    CheatDatabaseAdapter db;

    private GamesAndCheatsCountTaskResult gamesAndCheatsCountTaskResult;

    private ArrayList<SystemPlatform> systemGameandCheatCounterList = null;

    @Background
    public void loadGamesAndCheatsCounterBackground() {

        db.open();

        // SYSETM'S GAME COUNT WILL ONLY LOADED ONCE EVERY 24h
        boolean getSystemsAndCountsFromWebservice = false;

        ArrayList<SystemPlatform> systemsLocal = db.getAllSystemsAndCount();
        if (systemsLocal == null || systemsLocal.size() == 0) {
            getSystemsAndCountsFromWebservice = true;
        } else {
            // Check how old the database entries are. If over than 24 then
            // load them again from the webservice.

            long lastmod = systemsLocal.get(0).getLastModTimeStamp();

            long now = System.currentTimeMillis();
            long differenceInHours = (now - lastmod) / (1000 * 60 * 60);
            long differenceInMins = (now - lastmod) / (1000 * 60);

            Log.d(TAG, "NOW            : " + now);
            Log.d(TAG, "LAST UPDATE    : " + lastmod);
            Log.d(TAG, "DIFFERENCE in H: " + differenceInHours);
            Log.d(TAG, "DIFFERENCE in M: " + differenceInMins);

            if (differenceInHours > 23) {
                Log.d(TAG, "DIFFERENCE MORE THAN 23 HOURS. LOADING VALUES FROM WEBSERVICE AGAIN");
                getSystemsAndCountsFromWebservice = true;
            }
        }

        Log.d(TAG, "getSystemsAndCountsFromWebservice: " + getSystemsAndCountsFromWebservice);

        if (getSystemsAndCountsFromWebservice) {
            try {
                systemGameandCheatCounterList = Webservice.countGamesAndCheatsBySystem();
                Log.d(TAG, "Webservice countGamesAndCheatsBySystem() USED");

                // Update the local database
                db.updateSystemsAndCount(systemGameandCheatCounterList);
                Log.d(TAG, "SYSTEM VALUES FROM WEBSERVICE: " + new Gson().toJson(systemGameandCheatCounterList));

                if ((systemGameandCheatCounterList == null) || systemGameandCheatCounterList.size() == 0) {
                    db.deleteSystemsAndCount();
                    gamesAndCheatsCountTaskResult = new GamesAndCheatsCountTaskResult(new Exception());
                } else {
                    // Sorting
                    Collections.sort(systemGameandCheatCounterList, new Comparator<SystemPlatform>() {
                        @Override
                        public int compare(SystemPlatform system1, SystemPlatform system2) {
                            return system1.getSystemName().compareTo(system2.getSystemName());
                        }
                    });

                    gamesAndCheatsCountTaskResult = new GamesAndCheatsCountTaskResult(systemGameandCheatCounterList);
                }
            } catch (Exception e) {
                db.deleteSystemsAndCount();
                Log.e(TAG, "Load game and cheats counters failed: " + e.getLocalizedMessage());
                gamesAndCheatsCountTaskResult = new GamesAndCheatsCountTaskResult(new Exception());
            }

        } else {
            Log.d(TAG, "USED LOCAL DB DATA");
            gamesAndCheatsCountTaskResult = new GamesAndCheatsCountTaskResult(systemsLocal);
        }

        updateUI();
    }

    @UiThread
    void updateUI() {
        db.close();
        CheatDatabaseApplication.getEventBus().post(gamesAndCheatsCountTaskResult);
    }

}
