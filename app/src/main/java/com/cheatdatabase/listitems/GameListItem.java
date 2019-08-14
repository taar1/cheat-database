package com.cheatdatabase.listitems;

import com.cheatdatabase.businessobjects.Game;

public class GameListItem extends ListItem {

    private Game game;

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public int getType() {
        return ListItem.TYPE_GAME;
    }
}
