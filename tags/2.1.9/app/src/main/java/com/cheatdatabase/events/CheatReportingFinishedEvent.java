package com.cheatdatabase.events;

public class CheatReportingFinishedEvent {

    private Exception mException;
    private boolean mSucceeded;

    public CheatReportingFinishedEvent(boolean success) {
        mSucceeded = success;
    }

    public Exception getException() {
        return mException;
    }

    public boolean isSucceeded() {
        return mSucceeded;
    }
}
