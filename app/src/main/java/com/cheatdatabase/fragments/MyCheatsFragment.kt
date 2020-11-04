package com.cheatdatabase.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.cheatdatabase.R
import com.cheatdatabase.activity.CheatsByMemberListActivity
import com.cheatdatabase.activity.ui.mycheats.MyUnpublishedCheatsListActivity
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.data.model.MyCheatsCount
import com.cheatdatabase.databinding.FragmentMyCheatsOverviewBinding
import com.cheatdatabase.helpers.Tools
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_my_cheats_overview.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MyCheatsFragment : Fragment(R.layout.fragment_my_cheats_overview) {
    val TAG = "MyCheatsFragment"

    @Inject
    lateinit var tools: Tools

    private var myCheatsCount: MyCheatsCount? = null

    lateinit var myScoreLayout: LinearLayout
    lateinit var outerLayout: ConstraintLayout
    lateinit var unpublishedCheatsCard: CardView
    lateinit var publishedCheatsCard: CardView
    lateinit var publishedCheatsCount: TextView
    lateinit var unpublishedCheatsCount: TextView
    lateinit var unpublishedCheatsSubtitle: TextView

    private lateinit var viewBinding: FragmentMyCheatsOverviewBinding

    private val viewModel: MyCheatsViewModel by viewModels()

    companion object {
        fun newInstance() = MyCheatsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentMyCheatsOverviewBinding.inflate(inflater)
        val view = viewBinding.root

        myScoreLayout = view.my_score_layout
        outerLayout = view.outer_layout
        unpublishedCheatsCard = view.card_unpublished_cheats
        publishedCheatsCard = view.card_published_cheats
        publishedCheatsCount = view.published_cheats_count
        unpublishedCheatsCount = view.unpublished_cheats_count
        unpublishedCheatsSubtitle = view.unpublished_cheats_subtitle

        viewModel.myCheats.observe(viewLifecycleOwner, { myCheats ->
            myCheatsCount = myCheats
            updateText()
        })

        initListeners()
        updateText()
        return viewBinding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.getMyCheatsCount(tools.member)
    }

    private fun initListeners() {
        myScoreLayout.setOnClickListener {
            tools.showSnackbar(outerLayout, getString(R.string.earn_points_submitting_cheats))
        }

        unpublishedCheatsCard.setOnClickListener {
            startActivityIfLoggedIn(
                tools.member,
                Intent(activity, MyUnpublishedCheatsListActivity::class.java)
            )
        }

        publishedCheatsCard.setOnClickListener {
            val member = tools.member

            val myCheatsIntent = Intent(activity, CheatsByMemberListActivity::class.java)
            myCheatsIntent.putExtra("member", member)
            startActivityIfLoggedIn(member, myCheatsIntent)
        }
    }

    private fun startActivityIfLoggedIn(member: Member?, myCheatsIntent: Intent) {
        if (member == null || member.mid == 0) {
            tools.showSnackbar(outerLayout, getString(R.string.error_login_required))
        } else {
            startActivity(myCheatsIntent)
        }
    }

    private fun updateText() {
        if (myCheatsCount != null) {
            val unpublishedCheatsSum: Int =
                myCheatsCount!!.uncheckedCheats + myCheatsCount!!.rejectedCheats

            if (unpublishedCheatsSum != 0) {
                unpublishedCheatsCount.text = "(".plus(unpublishedCheatsSum).plus(")")
            } else {
                unpublishedCheatsCount.text = ""
            }

            publishedCheatsCount.text = "(".plus(myCheatsCount?.publishedCheats).plus(")")

            unpublishedCheatsSubtitle.text =
                getString(
                    R.string.cheats_waiting_for_approval,
                    "(".plus(myCheatsCount?.uncheckedCheats).plus(") "),
                    "(".plus(myCheatsCount?.rejectedCheats).plus(")")
                )
        } else {
            showLoggedOutText()
        }
    }

    private fun showLoggedOutText() {
        publishedCheatsCount.text = ""
        unpublishedCheatsCount.text = ""
        unpublishedCheatsSubtitle.text =
            getString(R.string.cheats_waiting_for_approval, "", "")
    }

}