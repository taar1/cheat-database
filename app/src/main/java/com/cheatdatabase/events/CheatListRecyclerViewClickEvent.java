package com.cheatdatabase.events;

import com.cheatdatabase.model.Cheat;

public class CheatListRecyclerViewClickEvent {

    private Exception mException;
    private boolean mSucceeded;
    private Cheat mCheat;
    private int mPosition;

    public CheatListRecyclerViewClickEvent(Cheat cheat, int position) {
        mSucceeded = true;
        mCheat = cheat;
        mPosition = position;
    }

    public CheatListRecyclerViewClickEvent(Exception exception) {
        mSucceeded = false;
        mException = exception;
    }

    public Cheat getCheat() {
        return mCheat;
    }

    public int getPosition() {
        return mPosition;
    }

    public Exception getException() {
        return mException;
    }

    public boolean isSucceeded() {
        return mSucceeded;
    }
}
