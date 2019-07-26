package com.cheatdatabase.dialogs;

import android.app.Activity;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.events.CheatRatingFinishedEvent;
import com.cheatdatabase.helpers.Webservice;

import org.greenrobot.eventbus.EventBus;

/**
 * Material Design Cheat Rating Dialog.
 */
public class RateCheatMaterialDialog {

    private static final String TAG = RateCheatMaterialDialog.class.getSimpleName();

    Cheat cheat;
    Member member;
    private int newRatingBarValue;

    public RateCheatMaterialDialog(final Activity activity, Cheat cheat, Member member) {
        this.cheat = cheat;
        this.member = member;

        final int previousRating = (int) (cheat.getMemberRating() / 2);
        newRatingBarValue = previousRating;

        final MaterialDialog ratingDialog = new MaterialDialog.Builder(activity)
                .title(R.string.rate_cheat)
                .customView(R.layout.dialog_layout_rate_cheat, false)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if ((previousRating != newRatingBarValue) && (newRatingBarValue != 0)) {

                            try {
                                new RateCheatBackgroundTask().execute(newRatingBarValue);
                            } catch (Exception e) {
                                Toast.makeText(activity, R.string.no_internet, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, e.getLocalizedMessage());
                            }

                        }
                    }
                })
                .theme(Theme.DARK)
                .show();

        final View positive = ratingDialog.getActionButton(DialogAction.POSITIVE);
        positive.setEnabled(false);

        View dialogView = ratingDialog.getCustomView();
        RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);
        ratingBar.setRating(previousRating);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                newRatingBarValue = Math.round(rating) * 2;

                if (newRatingBarValue != previousRating * 2) {
                    positive.setEnabled(true);
                } else {
                    positive.setEnabled(false);
                }
            }
        });

    }

    private class RateCheatBackgroundTask extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected Integer doInBackground(Integer... rating) {

            try {
                Webservice.rateCheat(member.getMid(), cheat.getCheatId(), rating[0]);
                cheat.setMemberRating(rating[0]);
                return rating[0];
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
                return 0;
            }
        }

        @Override
        protected void onPostExecute(Integer rating) {
            EventBus.getDefault().post(new CheatRatingFinishedEvent(cheat, rating));
        }
    }

}
