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
import kotlinx.android.synthetic.main.unpublished_cheats_fragment.view.*

/**
 * This is the MODEL of MVVM.
 */
class MyUnpublishedCheatsFragment : Fragment(), MyUnpublishedCheatsListItemSelectedListener {
    private var myUnpublishedCheatsViewModel: MyUnpublishedCheatsViewModel? = null
    private var myUnpublishedCheatsListViewAdapter: MyUnpublishedCheatsListViewAdapter? = null

    lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.unpublished_cheats_fragment, container, false)

        recyclerView = view.recycler_view

//        val binding = UnpublishedCheatsFragmentBinding.inflate(layoutInflater)
//        recyclerView = binding.recyclerView

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        myUnpublishedCheatsViewModel =
            ViewModelProvider(this).get(MyUnpublishedCheatsViewModel::class.java)
        myUnpublishedCheatsViewModel!!.init()

        setupRecyclerView()

        myUnpublishedCheatsViewModel!!.myUnpublishedCheatsRepository.observe(
            activity!!,
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

    override fun onCheatClicked(cheat: UnpublishedCheat) {
        Log.d(TAG, "onCheatClicked: ")
    }

    override fun onRejectReasonButtonClicked(cheat: UnpublishedCheat) {
        Log.d(TAG, "onRejectReasonButtonClicked: ")
    }

    override fun onDeleteButtonClicked(cheat: UnpublishedCheat) {
        Log.d(TAG, "onDeleteButtonClicked: ")
    }

    companion object {
        private const val TAG = "MyUnpublishedCheatsFt"
        fun newInstance(): MyUnpublishedCheatsFragment {
            return MyUnpublishedCheatsFragment()
        }
    }
}