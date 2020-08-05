package com.cheatdatabase.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.cheatdatabase.R
import com.cheatdatabase.activity.SubmitCheatSelectGameActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.submit_cheat_fragment.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SubmitCheatFragment(val activity: SubmitCheatSelectGameActivity) : Fragment() {

    lateinit var searchButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.submit_cheat_fragment, container, false)

        searchButton = view.search_button

        searchButton.setOnClickListener {
            activity.toolbarMenu.findItem(R.id.search).expandActionView()
        }

        return view
    }

    companion object {
        fun newInstance(activity: SubmitCheatSelectGameActivity): SubmitCheatFragment {
            return SubmitCheatFragment(activity)
        }
    }
}