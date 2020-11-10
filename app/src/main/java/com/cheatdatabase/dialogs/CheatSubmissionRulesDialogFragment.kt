package com.cheatdatabase.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.cheatdatabase.R

/**
 * NOT USED BECAUSE IT'S NOT NICE...
 *
 * How to use:
 * cheatSubmissionRulesDialogFragment = new CheatSubmissionRulesDialogFragment(title, text, buttonText);
 * cheatSubmissionRulesDialogFragment.show(getSupportFragmentManager(), "CheatSubmissionRulesDialogFragment");
 */
class CheatSubmissionRulesDialogFragment(val title: String, val text: String, val buttonText: String) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

//        val builder = AlertDialog.Builder(requireActivity())
//        val inflater = requireActivity().layoutInflater;
//
//        builder.setView(inflater.inflate(R.layout.dialog_information, null))
//        builder.setCancelable(false)
//
//        val dialogView = builder.create()
//        val title: TextView? = dialogView.findViewById(R.id.title)
//        title?.text = title.toString()
//
//        val mainText: TextView? = dialogView.findViewById(R.id.mainText)
//        mainText?.text = text
//
//        val okButton: Button? = dialogView.findViewById(R.id.okButton)
//        okButton?.text = buttonText
//
//        // Create the AlertDialog object and return it
//        return dialogView

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater

            builder.setView(inflater.inflate(R.layout.dialog_information, null))
            builder.setCancelable(false)

            val dialogView = builder.create()
            val title: TextView? = dialogView.findViewById(R.id.title)
            title?.text = title.toString()

            val mainText: TextView? = dialogView.findViewById(R.id.mainText)
            mainText?.text = text

            val okButton: Button? = dialogView.findViewById(R.id.okButton)
            okButton?.text = buttonText

            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }

}