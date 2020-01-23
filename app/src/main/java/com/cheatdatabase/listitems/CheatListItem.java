package com.cheatdatabase.listitems;

import com.cheatdatabase.model.Cheat;

public class CheatListItem extends ListItem {

    private Cheat cheat;

    public Cheat getCheat() {
        return cheat;
    }

    public void setCheat(Cheat cheat) {
        this.cheat = cheat;
    }

    @Override
    public int getType() {
        return ListItem.TYPE_CHEAT;
    }

    @Override
    public String getTitle() {
        return cheat.getCheatTitle().substring(0, 1);
    }
}
