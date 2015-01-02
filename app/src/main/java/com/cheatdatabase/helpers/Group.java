package com.cheatdatabase.helpers;

import java.util.ArrayList;
import java.util.List;

import com.cheatdatabase.businessobjects.Game;

public class Group {

	public String string;
	private Game game;

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public final List<String> children = new ArrayList<String>();
	public final List<Game> gameChildren = new ArrayList<Game>();

	public Group(String string) {
		this.string = string;
	}

}