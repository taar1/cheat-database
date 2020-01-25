package com.cheatdatabase.di;

import android.content.Context;

import com.cheatdatabase.activity.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {MainActivityModule.class})
@Singleton
public interface MainActivityComponent {
    Context context();

    void inject(MainActivity mainActivity);
}