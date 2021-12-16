package com.cheatdatabase.dialogs

import android.app.Activity
import android.view.View
import android.widget.RatingBar
import androidx.appcompat.app.AlertDialog
import com.cheatdatabase.R
import com.cheatdatabase.data.RetrofitClientInstance
import com.cheatdatabase.data.model.Cheat
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.events.CheatRatingFinishedEvent
import com.cheatdatabase.helpers.Tools
import com.cheatdatabase.rest.RestApi
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonObject
import org.greenrobot.eventbus.EventBus
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
    private val member: Member,
    private val view: View,
    private val tools: Tools
) {
    private var madb: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(activity)
    private val restApi: RestApi =
        RetrofitClientInstance.getRetrofitInstance().create(RestApi::class.java)

    private var newRatingBarValue: Int

    init {
        val previousRating = (cheat.memberRating / 2) as Int
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
        val call: Call<JsonObject> = restApi.rateCheat(member.mid, cheat.cheatId, rating)
        call.enqueue(object : Callback<JsonObject?> {
            override fun onResponse(forum: Call<JsonObject?>, response: Response<JsonObject?>) {
                cheat.memberRating = rating.toFloat()
                tools.showSnackbar(view, activity.getString(R.string.rating_inserted))
            }

            override fun onFailure(call: Call<JsonObject?>, e: Throwable) {
                // TODO FIXME event bus entfernen, ADD CALLBACK
                // TODO FIXME event bus entfernen, ADD CALLBACK
                // TODO FIXME event bus entfernen, ADD CALLBACK
                EventBus.getDefault().post(CheatRatingFinishedEvent(cheat, rating))
            }
        })
    }


}