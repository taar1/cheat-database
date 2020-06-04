package com.cheatdatabase.activity.ui.mycheats

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
class MyUnpublishedCheatsListFragment(val activity: MyUnpublishedCheatsListActivity) : Fragment(),
    MyUnpublishedCheatsListItemSelectedListener,
    MyUnpublishedCheatsListener {

    val TAG = "MyUnpublishedCheatsFt"

    var myUnpublishedCheatsViewModel: MyUnpublishedCheatsViewModel? = null
    var myUnpublishedCheatsListViewAdapter: MyUnpublishedCheatsListViewAdapter? = null

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
        myUnpublishedCheatsViewModel!!.fetchListener = this

        setupRecyclerView()

        myUnpublishedCheatsViewModel?.getMyUnpublishedCheatsByCoroutines()
    }

    override fun fetchUnpublishedCheatsSuccess(unpublishedCheats: List<UnpublishedCheat>) {
        myUnpublishedCheatsListViewAdapter!!.unpublishedCheats = unpublishedCheats
        myUnpublishedCheatsListViewAdapter!!.notifyDataSetChanged()

        progressBar.visibility = View.GONE
    }

    override fun fetchUnpublishedCheatsFail(message: String) {
        Tools.showSnackbar(outerLayout, message)
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

    override fun onEditCheatButtonClicked(cheat: UnpublishedCheat) {
        Log.d(TAG, "XXXXX onEditCheatButtonClicked: ")
        Log.d(TAG, "XXXXX onEditCheatButtonClicked: ")
        Log.d(TAG, "XXXXX onEditCheatButtonClicked: ")
    }

    override fun onRejectReasonButtonClicked(cheat: UnpublishedCheat) {
        val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText(cheat.game.gameName, cheat.rejectReason)

        val dialog = MaterialAlertDialogBuilder(context, R.style.SimpleAlertDialog)
            .setTitle(getString(R.string.why_was_cheat_rejected))
            .setMessage(cheat.rejectReason)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                // just close dialog
            }
            .setNegativeButton(getString(R.string.copy_to_clipboard), null)
            .show()


        val copyToClipboardButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        copyToClipboardButton.setOnClickListener {
            clipboard.primaryClip = clip
            Toast.makeText(
                context,
                getString(R.string.text_copied_to_clipboard),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDeleteButtonClicked(cheat: UnpublishedCheat, position: Int) {
        MaterialAlertDialogBuilder(context, R.style.SimpleAlertDialog)
            .setTitle("\"".plus(cheat.title).plus("\""))
            .setMessage(getString(R.string.unpublished_cheat_are_you_sure_delete))
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                // just close dialog
            }
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                Log.d(TAG, "XXXXX onDeleteButtonClicked: DELETE")
                deleteUnpublishedCheatAndDisplaySnackbar(cheat, position)
            }
            .show()
    }

    private fun deleteUnpublishedCheatAndDisplaySnackbar(cheat: UnpublishedCheat, position: Int) {
        myUnpublishedCheatsViewModel!!.deleteUnpublishedCheat(cheat)
            .enqueue(object : Callback<JsonObject?> {
                override fun onResponse(
                    unpublishedCheats: Call<JsonObject?>,
                    response: Response<JsonObject?>
                ) {
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

                        removeCheatFromList(position)
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, e: Throwable) {
                    Log.e(TAG, "XXXXX getMyUnpublishedCheats onFailure: " + e.localizedMessage, e)
                }
            })
    }

    private fun removeCheatFromList(position: Int) {

        // TODO FIXME ein einzelner cheat aus der liste austragen geht (nicht mehr, es ging als ich myUnpublishedCheatsListViewAdapter noch im java format hatte....)
        // TODO FIXME es entfernt ihn aus der liste aber beim neuladen (automatisch) bleibt der gelöschte cheat in der liste.
        // TODO FIXME erst beim schliessen und neu öffnen der view stimmt die liste dann wieder...

        // TODO FIXME ein einzelner cheat aus der liste austragen geht (nicht mehr, es ging als ich myUnpublishedCheatsListViewAdapter noch im java format hatte....)
        // TODO FIXME es entfernt ihn aus der liste aber beim neuladen (automatisch) bleibt der gelöschte cheat in der liste.
        // TODO FIXME erst beim schliessen und neu öffnen der view stimmt die liste dann wieder...


        myUnpublishedCheatsListViewAdapter?.unpublishedCheats?.drop(position)
        myUnpublishedCheatsListViewAdapter?.notifyItemRemoved(position)
        myUnpublishedCheatsListViewAdapter?.unpublishedCheats?.size?.let {
            myUnpublishedCheatsListViewAdapter?.notifyItemRangeChanged(
                position,
                it
            )
        }
    }

    companion object {
        fun newInstance(activity: MyUnpublishedCheatsListActivity): MyUnpublishedCheatsListFragment {
            return MyUnpublishedCheatsListFragment(activity)
        }
    }

}