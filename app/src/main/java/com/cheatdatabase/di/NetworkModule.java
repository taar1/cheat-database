package com.cheatdatabase.di;

import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.rest.RestApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class NetworkModule {

    @Singleton
    @Provides
    static Gson provideGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.setDateFormat("yyyy-MM-dd");
        return builder.create();
    }

    @Provides
    static RestApi provideRestApi(Retrofit retrofit) {
        return retrofit.create(RestApi.class);
    }

    @Singleton
    @Provides
    static Retrofit provideRetrofit(Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(Konstanten.BASE_URL_REST)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }


}
