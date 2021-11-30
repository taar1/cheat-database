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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MyCheatsFragment : Fragment() {

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

    private var _binding: FragmentMyCheatsOverviewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyCheatsViewModel by viewModels()

    companion object {
        const val TAG = "MyCheatsFragment"
        fun newInstance() = MyCheatsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyCheatsOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myScoreLayout = binding.myScoreLayout
        outerLayout = binding.outerLayout
        unpublishedCheatsCard = binding.cardUnpublishedCheats
        publishedCheatsCard = binding.cardPublishedCheats
        publishedCheatsCount = binding.publishedCheatsCount
        unpublishedCheatsCount = binding.unpublishedCheatsCount
        unpublishedCheatsSubtitle = binding.unpublishedCheatsSubtitle

        viewModel.myCheats.observe(viewLifecycleOwner) { myCheats ->
            myCheatsCount = myCheats
            updateText()
        }

        initListeners()
        updateText()

        myScoreLayout = binding.myScoreLayout
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}