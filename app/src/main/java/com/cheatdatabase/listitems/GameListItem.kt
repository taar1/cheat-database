package com.cheatdatabase.listitems

import com.cheatdatabase.data.model.Game

class GameListItem : ListItem() {
    var game: Game? = null

    override fun type(): Int {
        return TYPE_GAME
    }

    override fun title(): String? {
        return game?.gameName?.substring(0, 1)
    }
}