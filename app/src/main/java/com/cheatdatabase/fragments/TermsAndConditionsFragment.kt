package com.cheatdatabase.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cheatdatabase.R
import com.cheatdatabase.databinding.WebviewContainerBinding

class TermsAndConditionsFragment : Fragment(R.layout.webview_container) {

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

        binding.webview.loadUrl("https://www.freeprivacypolicy.com/privacy/view/1ac30e371af5decb7631a29e7eed2d15")
    }

}