package com.cheatdatabase.di;

import com.cheatdatabase.activity.MainActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MyApplicationModule {

    @ContributesAndroidInjector
    abstract MainActivity contributeActivityInjector();
}
