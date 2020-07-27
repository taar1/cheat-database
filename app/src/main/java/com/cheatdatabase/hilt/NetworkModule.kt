package com.cheatdatabase.hilt

import com.cheatdatabase.helpers.Konstanten
import com.cheatdatabase.rest.RestApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
class NetworkModule {

    // https://www.youtube.com/watch?v=8vAQrgbh6YM
    // https://www.youtube.com/watch?v=8vAQrgbh6YM
    // https://www.youtube.com/watch?v=8vAQrgbh6YM
    // https://www.youtube.com/watch?v=8vAQrgbh6YM


    private val okHttpClient = OkHttpClient.Builder().build()
//    private val gson = GsonBuilder().setDateFormat("yyyy-MM-dd").create()

    @Provides
    @Singleton
    fun providesOkHttpClientBuilder(): OkHttpClient {
        return OkHttpClient
            .Builder()
            .build()
    }


    @Provides
    @Singleton
    fun providesGsonBuilder(): Gson {
        return GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .setDateFormat("yyyy-MM-dd")
            .create()
    }

    //    @Provides
//    @Singleton
    fun provideRestApiService(gson: Gson): RestApi? {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(Konstanten.BASE_URL_REST)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(RestApi::class.java)
    }


//    private var retrofit: Retrofit? = null
//    private val okHttpClient = OkHttpClient.Builder().build()
//    private val gson = GsonBuilder().setDateFormat("yyyy-MM-dd").create()
//
//    fun getRetrofitInstance(): Retrofit? {
//        if (retrofit == null) {
//            retrofit = Retrofit.Builder()
//                .client(okHttpClient)
//                .baseUrl(Konstanten.BASE_URL_REST)
//                .addConverterFactory(GsonConverterFactory.create(gson))
//                .build()
//        }
//        return retrofit
//    }

}