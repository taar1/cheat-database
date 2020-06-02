package com.cheatdatabase.activity.ui.mycheats

import com.cheatdatabase.data.model.UnpublishedCheat
import com.cheatdatabase.rest.KotlinRestApi
import retrofit2.Response

class UnpublishedCheatsRepositoryKotlin {

    suspend fun getMyUnpublishedCheats(
        memberId: Int,
        passwordMd5: String
    ): Response<List<UnpublishedCheat>> {
        // TODO FIXME hier gibts ein fehler wenn man die cheats laden will.
        // TODO FIXME https://www.youtube.com/watch?v=EQvLP5BThZ0 das video nochmals genau anschauen beim API teil
        // TODO FIXME vermutlich fehlt eine implementation von einer helferklasse...?

        return KotlinRestApi().getMyUnpublishedCheats(memberId, passwordMd5)
    }

}