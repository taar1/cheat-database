package com.cheatdatabase.data.repository

import com.cheatdatabase.data.model.HttpPostReturnValue
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.data.model.MyCheatsCount
import com.cheatdatabase.data.model.UnpublishedCheat
import com.cheatdatabase.rest.KotlinRestApi
import com.cheatdatabase.rest.SafeApiRequest
import retrofit2.Response

class MyCheatsRepository : SafeApiRequest() {

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
            member.passwordMd5,
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

    suspend fun getTopMembers(): Response<List<Member>> {
        return KotlinRestApi().getTopMembers()
    }

    suspend fun getTopMembersUsingSafeApiRequest(): List<Member> {
        return apiRequest { KotlinRestApi().getTopMembers() }
    }
}