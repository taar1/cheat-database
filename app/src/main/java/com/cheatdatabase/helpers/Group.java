package com.cheatdatabase.helpers;

import com.cheatdatabase.model.Game;

import java.util.ArrayList;
import java.util.List;

public class Group {

    public String systemName;

    private Game game;

    public final List<String> children = new ArrayList<>();

    public final List<Game> gameChildren = new ArrayList<>();

    public Group(String systemName) {
        this.systemName = systemName;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}