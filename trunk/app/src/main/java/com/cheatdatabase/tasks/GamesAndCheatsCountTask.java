package com.cheatdatabase.tasks;

import android.util.Log;

import com.cheatdatabase.CheatDatabaseApplication;
import com.cheatdatabase.businessobjects.SystemPlatform;
import com.cheatdatabase.taskresults.GamesAndCheatsCountTaskResult;
import com.cheatdatabase.helpers.Webservice;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@EBean
public class GamesAndCheatsCountTask {

    private final String TAG = GamesAndCheatsCountTask.class.getSimpleName();

    private GamesAndCheatsCountTaskResult gamesAndCheatsCountTaskResult;

    @Background
    public void loadGamesAndCheatsCounterBackground() {

        ArrayList<SystemPlatform> systemGameandCheatCounterList = null;
        try {
            systemGameandCheatCounterList = Webservice.countGamesAndCheatsBySystem();
        } catch (Exception e) {
            Log.e(TAG, "Load game and cheats counters failed: " + e.getLocalizedMessage());
        }

        if ((systemGameandCheatCounterList == null) || systemGameandCheatCounterList.size() == 0) {
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
        updateUI();
    }

    @UiThread
    void updateUI() {
        CheatDatabaseApplication.getEventBus().post(gamesAndCheatsCountTaskResult);
    }

}
