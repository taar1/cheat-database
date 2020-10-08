package com.cheatdatabase.activity.ui.mycheats.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cheatdatabase.R

class EditCheatFragment : Fragment() {

    companion object {
        fun newInstance() = EditCheatFragment()
    }

    private lateinit var viewModel: EditCheatViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(this).get(EditCheatViewModel::class.java)
        // TODO: Use the ViewModel
    }

}