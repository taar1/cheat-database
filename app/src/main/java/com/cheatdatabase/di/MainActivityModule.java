package com.cheatdatabase.di;

import android.content.Context;

import dagger.Module;

@Module
public class MainActivityModule {
    private final Context context;

    public MainActivityModule(Context context) {
        this.context = context;
    }

}
