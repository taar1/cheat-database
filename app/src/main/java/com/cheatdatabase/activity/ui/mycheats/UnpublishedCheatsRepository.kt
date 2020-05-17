package com.cheatdatabase.activity.ui.mycheats

import com.cheatdatabase.rest.KotlinRestApi
import com.cheatdatabase.rest.SafeApiRequest

class UnpublishedCheatsRepository(val api: KotlinRestApi) : SafeApiRequest() {

    suspend fun getMyUnpublishedCheats(memberId: Int, pw: String) =
        apiRequest(memberId, pw) { api.getMyUnpublishedCheats(memberId, pw) }

}