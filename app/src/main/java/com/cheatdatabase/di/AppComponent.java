package com.cheatdatabase.di;

import android.app.Application;

import com.cheatdatabase.CheatDatabaseApplication;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

@Singleton
@Component(
        modules = {
                AndroidSupportInjectionModule.class,
                AppModule.class,
                MainActivityModule.class
        }
)
public interface AppComponent extends AndroidInjector<CheatDatabaseApplication> {

//    @Component.Builder
//    interface Builder {
//
//        @BindsInstance
//        Builder application(Application application);
//
//        AppComponent build();
//    }
}






