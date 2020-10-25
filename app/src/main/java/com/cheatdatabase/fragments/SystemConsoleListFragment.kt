package com.cheatdatabase.fragments

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cheatdatabase.R
import com.cheatdatabase.activity.GamesBySystemListActivity
import com.cheatdatabase.adapters.SystemsRecycleListViewAdapter
import com.cheatdatabase.data.model.SystemModel
import com.cheatdatabase.databinding.FragmentSystemlistBinding
import com.cheatdatabase.listeners.OnSystemListItemSelectedListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SystemConsoleListFragment @Inject constructor() : Fragment(R.layout.fragment_systemlist), OnSystemListItemSelectedListener {

    private val TAG = "SystemConsoleListFragme"

    companion object {
        fun newInstance() = SystemConsoleListFragment()
    }

    private lateinit var viewBinding: FragmentSystemlistBinding
    private lateinit var viewModel: SystemConsoleListViewModel

    private lateinit var systemsRecycleListViewAdapter: SystemsRecycleListViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentSystemlistBinding.inflate(inflater)
        return viewBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "XXXXX onActivityCreated: ")

        viewModel = ViewModelProvider(this).get(SystemConsoleListViewModel::class.java)

        viewModel.allSystems.observe(viewLifecycleOwner, Observer { systems ->
            Log.d(TAG, "XXXXX 10000: ")
            systemsRecycleListViewAdapter.setSystemPlatforms(systems)

        })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "XXXXX onViewCreated: ")

        systemsRecycleListViewAdapter = SystemsRecycleListViewAdapter(this)

        viewBinding.myRecyclerView.apply {
            adapter = systemsRecycleListViewAdapter
            layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
            itemAnimator?.removeDuration = 50
            setHasFixedSize(true)
        }

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            toggleRefreshingAnimation(true)
            viewModel.getAllSystemsObserver()
            toggleRefreshingAnimation(false)
        }
    }

    fun toggleRefreshingAnimation(isRefreshing: Boolean) {
        viewBinding.swipeRefreshLayout.isRefreshing = isRefreshing
    }

    override fun onSystemListItemSelected(systemPlatform: SystemModel) {
        Log.d(TAG, "XXXXX onSystemListItemSelected: systemPlatform: " + systemPlatform.name)

        val intent = Intent(activity, GamesBySystemListActivity::class.java)
        intent.putExtra("systemObj", systemPlatform)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity).toBundle())
    }
}