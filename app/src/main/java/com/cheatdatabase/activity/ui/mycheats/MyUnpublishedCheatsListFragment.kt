package com.cheatdatabase.activity.ui.mycheats

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cheatdatabase.R
import com.cheatdatabase.data.model.UnpublishedCheat
import com.cheatdatabase.helpers.Tools
import com.cheatdatabase.listeners.MyUnpublishedCheatsListItemSelectedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.toolbar.view.*
import kotlinx.android.synthetic.main.unpublished_cheats_fragment.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * This is the MODEL of MVVM.
 */
class MyUnpublishedCheatsListFragment : Fragment(), MyUnpublishedCheatsListItemSelectedListener,
    MyUnpublishedCheatsListener {
    private var myUnpublishedCheatsViewModel: MyUnpublishedCheatsViewModel? = null
    private var myUnpublishedCheatsListViewAdapter: MyUnpublishedCheatsListViewAdapter? = null

    //    lateinit var emptyListLayout: RelativeLayout
//    lateinit var emptyLabel: TextView
//    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var outerLayout: CoordinatorLayout
    lateinit var recyclerView: RecyclerView
    lateinit var progressBar: ProgressBar
    lateinit var mToolbar: androidx.appcompat.widget.Toolbar

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

        // TODO
//        swipeRefreshLayout.setOnRefreshListener {
//            swipeRefreshLayout.visibility = View.VISIBLE
//            myUnpublishedCheatsListViewAdapter!!.notifyDataSetChanged()
//            swipeRefreshLayout.visibility = View.GONE
//        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        myUnpublishedCheatsViewModel =
            ViewModelProvider(this).get(MyUnpublishedCheatsViewModel::class.java)
//        myUnpublishedCheatsViewModel!!.init()
        myUnpublishedCheatsViewModel!!.fetchListener = this

        setupRecyclerView()

        myUnpublishedCheatsViewModel?.getMyUnpublishedCheatsByCoroutines()

        // Getting unpublished cheats from server
//        myUnpublishedCheatsViewModel!!.myUnpublishedCheats!!.observe(
//            requireActivity(),
//            Observer { unpublishedCheats ->
//                myUnpublishedCheatsListViewAdapter!!.setUnpublishedCheats(unpublishedCheats)
//                myUnpublishedCheatsListViewAdapter!!.notifyDataSetChanged()
//
//                progressBar.visibility = View.GONE
//            })
    }

    private fun setupRecyclerView() {
        if (myUnpublishedCheatsListViewAdapter == null) {
            myUnpublishedCheatsListViewAdapter = MyUnpublishedCheatsListViewAdapter(this, activity)

            recyclerView.layoutManager = LinearLayoutManager(activity)
            recyclerView.setHasFixedSize(true)
            recyclerView.itemAnimator = DefaultItemAnimator()
            recyclerView.isNestedScrollingEnabled = true
            recyclerView.adapter = myUnpublishedCheatsListViewAdapter
        } else {
            myUnpublishedCheatsListViewAdapter!!.notifyDataSetChanged()
        }
    }

    override fun fetchUnpublishedCheatsSuccess(unpublishedCheats: List<UnpublishedCheat>) {
        myUnpublishedCheatsListViewAdapter!!.setUnpublishedCheats(unpublishedCheats)
        myUnpublishedCheatsListViewAdapter!!.notifyDataSetChanged()

        progressBar.visibility = View.GONE

        Tools.showSnackbar(outerLayout, "SSUCCESSSSSSSSS")
    }

    override fun fetchUnpublishedCheatsFail(message: String) {
        Tools.showSnackbar(outerLayout, message)
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
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                // do nothing
            }
            .setPositiveButton(getString(R.string.delete)) { dialog, which ->
                Log.d(TAG, "XXXXX onDeleteButtonClicked: DELETE")
                deleteUnpublishedCheatAndDisplaySnackbar(cheat)
            }
            .show()
    }

    private fun deleteUnpublishedCheatAndDisplaySnackbar(cheat: UnpublishedCheat) {
        myUnpublishedCheatsViewModel!!.deleteUnpublishedCheat(cheat)
            .enqueue(object : Callback<JsonObject?> {
                override fun onResponse(
                    unpublishedCheats: Call<JsonObject?>,
                    response: Response<JsonObject?>
                ) {
                    Log.d(TAG, "XXXXX onResponse: ")
                    if (response.isSuccessful) {
                        val responseJsonObject = response.body()

                        val translatedReturnValue: String =
                            when (responseJsonObject!!["returnValue"].asString) {
                                "delete_ok" -> getString(R.string.cheat_deleted)
                                "delete_nok" -> getString(R.string.cheat_delete_nok)
                                "wrong_pw" -> getString(R.string.error_incorrect_password)
                                "member_banned" -> getString(R.string.member_banned)
                                "member_not_exist" -> getString(R.string.err_no_member_data)
                                "no_database_access" -> getString(R.string.no_database_access)
                                else -> getString(R.string.err_occurred)
                            }

                        Tools.showSnackbar(outerLayout, translatedReturnValue)

                        // TODO refresh list without the deleted cheat...
                        // TODO refresh list without the deleted cheat...
                        // TODO refresh list without the deleted cheat...
                        // TODO refresh list without the deleted cheat...
                        myUnpublishedCheatsListViewAdapter!!.notifyDataSetChanged()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, e: Throwable) {
                    Log.e(TAG, "XXXXX getMyUnpublishedCheats onFailure: " + e.localizedMessage, e)
                }
            })


    }

    companion object {
        private const val TAG = "MyUnpublishedCheatsFt"
        fun newInstance(): MyUnpublishedCheatsListFragment {
            return MyUnpublishedCheatsListFragment()
        }
    }


}