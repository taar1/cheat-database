package com.cheatdatabase.cheatdetailview

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cheatdatabase.cheatdetailview.CheatViewFragment.Companion.newInstance
import com.cheatdatabase.data.model.Cheat
import com.cheatdatabase.data.model.Game

class CheatViewFragmentAdapter(
    fragmentActivity: FragmentActivity,
    private val game: Game,
    private val cheatArray: ArrayList<Cheat>
) : FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        return newInstance(game, position)
    }

    override fun getItemCount(): Int {
        return cheatArray.size
    }

//    override fun getPageTitle(position: Int): CharSequence? {
//        return cheatArray[position].cheatTitle
//    }
}