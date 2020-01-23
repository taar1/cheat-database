package com.cheatdatabase.events;

import com.cheatdatabase.model.SystemPlatform;

/**
 * Created by Dominik on 01.06.2015.
 */
public class SystemListRecyclerViewClickEvent {

    private Exception mException;
    private boolean mSucceeded;
    private SystemPlatform systemPlatform;

    public SystemListRecyclerViewClickEvent(SystemPlatform systemPlatform) {
        mSucceeded = true;
        this.systemPlatform = systemPlatform;
    }

    public SystemListRecyclerViewClickEvent(Exception mException) {
        mSucceeded = false;
        this.mException = mException;
    }

    public SystemPlatform getSystemPlatform() {
        return systemPlatform;
    }

    public Exception getException() {
        return mException;
    }

    public boolean isSucceeded() {
        return mSucceeded;
    }
}
