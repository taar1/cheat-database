package com.cheatdatabase.cheat_detail_view;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.cheatdatabase.data.model.Cheat;
import com.cheatdatabase.data.model.Game;

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
        return CheatViewFragment.newInstance(game, position);
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
