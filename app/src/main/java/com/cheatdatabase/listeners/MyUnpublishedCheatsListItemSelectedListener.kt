package com.cheatdatabase.listeners

import com.cheatdatabase.data.model.UnpublishedCheat

interface MyUnpublishedCheatsListItemSelectedListener {
    fun onRejectReasonButtonClicked(cheat: UnpublishedCheat)
    fun onEditCheatButtonClicked(cheat: UnpublishedCheat)
    fun onDeleteButtonClicked(cheat: UnpublishedCheat, position: Int)
}