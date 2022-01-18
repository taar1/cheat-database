package com.cheatdatabase.cheatdetailview

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.cheatdatabase.cheatdetailview.MemberCheatViewFragment.Companion.newInstance
import com.cheatdatabase.data.model.Cheat

class MemberCheatViewFragmentAdapter(
    fragmentManager: FragmentManager,
    private val cheats: List<Cheat>
) : FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment {
        return newInstance(cheats, position)
    }

    override fun getCount(): Int {
        return cheats.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return cheats[position % cheats.size].cheatTitle
    }
}