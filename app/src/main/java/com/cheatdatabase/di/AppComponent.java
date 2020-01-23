package com.cheatdatabase.di;

import android.app.Application;

import com.cheatdatabase.CheatDatabaseApplication;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;

@Singleton
@Component(modules = {AndroidInjectionModule.class, MyApplicationModule.class})
public interface AppComponent extends AndroidInjector<CheatDatabaseApplication> {

    @Component.Factory
    interface Factory {

    }

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }


}
