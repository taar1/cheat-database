package com.cheatdatabase.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cheatdatabase.R
import com.cheatdatabase.databinding.WebviewContainerBinding

class MoreAppsFragment : Fragment() {

    companion object {
        private const val TAG = "MoreAppsFragment"
    }

    private var _binding: WebviewContainerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = WebviewContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val resourcesUrl = resources.getString(R.string.more_apps_url)
        Log.d(TAG, "App Store URL: ".plus(resourcesUrl))

        binding.webview.loadUrl(resourcesUrl)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}