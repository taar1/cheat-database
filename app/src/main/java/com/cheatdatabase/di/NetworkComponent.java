package com.cheatdatabase.di;

import com.cheatdatabase.activity.CheatsByGameListActivity;
import com.cheatdatabase.activity.MainActivity;
import com.cheatdatabase.fragments.SystemListFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class, NetworkModule.class})
public interface NetworkComponent {

    void inject(MainActivity activity);

    void inject(CheatsByGameListActivity activity);

    void inject(SystemListFragment activity);


}
