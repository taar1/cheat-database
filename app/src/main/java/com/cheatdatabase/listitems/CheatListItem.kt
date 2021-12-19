package com.cheatdatabase.listitems

import com.cheatdatabase.data.model.Cheat

class CheatListItem : ListItem() {
    var cheat: Cheat? = null

    override fun type(): Int {
        return TYPE_CHEAT
    }

    override fun title(): String? {
        return cheat?.cheatTitle?.substring(0, 1)
    }
}