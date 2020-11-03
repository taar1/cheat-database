package com.cheatdatabase.rest

import com.cheatdatabase.data.model.*
import com.cheatdatabase.data.network.SystemContainer
import com.cheatdatabase.helpers.Konstanten
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface KotlinRestApi {

    /**
     * Gets all cheats from a member.
     *
     * @param memberId Member ID.
     * @return List<Cheat>
     */
    @FormUrlEncoded
    @POST("getCheatsByMemberId.php")
    fun getCheatsByMemberId(
        @Field("memberId") memberId: Int
    ): Deferred<List<Cheat>>

    /**
     * UNPUBLISHED CHEATS
     */
    @FormUrlEncoded
    @POST("myUnpublishedCheats.php")
    suspend fun getMyUnpublishedCheats(
        @Field("memberId") memberId: Int,
        @Field("pw") password_md5: String
    ): Response<List<UnpublishedCheat>>

    @FormUrlEncoded
    @POST("myUnpublishedCheats.php")
    suspend fun getMyUnpublishedCheatsSuspended(
        @Field("memberId") memberId: Int,
        @Field("pw") password_md5: String
    ): Response<List<UnpublishedCheat>>

    @FormUrlEncoded
    @POST("deleteMyUnpublishedCheat.php")
    suspend fun deleteUnpublishedCheat(
        @Field("memberId") memberId: Int,
        @Field("pw") password_md5: String,
        @Field("id") id: Int,
        @Field("gameId") gameId: Int,
        @Field("tableInfo") tableInfo: String
    ): Response<HttpPostReturnValue>

    @GET("getMemberTop20.php")
    suspend fun getTopMembers(): Response<List<Member>>

    @GET("countGamesAndCheatsOfAllSystems.php")
    suspend fun getSystems(): Response<List<SystemModel>>

    @GET("countGamesAndCheatsOfAllSystems.php")
    suspend fun getSystemsToContainer(): SystemContainer

    @FormUrlEncoded
    @POST("countMyCheats.php")
    suspend fun countMyCheats(
        @Field("memberId") memberId: Int,
        @Field("pw") password_md5: String
    ): Response<MyCheatsCount>

    companion object {
        operator fun invoke(): KotlinRestApi {
            val gsonBuilder = GsonBuilder()
            gsonBuilder.setDateFormat("yyyy-MM-dd HH:mm:ss")

            return Retrofit.Builder()
                .baseUrl(Konstanten.BASE_URL_REST)
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                .build()
                .create(KotlinRestApi::class.java)

        }
    }
}