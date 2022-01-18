package com.cheatdatabase.cheatdetailview

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.cheatdatabase.cheatdetailview.CheatViewFragment.Companion.newInstance
import com.cheatdatabase.data.model.Cheat
import com.cheatdatabase.data.model.Game

class CheatViewFragmentAdapter(
    fragmentManager: FragmentManager,
    private val game: Game,
    private val cheatArray: ArrayList<Cheat>
) : FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment {
        return newInstance(game, position)
    }

    override fun getCount(): Int {
        return cheatArray.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return cheatArray[position].cheatTitle
    }
}