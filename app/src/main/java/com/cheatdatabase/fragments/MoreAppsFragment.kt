package com.cheatdatabase.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cheatdatabase.R
import com.cheatdatabase.databinding.WebviewContainerBinding
import kotlinx.android.synthetic.main.webview_container.view.*

class MoreAppsFragment : Fragment(R.layout.webview_container) {

    lateinit var viewBinding: WebviewContainerBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = WebviewContainerBinding.inflate(inflater)
        viewBinding.root.webview.loadUrl(resources.getString(R.string.more_apps_url))
        return viewBinding.root
    }
}