package com.cheatdatabase.members.cheatview;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cheatdatabase.businessobjects.Cheat;

import java.util.List;

public class MemberCheatViewFragmentAdapter extends FragmentPagerAdapter {

    protected static String[] CONTENT = new String[]{"T1", "T2", "T3"};
    private int mCount;

    private List<Cheat> cheats;

    public MemberCheatViewFragmentAdapter(FragmentManager fm, List<Cheat> cheats, String[] cheatTitleNames) {
        super(fm);
        this.cheats = cheats;
        CONTENT = cheatTitleNames;
        mCount = CONTENT.length;
    }

    @Override
    public Fragment getItem(int position) {
        if (CONTENT == null) {
            CONTENT = new String[cheats.size()];
        }
//        return MemberCheatViewFragment.newInstance(CONTENT[position % CONTENT.length], cheats, position);
        return MemberCheatViewFragment.newInstance(cheats, position);
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return MemberCheatViewFragmentAdapter.CONTENT[position % CONTENT.length];
    }


}
