package com.cheatdatabase.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

    protected Application application;

    public ApplicationModule(Application application) {
        this.application = application;
    }

    @Provides
    Context providesContext() {
        return application.getApplicationContext();
    }

    @Provides
    SharedPreferences providesSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(application.getApplicationContext());
    }

    @Provides
    @Singleton
    Application providesApplication() {
        return application;
    }


//    @Provides
//    Settings providesSettings() {
//        return application.getApplicationPreferences();
//    }
}
