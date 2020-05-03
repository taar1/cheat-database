package com.cheatdatabase.listitems;

import com.cheatdatabase.data.model.Game;

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

    @Override
    public String getTitle() {
        return game.getGameName().substring(0, 1);
    }
}
