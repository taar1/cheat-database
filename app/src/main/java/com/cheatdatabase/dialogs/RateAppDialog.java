package com.cheatdatabase.dialogs;

import android.content.Context;
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
import com.cheatdatabase.R;
import com.cheatdatabase.activity.MainActivity;
import com.cheatdatabase.helpers.DistinctValues;
import com.cheatdatabase.helpers.Konstanten;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ActivityContext;

public class RateAppDialog {
    private static final int MINIMUM_RATING_FOR_GOOGLE_PLAY = 4;

    private final Context context;
    private MainActivity.MainActivityCallbacks mainActivityCallbacks;
    private MaterialDialog.Builder materialDialogBuilder;

    private final SharedPreferences settings;
    private final SharedPreferences.Editor editor;
    private final String APP_RATING_LOCAL = "app_rating_local";

    private int rating = 0;

    @Inject
    public RateAppDialog(@ActivityContext Context context) {
        this.context = context;

        settings = context.getSharedPreferences(Konstanten.PREFERENCES_FILE, Context.MODE_PRIVATE);
        editor = settings.edit();

        materialDialogBuilder = new MaterialDialog.Builder(context)
                .customView(R.layout.dialog_rate_cheatdatabase, true)
                .positiveText(R.string.rate_us_submit)
                .negativeText(R.string.cancel)
                .onPositive((dialog, which) -> {
                    editor.putInt(APP_RATING_LOCAL, rating);
                    editor.apply();

                    if (rating >= MINIMUM_RATING_FOR_GOOGLE_PLAY) {
                        Toast.makeText(context, R.string.rate_us_thanks_good_rating, Toast.LENGTH_LONG).show();

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
                .cancelable(false);
    }

    public void show(MainActivity.MainActivityCallbacks mainActivityCallbacks) {
        this.mainActivityCallbacks = mainActivityCallbacks;
        makeCustomLayout(materialDialogBuilder.show());
    }

    private void makeCustomLayout(MaterialDialog materialDialog) {
        View dialogView = materialDialog.getCustomView();

        final RatingBar ratingBar = dialogView.findViewById(R.id.ratingbar);
        ratingBar.setOnRatingBarChangeListener((ratingBar1, v, b) -> rating = Math.round(ratingBar1.getRating()));
        ratingBar.setRating(Float.parseFloat(String.valueOf(settings.getInt(APP_RATING_LOCAL, 0))));
    }

    private void goToGooglePlay() {
        Uri appUri = Uri.parse(DistinctValues.GOOGLE_PLAY_URL);
        Intent intentRateApp = new Intent(Intent.ACTION_VIEW, appUri);
        if (intentRateApp.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intentRateApp);
        } else {
            Toast.makeText(context, R.string.err_other_problem, Toast.LENGTH_LONG).show();
        }
    }

    private void showBadRatingDialog() {
        MaterialDialog badRatingDialog = new MaterialDialog.Builder(context)
                .customView(R.layout.dialog_bad_rating, true)
                .positiveText(R.string.submit_feedback)
                .negativeText(R.string.no_thanks)
                .onPositive((dialog, which) -> mainActivityCallbacks.showContactFormFragmentCallback())
                .onNegative((dialog, which) -> mainActivityCallbacks.closeNagivationDrawerCallback())
                .theme(Theme.DARK)
                .cancelable(false)
                .show();

        View dialogView = badRatingDialog.getCustomView();

        final TextView dialogTitle = dialogView.findViewById(R.id.bad_rating_title);
        final TextView dialogText = dialogView.findViewById(R.id.bad_rating_text);
        dialogText.setText(context.getString(R.string.bad_rating_text, rating));
    }
}
