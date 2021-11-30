package com.cheatdatabase.dialogs

import android.app.PendingIntent
import android.content.Context
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView

@Deprecated("Wird wohl nie gebraucht, war ein Experiment")
class DialogGenerator(
    val context: Context,
    val pendingIntent: PendingIntent?,
    val title: String,
    val bodyText: String,
    val customView: Int?,
    val positiveButtonText: Int?,
    val negativeButtonText: Int?,
    onPositiveAction: () -> Unit?,
    onNegativeAction: () -> Unit?
) {
    private val mDialog = MaterialDialog(context)

    init {
        mDialog.apply {
            customView?.let {
                customView(it)
            }

            positiveButtonText?.let {
                positiveButton(positiveButtonText) {
                    pendingIntent?.send()
                }
            }

            negativeButtonText?.let {
                negativeButton(negativeButtonText) {
                    dismiss()
                }
            }

        }

    }

    fun show() {
        mDialog.show()
    }

}