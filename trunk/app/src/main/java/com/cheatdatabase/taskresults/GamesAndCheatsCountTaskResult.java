package com.cheatdatabase.taskresults;

import com.cheatdatabase.businessobjects.SystemPlatform;

import java.util.ArrayList;

/**
 * Created by Dominik on 31.05.2015.
 */
public class GamesAndCheatsCountTaskResult {

    private Exception mException;
    private boolean mSucceeded;
    private ArrayList<SystemPlatform> systemPlatforms;

    public GamesAndCheatsCountTaskResult(ArrayList<SystemPlatform> systemPlatforms) {
        mSucceeded = true;
        this.systemPlatforms = systemPlatforms;
    }

    public GamesAndCheatsCountTaskResult(Exception exception) {
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
