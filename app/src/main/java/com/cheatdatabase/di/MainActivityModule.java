package com.cheatdatabase.di;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public class MainActivityModule {
    private final Context context;

    public MainActivityModule(Context context) {
        this.context = context;
    }

    @Provides //scope is not necessary for parameters stored within the module
    public Context context() {
        return context;
    }
}
