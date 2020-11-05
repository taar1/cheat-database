package com.cheatdatabase.fragments

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cheatdatabase.R
import com.cheatdatabase.activity.GamesBySystemListActivity
import com.cheatdatabase.adapters.SystemsRecycleListViewAdapter
import com.cheatdatabase.data.model.SystemModel
import com.cheatdatabase.databinding.FragmentSystemlistBinding
import com.cheatdatabase.helpers.Konstanten
import com.cheatdatabase.helpers.Tools
import com.cheatdatabase.listeners.OnSystemListItemSelectedListener
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.google.android.material.snackbar.BaseTransientBottomBar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SystemConsoleListFragment @Inject constructor() : Fragment(R.layout.fragment_systemlist), OnSystemListItemSelectedListener {
    private val TAG = "SystemConsoleListFragme"

    @Inject
    lateinit var tools: Tools

    private lateinit var bannerContainerFacebook: LinearLayout
    private lateinit var adView: AdView

    companion object {
        fun newInstance() = SystemConsoleListFragment()
    }

    private val viewModel: SystemConsoleListViewModel by viewModels()
    private lateinit var viewBinding: FragmentSystemlistBinding
    private lateinit var systemsRecycleListViewAdapter: SystemsRecycleListViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentSystemlistBinding.inflate(inflater)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        systemsRecycleListViewAdapter = SystemsRecycleListViewAdapter(this)
        prepareAdBanner()

        viewBinding.myRecyclerView.apply {
            adapter = systemsRecycleListViewAdapter
            layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
            itemAnimator?.removeDuration = 50
            setHasFixedSize(true)
        }

        viewModel.systems.observe(viewLifecycleOwner, Observer { systems ->
            systems?.apply {
                systemsRecycleListViewAdapter.setSystemPlatforms(systems)
            }
        })

        // Observer for the network error.
        viewModel.eventNetworkError.observe(viewLifecycleOwner, Observer<Boolean> { isNetworkError ->
            if (isNetworkError) onNetworkError()
        })

        // Observer for the network error shown flag
        viewModel.isNetworkErrorShown.observe(viewLifecycleOwner, Observer<Boolean> { isNetworkErrorShown ->
            if (!isNetworkErrorShown) {
                viewBinding.myRecyclerView.visibility = View.VISIBLE
                viewBinding.emptyLabel.visibility = View.GONE
            }
        })

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            toggleRefreshingAnimation(true)
            viewModel.refreshDataFromNetwork()
            toggleRefreshingAnimation(false)
        }
    }

    fun toggleRefreshingAnimation(isRefreshing: Boolean) {
        viewBinding.swipeRefreshLayout.isRefreshing = isRefreshing
    }

    private fun prepareAdBanner() {
        Log.d(TAG, "Banner: Using Facebook Audience Network")
        bannerContainerFacebook = viewBinding.bannerContainerFacebook
        bannerContainerFacebook.visibility = View.VISIBLE
        adView = AdView(context, Konstanten.FACEBOOK_AUDIENCE_NETWORK_NATIVE_BANNER_ID, AdSize.BANNER_HEIGHT_50)
        bannerContainerFacebook.addView(adView)
        adView.loadAd()
    }

    /**
     * Method for displaying a Toast error message for network errors.
     */
    private fun onNetworkError() {
        if (!viewModel.isNetworkErrorShown.value!!) {
            tools.showSnackbar(viewBinding.outerLayout, getString(R.string.error_offline_pull_down_retry), BaseTransientBottomBar.LENGTH_LONG)
            viewBinding.myRecyclerView.visibility = View.GONE
            viewBinding.emptyLabel.visibility = View.VISIBLE
            viewModel.onNetworkErrorShown()
        }
    }

    /**
     * User clicked on a console to display the games.
     */
    override fun onSystemListItemSelected(systemPlatform: SystemModel) {
        val intent = Intent(activity, GamesBySystemListActivity::class.java)
        intent.putExtra("systemObj", systemPlatform)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity).toBundle())
    }
}