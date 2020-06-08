package com.cheatdatabase.activity.ui.mycheats

import com.cheatdatabase.data.model.UnpublishedCheat

interface MyUnpublishedCheatsListener {

    fun fetchUnpublishedCheatsSuccess(unpublishedCheats: List<UnpublishedCheat>)
    fun fetchUnpublishedCheatsFail(message: String)

    fun deleteUnpublishedCheatSuccess(message: String)
    fun deleteUnpublishedCheatFailed(message: String)
}