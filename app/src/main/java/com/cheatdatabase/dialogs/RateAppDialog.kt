package com.cheatdatabase.dialogs

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.cheatdatabase.R
import com.cheatdatabase.helpers.DistinctValues
import com.cheatdatabase.helpers.Konstanten
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject
import kotlin.math.roundToInt

class RateAppDialog @Inject constructor(@param:ActivityContext private val context: Context) {
    private var mMaterialDialog: MaterialDialog = MaterialDialog(context)
    private val settings: SharedPreferences =
        context.getSharedPreferences(Konstanten.PREFERENCES_FILE, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = settings.edit()

    private val APP_RATING_LOCAL = "app_rating_local"
    private var rating = 0
    private var pendingIntent: PendingIntent? = null

    companion object {
        private const val MINIMUM_RATING_FOR_GOOGLE_PLAY = 4
    }

    init {
        mMaterialDialog.apply {
            customView(R.layout.dialog_rate_app)
            positiveButton(R.string.rate_us_submit) {
                editor.putInt(APP_RATING_LOCAL, rating).apply()

                if (rating >= MINIMUM_RATING_FOR_GOOGLE_PLAY) {
                    thanksForGoodRating()
                } else {
                    showBadRatingDialog()
                }
            }
            negativeButton(R.string.cancel) { dialog ->
                dialog.dismiss()
            }
            cancelable(false)
        }
    }

    fun show(pendingIntent: PendingIntent?) {
        this.pendingIntent = pendingIntent
        makeCustomLayout()

        mMaterialDialog.show()
    }

    private fun thanksForGoodRating() {
        Toast.makeText(context, R.string.rate_us_thanks_good_rating, Toast.LENGTH_LONG)
            .show()

        Handler(Looper.getMainLooper()).postDelayed({
            goToAppStore()
        }, 1000)
    }

    private fun makeCustomLayout() {
        val dialogView: View = mMaterialDialog.view
        val ratingBar: RatingBar = dialogView.findViewById(R.id.ratingbar)
        ratingBar.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { ratingBar1: RatingBar, v: Float, b: Boolean ->
                rating = ratingBar1.rating.roundToInt()
            }
        ratingBar.rating = settings.getInt(APP_RATING_LOCAL, 0).toString().toFloat()
    }

    private fun goToAppStore() {
        val appUri = Uri.parse(DistinctValues.APP_STORE_URL)
        val intentRateApp = Intent(Intent.ACTION_VIEW, appUri)
        if (intentRateApp.resolveActivity(context.packageManager) != null) {
            context.startActivity(intentRateApp)
        }
    }

    private fun showBadRatingDialog() {
        val badRatingDialog = MaterialDialog(context)
        badRatingDialog.apply {
            customView(R.layout.dialog_bad_rating)
            positiveButton(R.string.submit_feedback) {
                pendingIntent?.send()
            }
            negativeButton(R.string.no_thanks) {
                dismiss()
            }
        }

        val starOrStars =
            rating.toString().plus(" ")
                .plus(context.resources.getQuantityString(R.plurals.stars, rating))

        val dialogView: View = badRatingDialog.view
        dialogView.apply {
            findViewById<TextView>(R.id.bad_rating_text).text =
                context.getString(R.string.bad_rating_text, starOrStars)
        }
    }


}