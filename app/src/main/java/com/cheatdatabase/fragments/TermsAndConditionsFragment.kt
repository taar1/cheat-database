package com.cheatdatabase.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cheatdatabase.R
import com.cheatdatabase.databinding.WebviewContainerBinding
import kotlinx.android.synthetic.main.webview_container.view.*

class TermsAndConditionsFragment : Fragment(R.layout.webview_container) {

    lateinit var viewBinding: WebviewContainerBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = WebviewContainerBinding.inflate(inflater)
        viewBinding.root.webview.loadUrl("https://www.freeprivacypolicy.com/privacy/view/1ac30e371af5decb7631a29e7eed2d15")
        return viewBinding.root
    }
}