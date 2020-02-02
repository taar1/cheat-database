package com.cheatdatabase.di;

import android.content.Context;

import com.cheatdatabase.activity.MainActivity;

import javax.inject.Singleton;

import dagger.Component;
import dagger.Provides;

@Singleton
@Component(modules = {MainActivityModule.class, NetworkModule.class})
public interface MainActivityComponent {
//    @Provides
//    Context context();
//
//    @Provides
//    void inject(MainActivity mainActivity);
}