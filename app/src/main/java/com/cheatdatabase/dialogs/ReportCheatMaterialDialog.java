package com.cheatdatabase.dialogs;

import android.app.Activity;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.cheatdatabase.R;
import com.cheatdatabase.data.RetrofitClientInstance;
import com.cheatdatabase.data.model.Cheat;
import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.helpers.Tools;
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

    private Tools tools;

    private final View view;
    Activity activity;
    RestApi restApi;

    public ReportCheatMaterialDialog(final Activity activity, final Cheat cheat, final Member member, View view, Tools tools) {
        this.activity = activity;
        this.view = view;
        this.tools = tools;

        restApi = RetrofitClientInstance.getRetrofitInstance().create(RestApi.class);

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

                String cheatRatingResponseValue = cheatRatingResponse.get("returnValue").getAsString(); // inserted|updated|invalid_parameters
                if (cheatRatingResponseValue.equalsIgnoreCase("inserted") || (cheatRatingResponseValue.equalsIgnoreCase("updated"))) {
                    postReporting(true);
                } else {
                    postReporting(false);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable e) {
                postReporting(false);
            }
        });
    }

    private void postReporting(final boolean isSuccess) {
        if (isSuccess) {
            tools.showSnackbar(view, activity.getString(R.string.thanks_for_reporting));
        } else {
            tools.showSnackbar(view, activity.getString(R.string.err_occurred));
        }
    }
}
