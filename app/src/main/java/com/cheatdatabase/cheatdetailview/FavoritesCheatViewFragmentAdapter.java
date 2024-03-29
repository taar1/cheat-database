package com.cheatdatabase.cheatdetailview;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.cheatdatabase.data.model.Game;

public class FavoritesCheatViewFragmentAdapter extends FragmentPagerAdapter {
    private final Game game;

    public FavoritesCheatViewFragmentAdapter(FragmentManager fragmentManager, Game game) {
        super(fragmentManager);
        this.game = game;
    }

    @Override
    public Fragment getItem(int position) {
        return FavoritesCheatViewFragment.newInstance(game, position);
    }

    @Override
    public int getCount() {
        return game.getCheatList().size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return game.getCheatList().get(position % game.getCheatList().size()).getCheatTitle();
    }

}
