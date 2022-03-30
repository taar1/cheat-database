package com.cheatdatabase.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.applovin.adview.AppLovinAdView
import com.cheatdatabase.R
import com.cheatdatabase.activity.ui.mycheats.edit.EditCheatActivity
import com.cheatdatabase.adapters.MemberCheatRecycleListViewAdapter
import com.cheatdatabase.cheatdetailview.MemberCheatViewPageIndicator
import com.cheatdatabase.data.model.Cheat
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.databinding.ActivityMemberCheatListBinding
import com.cheatdatabase.helpers.Konstanten
import com.cheatdatabase.helpers.Tools
import com.cheatdatabase.listeners.OnMyCheatListItemSelectedListener
import com.cheatdatabase.rest.RestApi
import com.cheatdatabase.widgets.DividerDecoration
import com.google.gson.Gson
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

/**
 * Shows all cheats of one particular member.
 */
@AndroidEntryPoint
class CheatsByMemberListActivity : AppCompatActivity(), OnMyCheatListItemSelectedListener {

    @Inject
    lateinit var tools: Tools

    @Inject
    lateinit var restApi: RestApi

    lateinit var progressBar: ProgressBar
    lateinit var recyclerView: FastScrollRecyclerView
    lateinit var toolbar: Toolbar
    lateinit var emptyView: TextView
    lateinit var outerLayout: ConstraintLayout
    lateinit var appLovinAdView: AppLovinAdView

    private var memberCheatRecycleListViewAdapter: MemberCheatRecycleListViewAdapter? = null
    private var authorMember: Member? = null
    private var cheatList: List<Cheat?>? = null

    private lateinit var binding: ActivityMemberCheatListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMemberCheatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // The Member of whom to display the cheats.
        authorMember = intent.getParcelableExtra("member")

        bindViews()
        init()

        memberCheatRecycleListViewAdapter =
            MemberCheatRecycleListViewAdapter(this, tools.member, this)

        with(recyclerView) {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            addItemDecoration(DividerDecoration(context))
            itemAnimator!!.removeDuration = 50
            setHasFixedSize(true)
        }

        getCheats()
    }

    private fun bindViews() {
        progressBar = binding.progressBar
        recyclerView = binding.myRecyclerView
        toolbar = binding.includeToolbar.toolbar
        emptyView = binding.itemListEmptyView
        outerLayout = binding.outerLayout
        appLovinAdView = binding.applovinInclude.adContainer
    }

    private fun init() {
        toolbar = tools.initToolbarBase(this, toolbar)
        emptyView.text = getString(R.string.no_member_cheats, authorMember!!.username)
        emptyView.visibility = View.GONE
        appLovinAdView.loadNextAd()

        supportActionBar?.title = getString(R.string.members_cheats_title, authorMember!!.username)
    }

    private fun getCheats() {
        progressBar.visibility = View.VISIBLE

        val call = restApi.getCheatsByMemberId(authorMember!!.mid)
        call.enqueue(object : Callback<List<Cheat?>?> {
            override fun onResponse(
                games: Call<List<Cheat?>?>,
                response: Response<List<Cheat?>?>
            ) {
                if (response.isSuccessful) {
                    cheatList = response.body()
                    tools.putString(
                        Konstanten.PREFERENCES_TEMP_CHEAT_ARRAY_OBJECT_VIEW,
                        Gson().toJson(cheatList)
                    )
                    updateUI()
                } else {
                    emptyView.visibility = View.GONE
                }
                progressBar.visibility = View.GONE
            }

            override fun onFailure(call: Call<List<Cheat?>?>, t: Throwable) {
                Log.e(TAG, "getting member cheats has failed: " + t.localizedMessage)
                emptyView.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                tools.showSnackbar(outerLayout, getString(R.string.error_loading_cheats))
            }
        })
    }

    private fun updateUI() {
        if (!cheatList.isNullOrEmpty()) {
            memberCheatRecycleListViewAdapter!!.setCheatList(cheatList!! as List<Cheat>)
            recyclerView.adapter = memberCheatRecycleListViewAdapter
            memberCheatRecycleListViewAdapter!!.notifyDataSetChanged()
            emptyView.visibility = View.GONE
        } else {
            emptyView.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        appLovinAdView.destroy()
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCheatListItemSelected(cheat: Cheat, position: Int) {
        tools.putInt(Konstanten.PREFERENCES_PAGE_SELECTED, position)

        // Using local Preferences to pass data (PREFERENCES_TEMP_CHEAT_ARRAY_OBJECT_VIEW) for game objects (instead of intent) otherwise runs into TransactionTooLargeException when passing the array to the next activity.
        val intent =
            Intent(this@CheatsByMemberListActivity, MemberCheatViewPageIndicator::class.java)
        intent.putExtra("selectedPage", position)
        intent.putExtra("layoutResourceId", R.layout.activity_cheatview_pager)
        startActivity(intent)
    }

    override fun onCheatListItemEditSelected(cheat: Cheat, position: Int) {
        // TODO create a edit cheat MVVM construct here....
        val intent = Intent(this@CheatsByMemberListActivity, EditCheatActivity::class.java)
        intent.putExtra("cheat", cheat)
        startActivity(intent)
    }

    companion object {
        private const val TAG = "CheatsByMemberListActiv"
    }
}