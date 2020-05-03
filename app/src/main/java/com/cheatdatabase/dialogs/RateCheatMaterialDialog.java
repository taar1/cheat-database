package com.cheatdatabase.dialogs;

import android.app.Activity;
import android.view.View;
import android.widget.RatingBar;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.cheatdatabase.R;
import com.cheatdatabase.data.model.Cheat;
import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.events.CheatRatingFinishedEvent;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.rest.RestApi;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Material Design Cheat Rating Dialog.
 */
public class RateCheatMaterialDialog {
    private static final String TAG = "RateCheatMaterialDialog";

    Activity activity;
    Cheat cheat;
    Member member;
    RestApi restApi;
    View view;
    private int newRatingBarValue;

    public RateCheatMaterialDialog(final Activity activity, Cheat cheat, Member member, RestApi restApi, View view) {
        this.activity = activity;
        this.cheat = cheat;
        this.member = member;
        this.restApi = restApi;
        this.view = view;

        final int previousRating = (int) (cheat.getMemberRating() / 2);
        newRatingBarValue = previousRating;

        final MaterialDialog ratingDialog = new MaterialDialog.Builder(activity)
                .title(R.string.rate_cheat)
                .customView(R.layout.dialog_layout_rate_cheat, false)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .onPositive((dialog, which) -> {
                    if ((previousRating != newRatingBarValue) && (newRatingBarValue != 0)) {
                        rateCheat(newRatingBarValue);
                    }
                })
                .theme(Theme.DARK)
                .show();

        final View positive = ratingDialog.getActionButton(DialogAction.POSITIVE);
        positive.setEnabled(false);

        View dialogView = ratingDialog.getCustomView();
        RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);
        ratingBar.setRating(previousRating);
        ratingBar.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {
            newRatingBarValue = Math.round(rating) * 2;

            if (newRatingBarValue != previousRating * 2) {
                positive.setEnabled(true);
            } else {
                positive.setEnabled(false);
            }
        });
    }

    void rateCheat(int rating) {
        Call<JsonObject> call = restApi.rateCheat(member.getMid(), cheat.getCheatId(), rating);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> forum, Response<JsonObject> response) {
                JsonObject cheatRatingResponse = response.body();
                String cheatRatingResponseValue = cheatRatingResponse.get("successMessage").getAsString(); // inserted|updated

                cheat.setMemberRating(rating);
                Tools.showSnackbar(view, activity.getString(R.string.rating_inserted));
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable e) {
                EventBus.getDefault().post(new CheatRatingFinishedEvent(cheat, rating));
            }
        });
    }
}
