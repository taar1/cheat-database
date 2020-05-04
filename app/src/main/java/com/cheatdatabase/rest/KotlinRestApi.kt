package com.cheatdatabase.rest

import com.cheatdatabase.data.model.Cheat
import com.cheatdatabase.helpers.Konstanten
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
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


    companion object {
        operator fun invoke(): KotlinRestApi {

//            val requestInterceptor = Interceptor { chain ->
//                val url = chain.request()
//                        .url
//                        .newBuilder()
//                        .build()
//
//                val request = chain.request()
//                        .newBuilder()
//                        .url(url)
//                        .build()
//
//                return@Interceptor chain.proceed(request)
//            }

            val okHttpClient = OkHttpClient.Builder()
                    //.addInterceptor(requestInterceptor)
                    .build()

            return Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl(Konstanten.BASE_URL_REST)
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(KotlinRestApi::class.java)

        }
    }
}