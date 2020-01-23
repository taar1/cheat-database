package com.cheatdatabase.taskresults;

import com.cheatdatabase.model.SystemPlatform;

import java.util.List;

/**
 * Created by Dominik on 31.05.2015.
 */
public class GamesAndCheatsCountTaskResult {

    private Exception mException;
    private boolean mSucceeded;
    private List<SystemPlatform> systemPlatforms;

    public GamesAndCheatsCountTaskResult(List<SystemPlatform> systemPlatforms) {
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

    public List<SystemPlatform> getSystemPlatforms() {
        return systemPlatforms;
    }
}
