package com.cheatdatabase.handset.cheatview;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cheatdatabase.businessobjects.Game;

public class CheatViewFragmentAdapter extends FragmentPagerAdapter {

    private String[] cheatTitles;
    private Game game;

    public CheatViewFragmentAdapter(FragmentManager fm, Game game, String[] cheatTitleNames) {
        super(fm);
        this.game = game;
        cheatTitles = cheatTitleNames;
    }

    @Override
    public Fragment getItem(int position) {
        if (cheatTitles == null) {
            cheatTitles = new String[game.getCheatsCount()];
        }
        return CheatViewFragment.newInstance(cheatTitles[position % cheatTitles.length], game, position);
    }

    @Override
    public int getCount() {
        return cheatTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return cheatTitles[position % cheatTitles.length];
    }

}
