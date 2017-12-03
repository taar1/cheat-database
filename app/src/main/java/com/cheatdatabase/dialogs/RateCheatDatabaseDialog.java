package com.cheatdatabase.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.LinearLayout;
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

    private final Activity activity;
    private TextView mTitle;
    private TextView mText;

    private int rating = 0;


    public RateCheatDatabaseDialog(final Activity activity) {
        this.activity = activity;

        Typeface latoFontBold = Tools.getFont(activity.getAssets(), Konstanten.FONT_BOLD);
        Typeface latoFontLight = Tools.getFont(activity.getAssets(), Konstanten.FONT_LIGHT);

        MaterialDialog md = new MaterialDialog.Builder(activity)
                .customView(R.layout.dialog_rate_cheatdatabase, true)
                .positiveText(R.string.rate_us_submit)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Toast.makeText(activity, R.string.rate_us_thanks_good_rating, Toast.LENGTH_LONG).show();
                        if (rating >= MINIMUM_RATING_FOR_GOOGLE_PLAY) {
                            goToGooglePlay();
                        } else {
                            // TODO show dialog for feedback...
                            // TODO show dialog for feedback...
                            // TODO show dialog for feedback...
                            // TODO show dialog for feedback...
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

        final LinearLayout ratingLayout = dialogView.findViewById(R.id.rating_layout);
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
}
