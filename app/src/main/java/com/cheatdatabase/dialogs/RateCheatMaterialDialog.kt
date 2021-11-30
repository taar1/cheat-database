package com.cheatdatabase.dialogs

import android.app.Activity
import android.view.View
import android.widget.RatingBar
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.customview.customView
import com.cheatdatabase.R
import com.cheatdatabase.data.RetrofitClientInstance
import com.cheatdatabase.data.model.Cheat
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.events.CheatRatingFinishedEvent
import com.cheatdatabase.helpers.Tools
import com.cheatdatabase.rest.RestApi
import com.google.gson.JsonObject
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.roundToInt

/**
 * Material Design Cheat Rating Dialog.
 */
class RateCheatMaterialDialog(
    private val activity: Activity,
    private val cheat: Cheat,
    private val member: Member,
    private val view: View,
    private val tools: Tools
) {

    private val mMaterialDialog: MaterialDialog = MaterialDialog(activity)
    private val restApi: RestApi =
        RetrofitClientInstance.getRetrofitInstance().create(RestApi::class.java)

    private var newRatingBarValue: Int

    init {
        val previousRating = (cheat.memberRating / 2) as Int
        newRatingBarValue = previousRating

        mMaterialDialog.apply {
            title(R.string.rate_cheat)
            customView(R.layout.dialog_layout_rate_cheat)
            positiveButton(R.string.ok) { dialog ->
                if (previousRating != newRatingBarValue && newRatingBarValue != 0) {
                    rateCheat(newRatingBarValue)
                }
            }
            getActionButton(WhichButton.POSITIVE).isEnabled = false

            negativeButton(R.string.cancel) { dialog ->
                dialog.dismiss()
            }
            show()
        }

        // positive.isEnabled = false

//        val ratingDialog: MaterialDialog = Builder(activity)
//            .title(R.string.rate_cheat)
//            .customView(R.layout.dialog_layout_rate_cheat, false)
//            .positiveText(R.string.ok)
//            .negativeText(R.string.cancel)
//            .onPositive { dialog, which ->
//                if (previousRating != newRatingBarValue && newRatingBarValue != 0) {
//                    rateCheat(newRatingBarValue)
//                }
//            }
//            .show()
//        val positive: View = ratingDialog.getActionButton(DialogAction.POSITIVE)
//        positive.isEnabled = false
        val dialogView: View = mMaterialDialog.view
        val ratingBar: RatingBar = dialogView.findViewById(R.id.rating_bar)
        ratingBar.rating = previousRating.toFloat()
        ratingBar.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { ratingBar1: RatingBar?, rating: Float, fromUser: Boolean ->
                newRatingBarValue = rating.roundToInt() * 2

                mMaterialDialog.getActionButton(WhichButton.POSITIVE).isEnabled =
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