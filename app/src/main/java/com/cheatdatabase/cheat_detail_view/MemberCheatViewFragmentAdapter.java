package com.cheatdatabase.cheat_detail_view;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.cheatdatabase.model.Cheat;

import java.util.List;

public class MemberCheatViewFragmentAdapter extends FragmentPagerAdapter {
    private List<Cheat> cheats;

    public MemberCheatViewFragmentAdapter(FragmentManager fm, List<Cheat> cheats) {
        super(fm);
        this.cheats = cheats;
    }

    @Override
    public Fragment getItem(int position) {
        return MemberCheatViewFragment.newInstance(cheats, position);
    }

    @Override
    public int getCount() {
        return cheats.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return cheats.get(position % cheats.size()).getCheatTitle();
    }
}
