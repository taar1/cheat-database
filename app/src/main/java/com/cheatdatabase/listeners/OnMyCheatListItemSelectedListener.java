package com.cheatdatabase.listeners;

import com.cheatdatabase.data.model.Cheat;

public interface OnMyCheatListItemSelectedListener {
    void onCheatListItemSelected(Cheat cheat, int position);

    void onCheatListItemEditSelected(Cheat cheat, int position);
}
