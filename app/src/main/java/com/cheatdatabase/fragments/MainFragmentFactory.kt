package com.cheatdatabase.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.cheatdatabase.activity.MainActivity
import com.cheatdatabase.activity.SubmitCheatSelectGameActivity
import com.cheatdatabase.activity.ui.mycheats.UnpublishedCheatsRepositoryKotlin
import javax.inject.Inject

class MainFragmentFactory
@Inject constructor(
    private val mainActivity: MainActivity,
    val myCheatsCount: UnpublishedCheatsRepositoryKotlin.MyCheatsCount?,
    val submitCheatSelectGameActivity: SubmitCheatSelectGameActivity
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            SystemListFragment::class.java.name -> {
                // TODO FIXME
                SystemListFragment()
            }

            ContactFormFragment::class.java.name -> {
                ContactFormFragment()
            }

            MyCheatsFragment::class.java.name -> {
                MyCheatsFragment(mainActivity, myCheatsCount)
            }

            SubmitCheatFragment::class.java.name -> {
                SubmitCheatFragment(submitCheatSelectGameActivity)
            }

            TopMembersFragment::class.java.name -> {
                TopMembersFragment(mainActivity)
            }

            else -> super.instantiate(classLoader, className)
        }
    }

}