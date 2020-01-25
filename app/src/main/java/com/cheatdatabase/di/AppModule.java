package com.cheatdatabase.di;

import android.app.Application;
import android.content.Context;

import com.cheatdatabase.CheatDatabaseApplication;
import com.cheatdatabase.helpers.Konstanten;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class AppModule {

    @Singleton
    @Provides
    static Retrofit provideRetrofitInstance(){
        return new Retrofit.Builder()
                .baseUrl(Konstanten.BASE_URL_ANDROID)
                //.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private final CheatDatabaseApplication application;

    public AppModule(CheatDatabaseApplication application) {
        this.application = application;
    }

    @Provides
    public CheatDatabaseApplication application() {
        return application;
    }

    @Provides
    @Singleton
    Context provideContext(Application application) {
        return application;
    }




}
