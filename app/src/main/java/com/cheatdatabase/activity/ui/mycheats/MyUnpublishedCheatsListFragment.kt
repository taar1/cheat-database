package com.cheatdatabase.activity.ui.mycheats

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.cheatdatabase.R
import com.cheatdatabase.data.model.UnpublishedCheat
import com.cheatdatabase.helpers.Tools
import com.cheatdatabase.listeners.MyUnpublishedCheatsListItemSelectedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.toolbar.view.*
import kotlinx.android.synthetic.main.unpublished_cheats_fragment.view.*

/**
 * This is the MODEL of MVVM.
 */
class MyUnpublishedCheatsListFragment : Fragment(), MyUnpublishedCheatsListItemSelectedListener {
    private var myUnpublishedCheatsViewModel: MyUnpublishedCheatsViewModel? = null
    private var myUnpublishedCheatsListViewAdapter: MyUnpublishedCheatsListViewAdapter? = null

    //    lateinit var emptyListLayout: RelativeLayout
//    lateinit var emptyLabel: TextView
    lateinit var outerLayout: CoordinatorLayout
    lateinit var recyclerView: RecyclerView
    lateinit var progressBar: ProgressBar
    lateinit var mToolbar: androidx.appcompat.widget.Toolbar
    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.unpublished_cheats_fragment, container, false)

//        emptyListLayout = view.empty
//        emptyListLayout.visibility = View.GONE
//        emptyLabel = view.empty_label
        outerLayout = view.outer_layout
        recyclerView = view.recycler_view
        mToolbar = view.toolbar
        progressBar = view.progress_bar
//        swipeRefreshLayout = view.swipe_refresh_layout

        mToolbar.title = getString(R.string.unpublished_cheats)

        // TODO FIXME hier noch "home as up" in toolbar irgendwie einbauen....
        // TODO FIXME hier noch "home as up" in toolbar irgendwie einbauen....
        // TODO FIXME hier noch "home as up" in toolbar irgendwie einbauen....

        setHasOptionsMenu(true)

        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.visibility = View.VISIBLE
            myUnpublishedCheatsListViewAdapter!!.notifyDataSetChanged()
            swipeRefreshLayout.visibility = View.GONE
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        myUnpublishedCheatsViewModel =
            ViewModelProvider(this).get(MyUnpublishedCheatsViewModel::class.java)
        myUnpublishedCheatsViewModel!!.init()

        setupRecyclerView()

        // Getting unpublished cheats from server
        myUnpublishedCheatsViewModel!!.myUnpublishedCheatsRepository.observe(
            requireActivity(),
            Observer { unpublishedCheats ->
                myUnpublishedCheatsListViewAdapter!!.setUnpublishedCheats(unpublishedCheats)
                myUnpublishedCheatsListViewAdapter!!.notifyDataSetChanged()

                progressBar.visibility = View.GONE
            })
    }

    private fun setupRecyclerView() {
        if (myUnpublishedCheatsListViewAdapter == null) {
            myUnpublishedCheatsListViewAdapter = MyUnpublishedCheatsListViewAdapter(this, activity)
            val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(activity)
            recyclerView.layoutManager = layoutManager
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = myUnpublishedCheatsListViewAdapter
        } else {
            myUnpublishedCheatsListViewAdapter!!.notifyDataSetChanged()
        }
    }

    override fun onEditCheatButtonClicked(cheat: UnpublishedCheat) {
        Log.d(TAG, "XXXXX onEditCheatButtonClicked: ")
    }

    override fun onRejectReasonButtonClicked(cheat: UnpublishedCheat) {
        Log.d(TAG, "XXXXX onRejectReasonButtonClicked: ")
    }

    override fun onDeleteButtonClicked(cheat: UnpublishedCheat) {
        Log.d(TAG, "XXXXX onDeleteButtonClicked: ")

        MaterialAlertDialogBuilder(context, R.style.SimpleAlertDialog)
            .setTitle("\"".plus(cheat.title).plus("\""))
            .setMessage(getString(R.string.unpublished_cheat_are_you_sure_delete))
            .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                // do nothing
            }
            .setPositiveButton(getString(R.string.delete)) { dialog, which ->
                // TODO: delete unpublished cheat and refresh list...
                Log.d(TAG, "XXXXX onDeleteButtonClicked: DELETE")

                displaySnackbarWithTranslatedMessage(
                    myUnpublishedCheatsViewModel!!.deleteUnpublishedCheat(
                        cheat
                    )
                )
                myUnpublishedCheatsListViewAdapter!!.notifyDataSetChanged()
            }
            .show()
    }

    fun displaySnackbarWithTranslatedMessage(stringResourceKey: String) {

        var translatedReturnValue: String

        when (stringResourceKey) {
            "delete_ok" -> translatedReturnValue = getString(R.string.cheat_deleted)
            "delete_nok" -> translatedReturnValue = getString(R.string.cheat_delete_nok)
            "wrong_pw" -> translatedReturnValue = getString(R.string.error_incorrect_password)
            "member_banned" -> translatedReturnValue = getString(R.string.member_banned)
            "member_not_exist" -> translatedReturnValue = getString(R.string.err_no_member_data)
            "no_database_access" -> translatedReturnValue = getString(R.string.no_database_access)
            else -> translatedReturnValue = getString(R.string.err_occurred)
        }

        Tools.showSnackbar(outerLayout, translatedReturnValue)
    }

    companion object {
        private const val TAG = "MyUnpublishedCheatsFt"
        fun newInstance(): MyUnpublishedCheatsListFragment {
            return MyUnpublishedCheatsListFragment()
        }
    }
}