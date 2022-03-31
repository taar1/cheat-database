package com.cheatdatabase.dialogs

import android.app.Activity
import android.view.View
import android.widget.RatingBar
import androidx.appcompat.app.AlertDialog
import com.cheatdatabase.R
import com.cheatdatabase.callbacks.OnCheatRated
import com.cheatdatabase.data.model.Cheat
import com.cheatdatabase.events.CheatRatingFinishedEvent
import com.cheatdatabase.helpers.Tools
import com.cheatdatabase.rest.RestApi
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.roundToInt

/**
 * Cheat Rating Dialog.
 */
class RateCheatMaterialDialog(
    private val activity: Activity,
    private val cheat: Cheat,
    private val view: View,
    private val tools: Tools,
    private val restApi: RestApi,
    private val onCheatRated: OnCheatRated
) {
    private var madb: MaterialAlertDialogBuilder =
        MaterialAlertDialogBuilder(activity, R.style.SimpleAlertDialog)
    private var newRatingBarValue: Int

    init {
        val previousRating = (cheat.memberRating / 2).toInt()
        newRatingBarValue = previousRating

        madb.setTitle(R.string.rate_cheat)
            .setView(R.layout.dialog_layout_rate_cheat)
            .setPositiveButton(R.string.ok) { _, _ ->
                if (previousRating != newRatingBarValue && newRatingBarValue != 0) {
                    rateCheat(newRatingBarValue)
                }
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        val materialDialog = madb.create()
        materialDialog.show()

        val ratingBar: RatingBar? = materialDialog.findViewById(R.id.rating_bar)
        ratingBar?.rating = previousRating.toFloat()
        ratingBar?.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { ratingBar1: RatingBar?, rating: Float, fromUser: Boolean ->
                newRatingBarValue = rating.roundToInt() * 2

                materialDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                    newRatingBarValue != previousRating * 2
            }
    }


    private fun rateCheat(rating: Int) {
        val call: Call<JsonObject> = restApi.rateCheat(tools.member.mid, cheat.cheatId, rating)
        call.enqueue(object : Callback<JsonObject?> {
            override fun onResponse(forum: Call<JsonObject?>, response: Response<JsonObject?>) {
                cheat.memberRating = rating.toFloat()
                tools.showSnackbar(view, activity.getString(R.string.rating_inserted))

                onCheatRated.onCheatRated(CheatRatingFinishedEvent(cheat, rating, true))
            }

            override fun onFailure(call: Call<JsonObject?>, throwable: Throwable) {
                onCheatRated.onCheatRated(CheatRatingFinishedEvent(throwable, false))
            }
        })
    }


}