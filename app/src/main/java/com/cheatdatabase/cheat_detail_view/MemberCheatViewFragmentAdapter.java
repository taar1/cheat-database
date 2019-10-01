package com.cheatdatabase.cheat_detail_view;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

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
