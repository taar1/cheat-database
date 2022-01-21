package com.cheatdatabase.cheatdetailview

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cheatdatabase.cheatdetailview.MemberCheatViewFragment.Companion.newInstance
import com.cheatdatabase.data.model.Cheat

class MemberCheatViewFragmentAdapter(
    fragmentActivity: FragmentActivity, private val cheats: List<Cheat>
) : FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        return newInstance(cheats, position)
    }

    override fun getItemCount(): Int {
        return cheats.size
    }

//    override fun getPageTitle(position: Int): CharSequence? {
//        return cheats[position % cheats.size].cheatTitle
//    }
}