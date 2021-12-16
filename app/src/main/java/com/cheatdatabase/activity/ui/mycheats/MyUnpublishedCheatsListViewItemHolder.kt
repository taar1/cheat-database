package com.cheatdatabase.activity.ui.mycheats

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.cheatdatabase.R
import com.cheatdatabase.data.model.UnpublishedCheat
import com.cheatdatabase.databinding.UnpublishedCheatListItemBinding
import com.google.android.material.button.MaterialButton
import java.text.DateFormat
import java.util.*

class MyUnpublishedCheatsListViewItemHolder(
    val binding: UnpublishedCheatListItemBinding, val activity: MyUnpublishedCheatsListActivity
) : RecyclerView.ViewHolder(binding.root) {

    val deleteButton: MaterialButton = binding.deleteButton
    val editButton: MaterialButton = binding.editButton
    val detailsButton: MaterialButton = binding.detailsButton

    fun setCheat(cheat: UnpublishedCheat) {
        with(binding) {
            if (cheat.title.isNotEmpty()) {
                cheatTitle.text = "\"".plus(cheat.title).plus("\"")
                cheatTitle.visibility = View.VISIBLE
            } else {
                cheatTitle.visibility = View.GONE
            }

            cheatText.text = cheat.cheat
            gameAndSystem.text =
                cheat.game.gameName.plus(" (").plus(cheat.system.systemName).plus(")")

            submissionDate.text = activity.resources.getString(R.string.submitted).plus(": ")
                .plus(formatDateToString(cheat.created))

            if (cheat.tableInfo.equals("cheat_submissions", ignoreCase = true)) {
                detailsButton.visibility = View.GONE

                submissionStatus.setText(R.string.pending_approval)

                submissionStatusLayoutBottom.setBackgroundColor(
                    activity.resources.getColor(
                        R.color.dark_gray,
                        null
                    )
                )
                submissionStatusLayout.setBackgroundColor(
                    activity.resources.getColor(R.color.dark_gray, null)
                )
            } else if (cheat.tableInfo.equals("rejected_cheats", ignoreCase = true)) {
                submissionStatus.setText(R.string.rejected)

                if (cheat.rejectReason.isNullOrEmpty()) {
                    detailsButton.visibility = View.GONE
                } else {
                    detailsButton.visibility = View.VISIBLE
                }

                submissionStatusLayout.setBackgroundColor(
                    activity.resources.getColor(R.color.dark_red, null)
                )
                submissionStatusLayoutBottom.setBackgroundColor(
                    activity.resources.getColor(
                        R.color.dark_red,
                        null
                    )
                )
            }
        }
    }

    private fun formatDateToString(date: Date): String? {
        val f: DateFormat =
            DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault())
        return f.format(date)
    }
}