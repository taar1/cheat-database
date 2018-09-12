package com.cheatdatabase.dialogs;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.helpers.Webservice;

import needle.Needle;

/**
 * Material Design Cheat Reporting Dialog.
 */
public class ReportCheatMaterialDialog {

    private static final String TAG = ReportCheatMaterialDialog.class.getSimpleName();

    Activity activity;

    public ReportCheatMaterialDialog(final Activity activity, final Cheat cheat, final Member member) {
        this.activity = activity;

        final String[] reasons = activity.getResources().getStringArray(R.array.report_reasons);

        new MaterialDialog.Builder(activity)
                .title(R.string.report_cheat_title)
                .items(R.array.report_reasons)
                .negativeText(R.string.cancel)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, final int which, CharSequence text) {
                        Needle.onBackgroundThread().execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Webservice.reportCheat(cheat.getCheatId(), member.getMid(), reasons[which]);
                                    postReporting(true);
                                } catch (Exception e) {
                                    postReporting(false);
                                }
                            }
                        });

                        return false;
                    }
                })
                .theme(Theme.DARK)
                .show();
    }

    private void postReporting(final boolean isSuccess) {
        Needle.onMainThread().execute(new Runnable() {
            @Override
            public void run() {
                if (isSuccess) {
                    Toast.makeText(activity, activity.getString(R.string.thanks_for_reporting), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(activity, activity.getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
