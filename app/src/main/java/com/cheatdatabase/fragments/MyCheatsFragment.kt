package com.cheatdatabase.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.cheatdatabase.R
import com.cheatdatabase.activity.CheatsByMemberListActivity
import com.cheatdatabase.activity.MainActivity
import com.cheatdatabase.activity.ui.mycheats.MyUnpublishedCheatsListActivity
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.helpers.Konstanten
import com.cheatdatabase.helpers.Tools
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_my_cheats_overview.view.*

class MyCheatsFragment(val mainActivity: MainActivity, var settings: SharedPreferences) :
    Fragment() {
    val TAG = "MyCheatsFragment"

    lateinit var myScoreLayout: LinearLayout
    lateinit var outerLayout: ConstraintLayout
    lateinit var unpublishedCheatsCard: CardView
    lateinit var publishedCheatsCard: CardView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_cheats_overview, container, false)

        myScoreLayout = view.my_score_layout
        outerLayout = view.outer_layout
        unpublishedCheatsCard = view.card_unpublished_cheats
        publishedCheatsCard = view.card_published_cheats

        settings = this.mainActivity.getSharedPreferences(Konstanten.PREFERENCES_FILE, 0)

        myScoreLayout.setOnClickListener {
            Tools.showSnackbar(outerLayout, getString(R.string.earn_points_submitting_cheats))
        }

        unpublishedCheatsCard.setOnClickListener {
            startActivityIfLoggedIn(getMember(), Intent(mainActivity, MyUnpublishedCheatsListActivity::class.java))
        }

        publishedCheatsCard.setOnClickListener {
            val member = getMember()

            val myCheatsIntent = Intent(mainActivity, CheatsByMemberListActivity::class.java)
            myCheatsIntent.putExtra("member", member)
            startActivityIfLoggedIn(member, myCheatsIntent)
        }

        return view
    }

    private fun startActivityIfLoggedIn(member: Member?, myCheatsIntent: Intent) {
        if (member == null || member.mid == 0) {
            Tools.showSnackbar(outerLayout, getString(R.string.error_login_required))
        } else {
            startActivity(myCheatsIntent)
        }
    }

    private fun getMember(): Member? {
        return Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member::class.java)
    }

}