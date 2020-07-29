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


@InstallIn(ApplicationComponent::class)
@Module
class NetworkModule {

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
            //.excludeFieldsWithoutExposeAnnotation()
            .setDateFormat("yyyy-MM-dd")
            .create()
    }

    @Provides
    @Singleton
    fun provideRestApiService(gson: Gson, okHttpClient: OkHttpClient): Retrofit {

        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(Konstanten.BASE_URL_REST)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): RestApi {
        return retrofit.create(RestApi::class.java)
    }

}