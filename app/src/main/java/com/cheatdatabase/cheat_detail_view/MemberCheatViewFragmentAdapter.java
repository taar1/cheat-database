package com.cheatdatabase.cheat_detail_view;

import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.cheatdatabase.model.Cheat;

import java.util.List;

public class MemberCheatViewFragmentAdapter extends FragmentPagerAdapter {
    private final LinearLayout outerLayout;
    private List<Cheat> cheats;

    public MemberCheatViewFragmentAdapter(FragmentManager fragmentManager, List<Cheat> cheats, LinearLayout outerLayout) {
        super(fragmentManager);
        this.cheats = cheats;
        this.outerLayout = outerLayout;
    }

    @Override
    public Fragment getItem(int position) {
        return MemberCheatViewFragment.newInstance(cheats, position, outerLayout);
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
