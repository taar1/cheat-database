package com.cheatdatabase.dialogs;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.cheatdatabase.R;
import com.cheatdatabase.model.Cheat;
import com.cheatdatabase.model.Member;
import com.cheatdatabase.rest.RestApi;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Material Design Cheat Reporting Dialog.
 */
public class ReportCheatMaterialDialog {
    private static final String TAG = "ReportCheatMaterialDial";

    Activity activity;
    RestApi restApi;

    public ReportCheatMaterialDialog(final Activity activity, final Cheat cheat, final Member member, RestApi restApi) {
        this.activity = activity;
        this.restApi = restApi;

        final String[] reasons = activity.getResources().getStringArray(R.array.report_reasons);

        new MaterialDialog.Builder(activity)
                .title(R.string.report_cheat_title)
                .items(R.array.report_reasons)
                .negativeText(R.string.cancel)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, final int which, CharSequence text) {
                        reportCheat(cheat.getCheatId(), member.getMid(), reasons[which]);
                        return false;
                    }
                })
                .theme(Theme.DARK)
                .show();
    }

    void reportCheat(int cheatId, int memberId, String reason) {
        Call<JsonObject> call = restApi.reportCheat(cheatId, memberId, reason);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> forum, Response<JsonObject> response) {
                JsonObject cheatRatingResponse = response.body();

                // TODO FIXME hier den return value handlen...
                // TODO FIXME hier den return value handlen...
                // TODO FIXME hier den return value handlen...
                // TODO FIXME hier den return value handlen...
                // TODO FIXME hier den return value handlen...
                // TODO FIXME hier den return value handlen...
                // TODO FIXME hier den return value handlen...
                // TODO FIXME hier den return value handlen...
                // TODO FIXME hier den return value handlen...
                // TODO FIXME hier den return value handlen...
                // TODO FIXME hier den return value handlen...
                // TODO FIXME hier den return value handlen...
                // TODO FIXME hier den return value handlen...
                String cheatRatingResponseValue = cheatRatingResponse.get("returnValue").getAsString(); // inserted|updated|invalid_parameters


                postReporting(true);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable e) {
                postReporting(false);
            }
        });
    }

    private void postReporting(final boolean isSuccess) {
        if (isSuccess) {
            Toast.makeText(activity, activity.getString(R.string.thanks_for_reporting), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(activity, activity.getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        }
    }
}
