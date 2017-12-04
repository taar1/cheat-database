package com.cheatdatabase.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.cheatdatabase.R;
import com.cheatdatabase.helpers.DistinctValues;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;

public class RateCheatDatabaseDialog {
    private static final String TAG = RateCheatDatabaseDialog.class.getSimpleName();
    private static final int MINIMUM_RATING_FOR_GOOGLE_PLAY = 4;

    private final Typeface latoFontBold;
    private final Typeface latoFontLight;
    private final Activity activity;

    private int rating = 0;

    public RateCheatDatabaseDialog(final Activity activity) {
        this.activity = activity;

        latoFontBold = Tools.getFont(activity.getAssets(), Konstanten.FONT_BOLD);
        latoFontLight = Tools.getFont(activity.getAssets(), Konstanten.FONT_LIGHT);

        MaterialDialog md = new MaterialDialog.Builder(activity)
                .customView(R.layout.dialog_rate_cheatdatabase, true)
                .positiveText(R.string.rate_us_submit)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // TODO rating in sharedpreferences speichern und nächstes mal pre-selecten

                        if (rating >= MINIMUM_RATING_FOR_GOOGLE_PLAY) {
                            Toast.makeText(activity, R.string.rate_us_thanks_good_rating, Toast.LENGTH_LONG).show();
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    goToGooglePlay();
                                }
                            }, 1000);
                        } else {
                            showBadRatingDialog();
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // User clicked CANCEL. Just close the dialog.
                    }
                })
                .theme(Theme.DARK)
                .cancelable(false)
                .show();

        View dialogView = md.getCustomView();

        final TextView ratingtext = dialogView.findViewById(R.id.ratingtext);
        ratingtext.setTypeface(latoFontLight);

        final RatingBar ratingBar = dialogView.findViewById(R.id.ratingbar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                rating = Math.round(ratingBar.getRating());
            }
        });
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
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // TODO go to feedback activity
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // User clicked CANCEL. Just close the dialog.
                    }
                })
                .theme(Theme.DARK)
                .cancelable(false)
                .show();

        // TODO text noch mit anzahl sternen ersetzen
        // TODO text noch mit anzahl sternen ersetzen
        // TODO text noch mit anzahl sternen ersetzen
        // TODO text noch mit anzahl sternen ersetzen
        // TODO text noch mit anzahl sternen ersetzen

        View dialogView = badRatingDialog.getCustomView();

        final TextView dialogTitle = dialogView.findViewById(R.id.bad_rating_title);
        dialogTitle.setTypeface(latoFontBold);
        final TextView dialogText = dialogView.findViewById(R.id.bad_rating_text);
        dialogText.setTypeface(latoFontLight);
    }
}
