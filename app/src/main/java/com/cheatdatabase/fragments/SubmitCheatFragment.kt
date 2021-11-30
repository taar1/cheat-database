package com.cheatdatabase.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cheatdatabase.R
import com.cheatdatabase.activity.SubmitCheatSelectGameActivity
import com.cheatdatabase.databinding.SubmitCheatFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SubmitCheatFragment(val activity: SubmitCheatSelectGameActivity) : Fragment() {

    private var _binding: SubmitCheatFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = SubmitCheatFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchButton.setOnClickListener {
            activity.toolbarMenu.findItem(R.id.search).expandActionView()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(activity: SubmitCheatSelectGameActivity): SubmitCheatFragment {
            return SubmitCheatFragment(activity)
        }
    }
}