package com.cheatdatabase.di;

import com.cheatdatabase.activity.CheatForumActivity;
import com.cheatdatabase.activity.CheatsByGameListActivity;
import com.cheatdatabase.activity.GamesBySystemListActivity;
import com.cheatdatabase.activity.MainActivity;
import com.cheatdatabase.dialogs.CheatMetaDialog;
import com.cheatdatabase.search.SearchResultsActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class, NetworkModule.class})
public interface NetworkComponent {

    void inject(MainActivity activity);

    void inject(GamesBySystemListActivity activity);

    void inject(CheatsByGameListActivity activity);

    void inject(SearchResultsActivity activity);

    void inject(CheatForumActivity activity);

    void inject(CheatMetaDialog dialog);

//    void inject(CheatViewFragment fragment);


}
