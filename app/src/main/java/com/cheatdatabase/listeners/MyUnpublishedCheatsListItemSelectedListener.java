package com.cheatdatabase.listeners;

import com.cheatdatabase.data.model.UnpublishedCheat;

public interface MyUnpublishedCheatsListItemSelectedListener {
    void onRejectReasonButtonClicked(UnpublishedCheat cheat);

    void onEditCheatButtonClicked(UnpublishedCheat cheat);

    void onDeleteButtonClicked(UnpublishedCheat cheat);
}
