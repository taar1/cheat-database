package com.cheatdatabase.dialogs

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.cheatdatabase.R
import com.cheatdatabase.helpers.DistinctValues
import com.cheatdatabase.helpers.Konstanten
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject
import kotlin.math.roundToInt

class RateAppDialog @Inject constructor(@param:ActivityContext private val context: Context) {

    private var madb: MaterialAlertDialogBuilder =
        MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_AppCompat_Dialog_Alert)
    private var materialDialog: AlertDialog

    private val settings: SharedPreferences =
        context.getSharedPreferences(Konstanten.PREFERENCES_FILE, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = settings.edit()

    private var rating = 0
    private var pendingIntent: PendingIntent? = null

    companion object {
        private const val APP_RATING_LOCAL = "app_rating_local"
        private const val MINIMUM_RATING_FOR_GOOGLE_PLAY = 4
    }

    init {
        madb.setTitle(R.string.rate_us)
        madb.setMessage(R.string.rate_us_text)
        madb.setView(R.layout.dialog_rate_app)
            .setPositiveButton(R.string.rate_us_submit) { _, _ ->
                editor.putInt(APP_RATING_LOCAL, rating).apply()

                if (rating >= MINIMUM_RATING_FOR_GOOGLE_PLAY) {
                    thanksForGoodRating()
                } else {
                    showBadRatingDialog()
                }
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
        materialDialog = madb.create()

    }

    fun show(pendingIntent: PendingIntent?) {
        this.pendingIntent = pendingIntent
        makeCustomLayout()

        materialDialog.show()
    }

    private fun thanksForGoodRating() {
        Toast.makeText(context, R.string.rate_us_thanks_good_rating, Toast.LENGTH_LONG)
            .show()

        Handler(Looper.getMainLooper()).postDelayed({
            goToAppStore()
        }, 1000)
    }

    private fun makeCustomLayout() {
        val ratingBar: RatingBar? = materialDialog.findViewById(R.id.ratingbar)
        ratingBar?.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { ratingBar1: RatingBar, v: Float, b: Boolean ->
                rating = ratingBar1.rating.roundToInt()
            }
        ratingBar?.rating = settings.getInt(APP_RATING_LOCAL, 0).toString().toFloat()
    }

    private fun goToAppStore() {
        val appUri = Uri.parse(DistinctValues.APP_STORE_URL)
        val intentRateApp = Intent(Intent.ACTION_VIEW, appUri)
        if (intentRateApp.resolveActivity(context.packageManager) != null) {
            context.startActivity(intentRateApp)
        }
    }

    private fun showBadRatingDialog() {
        val df = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.bad_rating_ouch)
            .setMessage(R.string.bad_rating_text)
            .setPositiveButton(R.string.submit_feedback) { _, _ ->
                pendingIntent?.send()
            }
            .setNegativeButton(R.string.no_thanks) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
        val materialDialog = df.create()
        materialDialog.show()

        val starOrStars =
            rating.toString().plus(" ")
                .plus(context.resources.getQuantityString(R.plurals.stars, rating))


        materialDialog.findViewById<TextView>(R.id.bad_rating_text)?.text =
            context.getString(R.string.bad_rating_text, starOrStars)
    }
}