package com.cheatdatabase.activity.ui.mycheats

import com.cheatdatabase.data.model.HttpPostReturnValue
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.data.model.UnpublishedCheat
import com.cheatdatabase.helpers.AeSimpleMD5
import com.cheatdatabase.rest.KotlinRestApi
import com.cheatdatabase.rest.SafeApiRequest
import com.google.gson.annotations.SerializedName
import retrofit2.Response

class UnpublishedCheatsRepositoryKotlin : SafeApiRequest() {

    suspend fun getMyUnpublishedCheats(
        memberId: Int,
        passwordMd5: String
    ): Response<List<UnpublishedCheat>> {
        return KotlinRestApi().getMyUnpublishedCheatsSuspended(memberId, passwordMd5)
    }

    suspend fun deleteUnpublishedCheat(
        unpublishedCheat: UnpublishedCheat,
        member: Member
    ): Response<HttpPostReturnValue> {
        return KotlinRestApi().deleteUnpublishedCheat(
            member.mid,
            AeSimpleMD5.MD5(member.password),
            unpublishedCheat.cheatId,
            unpublishedCheat.game.gameId,
            unpublishedCheat.tableInfo
        )
    }

    suspend fun countMyCheats(
        memberId: Int,
        passwordMd5: String
    ): Response<MyCheatsCount> {
        return KotlinRestApi().countMyCheats(memberId, passwordMd5)
    }

    class MyCheatsCount {

        @SerializedName("cheat_submissions")
        val uncheckedCheats: Int = 0

        @SerializedName("rejected_cheats")
        val rejectedCheats: Int = 0

        @SerializedName("cheats_main")
        val publishedCheats: Int = 0
    }

    suspend fun getTopMembers(): Response<List<Member>> {
        return KotlinRestApi().getTopMembers()
    }

    suspend fun getTopMembersUsingSafeApiRequest(): List<Member> {
        return apiRequest { KotlinRestApi().getTopMembers() }
    }
}