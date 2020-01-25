package com.cheatdatabase.cheat_detail_view;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.cheatdatabase.model.Cheat;
import com.cheatdatabase.model.Game;

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
        return com.cheatdatabase.cheat_detail_view.CheatViewFragment.newInstance(cheatArray.get(position).getCheatTitle(), game, position);
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