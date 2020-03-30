package com.cheatdatabase.cheat_detail_view;

import android.content.Context;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.cheatdatabase.model.Cheat;
import com.cheatdatabase.rest.RestApi;

import java.util.List;

public class MemberCheatViewFragmentAdapter extends FragmentPagerAdapter {
    private final LinearLayout outerLayout;
    private final Context context;
    private List<Cheat> cheats;
    private RestApi restApi;

    public MemberCheatViewFragmentAdapter(FragmentManager fragmentManager, List<Cheat> cheats, RestApi restApi, LinearLayout outerLayout, Context context) {
        super(fragmentManager);
        this.cheats = cheats;
        this.restApi = restApi;
        this.outerLayout = outerLayout;
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        return MemberCheatViewFragment.newInstance(cheats, position, restApi, outerLayout, context);
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
