package com.cheatdatabase.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import com.cheatdatabase.R
import com.cheatdatabase.activity.MainActivity
import com.cheatdatabase.activity.ui.mycheats.MyUnpublishedCheatsListActivity
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.helpers.Konstanten
import com.cheatdatabase.helpers.Tools
import com.google.gson.Gson

class MyCheatsFragment(val mainActivity: MainActivity, var settings: SharedPreferences) : Fragment() {
    val TAG = "MyCheatsFragment"

    @JvmField
    @BindView(R.id.outer_layout)
    var outerLayout: ConstraintLayout? = null

    @JvmField
    @BindView(R.id.card_unpublished_cheats)
    var unpublishedCheatsCard: CardView? = null

    @JvmField
    @BindView(R.id.card_published_cheats)
    var publishedCheatsCard: CardView? = null

    var member: Member? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_my_cheats_overview, container, false)
        ButterKnife.bind(this, view)

        settings = this.mainActivity.getSharedPreferences(Konstanten.PREFERENCES_FILE, 0)
        member = Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member::class.java)

        unpublishedCheatsCard?.setOnClickListener {
//            onCardClicked(Intent(mainActivity, MyUnpublishedCheatsListActivity::class.java))
            onCardClicked(Intent(mainActivity, MyUnpublishedCheatsListActivity::class.java))
        }

        publishedCheatsCard?.setOnClickListener {
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
        member = Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member::class.java)
    }

}