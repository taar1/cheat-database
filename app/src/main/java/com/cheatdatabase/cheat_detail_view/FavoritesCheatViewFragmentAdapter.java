package com.cheatdatabase.cheat_detail_view;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.cheatdatabase.businessobjects.Game;

public class FavoritesCheatViewFragmentAdapter extends FragmentPagerAdapter {

    protected static String[] CONTENT = new String[]{"T1", "T2", "T3"};
    private int mCount = CONTENT.length;

    // Icons not used....
    protected static final int[] ICONS = new int[]{android.R.drawable.btn_plus, android.R.drawable.btn_plus, android.R.drawable.btn_plus, android.R.drawable.btn_plus};

    private Game game;

    public FavoritesCheatViewFragmentAdapter(FragmentManager fm, Game game, String[] cheatTitleNames) {
        super(fm);
        this.game = game;
        CONTENT = cheatTitleNames;
        mCount = CONTENT.length;
    }

    @Override
    public Fragment getItem(int position) {
        if (CONTENT == null) {
            CONTENT = new String[game.getCheatsCount()];
        }
        return FavoritesCheatViewFragment.newInstance(CONTENT[position % CONTENT.length], game, position);
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return FavoritesCheatViewFragmentAdapter.CONTENT[position % CONTENT.length];
    }

    public int getIconResId(int index) {
        return ICONS[index % ICONS.length];
    }

    public void setCount(int count) {
        if (count > 0 && count <= 10) {
            mCount = count;
            notifyDataSetChanged();
        }
    }

}
