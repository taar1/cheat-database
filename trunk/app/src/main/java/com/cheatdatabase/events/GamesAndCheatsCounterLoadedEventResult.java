package com.cheatdatabase.events;

import com.cheatdatabase.businessobjects.SystemPlatform;

import java.util.ArrayList;

/**
 * Created by Dominik on 31.05.2015.
 */
public class GamesAndCheatsCounterLoadedEventResult {

    private Exception mException;
    private boolean mSucceeded;
    private ArrayList<SystemPlatform> systemPlatforms;

    public GamesAndCheatsCounterLoadedEventResult(ArrayList<SystemPlatform> systemPlatforms) {
        mSucceeded = true;
        this.systemPlatforms = systemPlatforms;
    }

    public GamesAndCheatsCounterLoadedEventResult(Exception exception) {
        mSucceeded = false;
        mException = exception;
    }

    public boolean isSucceeded() {
        return mSucceeded;
    }

    public Exception getException() {
        return mException;
    }

    public ArrayList<SystemPlatform> getSystemPlatforms() {
        return systemPlatforms;
    }
}
