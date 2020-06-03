package com.cheatdatabase.rest

import com.cheatdatabase.data.model.Cheat
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.data.model.UnpublishedCheat
import com.cheatdatabase.helpers.Konstanten
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
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
    fun getMyUnpublishedCheats(
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
    fun deleteUnpublishedCheat(
        @Field("memberId") memberId: Int,
        @Field("pw") password_md5: String,
        @Field("id") id: Int,
        @Field("gameId") gameId: Int,
        @Field("tableInfo") tableInfo: String
    ): Response<JsonObject>


    @GET("getMemberTop20.php")
    suspend fun getTopMembers(): Response<List<Member>>

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