package com.cheatdatabase.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.cheatdatabase.R
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

    var member: Member? = null

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
        member =
            Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member::class.java)

        myScoreLayout.setOnClickListener {
            Tools.showSnackbar(outerLayout, getString(R.string.earn_points_submitting_cheats))
        }

        unpublishedCheatsCard.setOnClickListener {
            onCardClicked(Intent(mainActivity, MyUnpublishedCheatsListActivity::class.java))
        }

        publishedCheatsCard.setOnClickListener {
            onCardClicked(Intent(mainActivity, MainActivity::class.java))
        }

        return view
    }

    private fun onCardClicked(intent: Intent) {
        if (member == null || member?.mid == 0) {
            Tools.showSnackbar(outerLayout, getString(R.string.error_login_required))
        } else {
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        member =
            Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member::class.java)
    }

}