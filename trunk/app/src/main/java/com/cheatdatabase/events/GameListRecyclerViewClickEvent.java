package com.cheatdatabase.events;

import com.cheatdatabase.businessobjects.Game;

/**
 * Created by Dominik on 21.06.2015.
 */
public class GameListRecyclerViewClickEvent {

    private Exception mException;
    private boolean mSucceeded;
    private Game mGame;

    public GameListRecyclerViewClickEvent(Game game) {
        mSucceeded = true;
        mGame = game;
    }

    public GameListRecyclerViewClickEvent(Exception exception) {
        mSucceeded = false;
        mException = exception;
    }

    public Game getGame() {
        return mGame;
    }

    public Exception getException() {
        return mException;
    }

    public boolean isSucceeded() {
        return mSucceeded;
    }
}
