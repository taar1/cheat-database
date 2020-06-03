package com.cheatdatabase.activity.ui.mycheats

import com.cheatdatabase.data.model.Member
import com.cheatdatabase.data.model.UnpublishedCheat
import com.cheatdatabase.rest.KotlinRestApi
import com.cheatdatabase.rest.SafeApiRequest
import retrofit2.Response

class UnpublishedCheatsRepositoryKotlin : SafeApiRequest() {

    suspend fun getMyUnpublishedCheats(
        memberId: Int,
        passwordMd5: String
    ): Response<List<UnpublishedCheat>> {
        return KotlinRestApi().getMyUnpublishedCheatsSuspended(memberId, passwordMd5)
    }

    suspend fun getTopMembers(): Response<List<Member>> {
        return KotlinRestApi().getTopMembers()
    }

    suspend fun getTopMembersUsingSafeApiRequest(): List<Member> {
        return apiRequest { KotlinRestApi().getTopMembers() }
    }
}