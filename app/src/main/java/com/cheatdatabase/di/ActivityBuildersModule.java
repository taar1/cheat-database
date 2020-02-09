package com.cheatdatabase.di;

import com.cheatdatabase.activity.CheatsByGameListActivity;
import com.cheatdatabase.activity.MainActivity;
import com.cheatdatabase.fragments.TopMembersFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuildersModule {

    @ContributesAndroidInjector(
            modules = {
                    MainActivityModule.class,
                    NetworkModule.class
            }
    )
    abstract MainActivity contributeMainActivity();


    @ContributesAndroidInjector(
            modules = {
                    NetworkModule.class
            }
    )
    abstract CheatsByGameListActivity contributeCheatsByGameListActivity();

    @ContributesAndroidInjector(
            modules = {
                    NetworkModule.class
            }
    )
    abstract TopMembersFragment contributeTopMembersFragment();
}
