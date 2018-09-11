package com.cheatdatabase.handset.cheatview;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Game;

import java.util.List;

public class CheatViewFragmentAdapter extends FragmentPagerAdapter {

    private List<Cheat> cheatArray;
    private Game game;

    public CheatViewFragmentAdapter(FragmentManager fm, Game game, List<Cheat> cheatArray) {
        super(fm);
        this.game = game;
        this.cheatArray = cheatArray;
    }

    @Override
    public Fragment getItem(int position) {
        return CheatViewFragment.newInstance(cheatArray.get(position).getCheatTitle(), game, position);
    }

    @Override
    public int getCount() {
        return cheatArray.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return cheatArray.get(position).getCheatTitle();
    }

}
