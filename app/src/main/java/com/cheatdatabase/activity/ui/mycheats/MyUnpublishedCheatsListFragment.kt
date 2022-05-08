package com.cheatdatabase.activity.ui.mycheats

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.cheatdatabase.R
import com.cheatdatabase.activity.SubmitCheatFormActivity
import com.cheatdatabase.activity.SubmitCheatSelectGameActivity
import com.cheatdatabase.data.model.UnpublishedCheat
import com.cheatdatabase.databinding.UnpublishedCheatsFragmentBinding
import com.cheatdatabase.helpers.Tools
import com.cheatdatabase.listeners.MyUnpublishedCheatsListItemSelectedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * This is the MODEL of MVVM.
 */
@AndroidEntryPoint
class MyUnpublishedCheatsListFragment(val activity: MyUnpublishedCheatsListActivity) : Fragment(),
    MyUnpublishedCheatsListItemSelectedListener,
    MyUnpublishedCheatsListener {

    @Inject
    lateinit var tools: Tools

    companion object {
        const val TAG = "MyUnpublishedCheatsFt"

        fun newInstance(activity: MyUnpublishedCheatsListActivity): MyUnpublishedCheatsListFragment {
            return MyUnpublishedCheatsListFragment(activity)
        }
    }

    var cheatPositionInList: Int = 0
    var myUnpublishedCheatsViewModel: MyUnpublishedCheatsViewModel? = null
    var myUnpublishedCheatsListViewAdapter: MyUnpublishedCheatsListViewAdapter? = null

    private var _binding: UnpublishedCheatsFragmentBinding? = null
    private val binding get() = _binding!!

    lateinit var emptyLabel: TextView
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var outerLayout: CoordinatorLayout
    lateinit var recyclerView: RecyclerView
    lateinit var progressBar: ProgressBar
    lateinit var fab: ExtendedFloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = UnpublishedCheatsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emptyLabel = binding.emptyLabel
        outerLayout = binding.outerLayout
        recyclerView = binding.recyclerView
        progressBar = binding.progressBar
        swipeRefreshLayout = binding.swipeRefreshLayout
        fab = binding.fabSubmitCheat

        swipeRefreshLayout.setOnRefreshListener {
            reloadData()
        }

        fab.setOnClickListener { submitCheat() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        myUnpublishedCheatsViewModel =
            ViewModelProvider(this).get(MyUnpublishedCheatsViewModel::class.java)
        myUnpublishedCheatsViewModel!!.fetchListener = this

        setupRecyclerView()

        myUnpublishedCheatsViewModel?.getMyUnpublishedCheatsByCoroutines()
    }

    private fun reloadData() {
        progressBar.visibility = View.VISIBLE
        myUnpublishedCheatsViewModel?.getMyUnpublishedCheatsByCoroutines()
        swipeRefreshLayout.isRefreshing = false
    }

    override fun fetchUnpublishedCheatsSuccess(unpublishedCheats: List<UnpublishedCheat>) {
        if (unpublishedCheats.isEmpty()) {
            showEmptyListView()
        } else {
            hideEmptyListView()

            activity.setToolbarSubtitle(
                unpublishedCheats.size.toString().plus(" ").plus(
                    resources.getQuantityString(
                        R.plurals.cheats,
                        unpublishedCheats.size
                    )
                )
            )

            myUnpublishedCheatsListViewAdapter!!.unpublishedCheats = unpublishedCheats
            myUnpublishedCheatsListViewAdapter!!.notifyDataSetChanged()
        }
    }

    override fun fetchUnpublishedCheatsFail() {
        showEmptyListView()
        tools.showSnackbar(outerLayout, getString(R.string.error_fetch_unpublished_cheats))
    }

    private fun showEmptyListView() {
        emptyLabel.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        activity.setToolbarSubtitle("")

        hideProgressBar()
    }

    private fun hideEmptyListView() {
        emptyLabel.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE

        hideProgressBar()
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.GONE
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
        val intent = Intent(activity, SubmitCheatFormActivity::class.java).apply {
            putExtra("unpublishedCheat", cheat)
            putExtra("gameObj", cheat.toGame())
        }
        startActivityForResult(intent, 9)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        reloadData()
    }

    override fun onRejectReasonButtonClicked(cheat: UnpublishedCheat) {
        val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText(cheat.game.gameName, cheat.rejectReason)

        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.SimpleAlertDialog)
            .setTitle(getString(R.string.why_was_cheat_rejected))
            .setMessage(cheat.rejectReason)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                // just close dialog
            }
            .setNegativeButton(getString(R.string.copy_to_clipboard), null)
            .show()


        val copyToClipboardButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        copyToClipboardButton.setOnClickListener {
            clipboard.setPrimaryClip(clip)
            Toast.makeText(
                context,
                getString(R.string.text_copied_to_clipboard),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDeleteButtonClicked(cheat: UnpublishedCheat, position: Int) {
        MaterialAlertDialogBuilder(requireContext(), R.style.SimpleAlertDialog)
            .setTitle("\"".plus(cheat.title).plus("\""))
            .setMessage(getString(R.string.unpublished_cheat_are_you_sure_delete))
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                // just close dialog
            }
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteUnpublishedCheatAndDisplaySnackbar(cheat, position)
            }
            .show()
    }

    private fun deleteUnpublishedCheatAndDisplaySnackbar(cheat: UnpublishedCheat, position: Int) {
        myUnpublishedCheatsViewModel?.deleteUnpublishedCheat(cheat)
        this.cheatPositionInList = position
    }

    override fun deleteUnpublishedCheatSuccess(message: String) {
        val translatedReturnValue: String =
            when (message) {
                "delete_ok" -> getString(R.string.cheat_deleted)
                "delete_nok" -> getString(R.string.cheat_delete_nok)
                "wrong_pw" -> getString(R.string.error_incorrect_password)
                "member_banned" -> getString(R.string.member_banned)
                "member_not_exist" -> getString(R.string.err_no_member_data)
                "no_database_access" -> getString(R.string.no_database_access)
                else -> getString(R.string.err_occurred)
            }

        tools.showSnackbar(outerLayout, translatedReturnValue)

        removeCheatFromList(cheatPositionInList)
    }

    override fun deleteUnpublishedCheatFailed() {
        tools.showSnackbar(outerLayout, getString(R.string.error_deleting_cheat))
    }

    private fun removeCheatFromList(position: Int) {
        val mutableArrayList = ArrayList<UnpublishedCheat>()
        myUnpublishedCheatsListViewAdapter?.unpublishedCheats?.let {
            mutableArrayList.addAll(it)

            if (position < mutableArrayList.size) {
                mutableArrayList.removeAt(position)

                myUnpublishedCheatsListViewAdapter?.unpublishedCheats = mutableArrayList
                myUnpublishedCheatsListViewAdapter?.notifyItemRemoved(position)
                myUnpublishedCheatsListViewAdapter?.notifyItemRangeChanged(
                    position,
                    myUnpublishedCheatsListViewAdapter?.unpublishedCheats!!.size
                )
            } else {
                myUnpublishedCheatsListViewAdapter?.notifyDataSetChanged()
            }

            activity.setToolbarSubtitle(
                mutableArrayList.size.toString().plus(" ").plus(
                    resources.getQuantityString(
                        R.plurals.cheats,
                        mutableArrayList.size
                    )
                )
            )
        }
    }

    private fun submitCheat() {
        val explicitIntent = Intent(activity, SubmitCheatSelectGameActivity::class.java)
        startActivity(explicitIntent)
    }


}