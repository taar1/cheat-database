package com.cheatdatabase.listeners;

import com.cheatdatabase.data.model.UnpublishedCheat;

public interface MyUnpublishedCheatsListItemSelectedListener {
    void onCheatClicked(UnpublishedCheat cheat);

    void onRejectReasonButtonClicked(UnpublishedCheat cheat);

    void onDeleteButtonClicked(UnpublishedCheat cheat);
}
