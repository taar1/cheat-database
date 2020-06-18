package com.cheatdatabase.activity.ui.mycheats

import com.cheatdatabase.data.model.UnpublishedCheat

interface MyUnpublishedCheatsListener {

    fun fetchUnpublishedCheatsSuccess(unpublishedCheats: List<UnpublishedCheat>)
    fun fetchUnpublishedCheatsFail()

    fun deleteUnpublishedCheatSuccess(message: String)
    fun deleteUnpublishedCheatFailed()
}