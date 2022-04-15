package com.cheatdatabase.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.cheatdatabase.R
import com.cheatdatabase.data.model.Member

class AlreadyLoggedInDialog(val member: Member) : DialogFragment() {

    interface AlreadyLoggedInDialogListener {
        fun onFinishDialog(signOutNow: Boolean)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(getString(R.string.already_logged_in, member.email))
            .setPositiveButton(R.string.sign_out) { _: DialogInterface?, _: Int ->
                val activity = activity as AlreadyLoggedInDialogListener?
                activity?.onFinishDialog(true)
            }.setNegativeButton(R.string.cancel) { _: DialogInterface?, _: Int ->
                val activity = activity as AlreadyLoggedInDialogListener?
                activity?.onFinishDialog(false)
            }
        builder.setCancelable(false)
        return builder.create()
    }

    companion object {
        const val TAG = "AlreadyLoggedInDialog"
    }
}