package com.cheatdatabase.tasks;

import android.util.Log;

import com.cheatdatabase.businessobjects.SystemPlatform;
import com.cheatdatabase.helpers.CheatDatabaseAdapter;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.taskresults.GamesAndCheatsCountTaskResult;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import needle.Needle;

public class GamesAndCheatsCountTask {

    private final String TAG = GamesAndCheatsCountTask.class.getSimpleName();

//    @Pref
//    SystemAndGameCountPrefs_ mPrefs;

    CheatDatabaseAdapter db;

    private GamesAndCheatsCountTaskResult gamesAndCheatsCountTaskResult;

    private List<SystemPlatform> systemGameandCheatCounterList = null;

    public void loadGamesAndCheatsCounterBackground() {

        // TODO USED AT ALL?
        // TODO USED AT ALL?
        // TODO USED AT ALL?
        // TODO USED AT ALL?

        Needle.onBackgroundThread().execute(() -> {
            db.open();

            // SYSETM'S GAME COUNT WILL ONLY LOADED ONCE EVERY 24h
            boolean getSystemsAndCountsFromWebservice = false;

            List<SystemPlatform> systemsLocal = db.getAllSystemsAndCount();
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
        });

    }

    private void updateUI() {
        Needle.onMainThread().execute(() -> {
            db.close();
            EventBus.getDefault().post(gamesAndCheatsCountTaskResult);
        });

    }

}
