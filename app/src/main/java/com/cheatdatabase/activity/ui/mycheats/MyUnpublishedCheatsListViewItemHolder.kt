package com.cheatdatabase.activity.ui.mycheats

import android.app.Activity
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cheatdatabase.R
import com.cheatdatabase.data.model.UnpublishedCheat
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.unpublished_cheat_list_item.view.*
import java.text.DateFormat
import java.util.*

class MyUnpublishedCheatsListViewItemHolder(
    val view: View, val activity: Activity
) : RecyclerView.ViewHolder(view) {

    lateinit var deleteButton: MaterialButton
    lateinit var editButton: MaterialButton
    lateinit var detailsButton: MaterialButton

    var submissionStatusLayout: RelativeLayout? = view.submission_status_layout
    var submissionStatus: TextView? = view.submission_status
    var gameAndSystem: TextView? = view.game_and_system
    var cheatTitle: TextView? = view.cheat_title
    var cheatText: TextView? = view.cheat_text
    var submissionDate: TextView? = view.submission_date
    //var deleteButton: MaterialButton? = view.delete_button

    init {
        deleteButton = view.delete_button
        editButton = view.edit_button
        detailsButton = view.details_button
    }

    fun updateUI(uc: UnpublishedCheat) {

        if (uc.title.isNotEmpty()) {
            cheatTitle?.text = "\"".plus(uc.title).plus("\"")
            cheatTitle?.visibility = View.VISIBLE
        } else {
            cheatTitle?.visibility = View.GONE
        }

        cheatText?.text = uc.cheat
        gameAndSystem?.text = uc.game.gameName.plus(" (").plus(uc.system.systemName).plus(")")

        submissionDate?.text = activity.resources.getString(R.string.submitted).plus(": ")
            .plus(formatDateToString(uc.created))


        if (uc.tableInfo.equals("cheat_submissions", ignoreCase = true)) {
            detailsButton.visibility = View.GONE

            submissionStatus?.setText(R.string.pending_approval)

            submissionStatusLayout?.setBackgroundColor(
                activity.resources.getColor(R.color.dark_gray, null)
            )
        } else if (uc.tableInfo.equals("rejected_cheats", ignoreCase = true)) {

            submissionStatus?.setText(R.string.rejected)

            if (uc.rejectReason.isNullOrEmpty()) {
                detailsButton.visibility = View.GONE
            } else {
                detailsButton.visibility = View.VISIBLE
            }

            submissionStatusLayout?.setBackgroundColor(
                activity.resources.getColor(R.color.dark_red, null)
            )
        }

    }

    private fun formatDateToString(date: Date): String? {
        val f: DateFormat =
            DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault())
        return f.format(date)
    }
}