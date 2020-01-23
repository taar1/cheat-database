package com.cheatdatabase.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.cheatdatabase.activity.MainActivity;
import com.cheatdatabase.R;
import com.cheatdatabase.helpers.DistinctValues;
import com.cheatdatabase.helpers.Konstanten;

public class RateAppDialog {
    private static final String TAG = RateAppDialog.class.getSimpleName();
    private static final int MINIMUM_RATING_FOR_GOOGLE_PLAY = 4;

    private final Activity activity;
    private final MainActivity.MainActivityCallbacks mainActivityCallbacks;

    private final SharedPreferences settings;
    private final SharedPreferences.Editor editor;
    private final String APP_RATING_LOCAL = "app_rating_local";

    private int rating = 0;

    public RateAppDialog(final Activity activity, MainActivity.MainActivityCallbacks mainActivityCallbacks) {
        this.activity = activity;
        this.mainActivityCallbacks = mainActivityCallbacks;

        settings = activity.getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();

        MaterialDialog md = new MaterialDialog.Builder(activity)
                .customView(R.layout.dialog_rate_cheatdatabase, true)
                .positiveText(R.string.rate_us_submit)
                .negativeText(R.string.cancel)
                .onPositive((dialog, which) -> {
                    editor.putInt(APP_RATING_LOCAL, rating);
                    editor.apply();

                    if (rating >= MINIMUM_RATING_FOR_GOOGLE_PLAY) {
                        Toast.makeText(activity, R.string.rate_us_thanks_good_rating, Toast.LENGTH_LONG).show();
                        Handler handler = new Handler();
                        handler.postDelayed(() -> goToGooglePlay(), 1000);
                    } else {
                        showBadRatingDialog();
                    }
                })
                .onNegative((dialog, which) -> {
                    // User clicked CANCEL. Just close the dialog.
                })
                .theme(Theme.DARK)
                .cancelable(false)
                .show();

        View dialogView = md.getCustomView();

        final RatingBar ratingBar = dialogView.findViewById(R.id.ratingbar);
        ratingBar.setOnRatingBarChangeListener((ratingBar1, v, b) -> rating = Math.round(ratingBar1.getRating()));
        ratingBar.setRating(Float.valueOf(String.valueOf(settings.getInt(APP_RATING_LOCAL, 0))));
    }

    private void goToGooglePlay() {
        Uri appUri = Uri.parse(DistinctValues.GOOGLE_PLAY_URL);
        Intent intentRateApp = new Intent(Intent.ACTION_VIEW, appUri);
        if (intentRateApp.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intentRateApp);
        } else {
            Toast.makeText(activity, R.string.err_other_problem, Toast.LENGTH_LONG).show();
        }
    }

    private void showBadRatingDialog() {
        MaterialDialog badRatingDialog = new MaterialDialog.Builder(activity)
                .customView(R.layout.dialog_bad_rating, true)
                .positiveText(R.string.submit_feedback)
                .negativeText(R.string.no_thanks)
                .onPositive((dialog, which) -> mainActivityCallbacks.showContactFormFragmentCallback())
                .onNegative((dialog, which) -> {
                    // User clicked CANCEL. Just close the dialog.
                })
                .theme(Theme.DARK)
                .cancelable(false)
                .show();

        View dialogView = badRatingDialog.getCustomView();

        final TextView dialogTitle = dialogView.findViewById(R.id.bad_rating_title);
        final TextView dialogText = dialogView.findViewById(R.id.bad_rating_text);
        dialogText.setText(activity.getString(R.string.bad_rating_text, rating));
    }
}
