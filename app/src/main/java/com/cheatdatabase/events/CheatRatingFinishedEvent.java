package com.cheatdatabase.events;

import com.cheatdatabase.model.Cheat;

public class CheatRatingFinishedEvent {

    private Exception mException;
    private boolean mSucceeded;
    private Cheat mCheat;
    private int mRating;

    public CheatRatingFinishedEvent(Cheat cheat, int rating) {
        mSucceeded = true;
        mCheat = cheat;
        mRating = rating;
    }

    public CheatRatingFinishedEvent(Exception exception) {
        mSucceeded = false;
        mException = exception;
    }

    public Cheat getCheat() {
        return mCheat;
    }

    public int getRating() {
        return mRating;
    }

    public Exception getException() {
        return mException;
    }

    public boolean isSucceeded() {
        return mSucceeded;
    }
}
