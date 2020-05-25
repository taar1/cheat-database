package com.cheatdatabase.activity.ui.mycheats

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cheatdatabase.R
import com.cheatdatabase.data.model.UnpublishedCheat
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

    lateinit var recyclerView: RecyclerView
    lateinit var mToolbar: androidx.appcompat.widget.Toolbar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.unpublished_cheats_fragment, container, false)

        recyclerView = view.recycler_view
        mToolbar = view.toolbar

        mToolbar.title = getString(R.string.unpublished_cheats)

        // TODO FIXME hier noch "home as up" in toolbar irgendwie einbauen....
        // TODO FIXME hier noch "home as up" in toolbar irgendwie einbauen....
        // TODO FIXME hier noch "home as up" in toolbar irgendwie einbauen....

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        myUnpublishedCheatsViewModel =
            ViewModelProvider(this).get(MyUnpublishedCheatsViewModel::class.java)
        myUnpublishedCheatsViewModel!!.init()

        setupRecyclerView()

        myUnpublishedCheatsViewModel!!.myUnpublishedCheatsRepository.observe(
            requireActivity(),
            Observer { unpublishedCheats ->
                Toast.makeText(context, "XXXXX Unpublished Cheats onChanged", Toast.LENGTH_LONG)
                    .show()
                myUnpublishedCheatsListViewAdapter!!.setUnpublishedCheats(unpublishedCheats)
                myUnpublishedCheatsListViewAdapter!!.notifyDataSetChanged()
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
                // TODO: delete unpublished cheat and refresh list...
                // TODO: delete unpublished cheat and refresh list...
                Log.d(TAG, "XXXXX onDeleteButtonClicked: DELETE")
            }
            .show()

    }

    companion object {
        private const val TAG = "MyUnpublishedCheatsFt"
        fun newInstance(): MyUnpublishedCheatsListFragment {
            return MyUnpublishedCheatsListFragment()
        }
    }
}