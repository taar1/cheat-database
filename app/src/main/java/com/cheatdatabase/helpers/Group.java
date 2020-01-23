package com.cheatdatabase.helpers;

import com.cheatdatabase.model.Game;

import java.util.ArrayList;
import java.util.List;

public class Group {

    public String string;
    private Game game;

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public final List<String> children = new ArrayList<>();
    public final List<Game> gameChildren = new ArrayList<>();

    public Group(String string) {
        this.string = string;
    }

}