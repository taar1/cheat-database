package com.cheatdatabase.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.cheatdatabase.R;

/**
 * Dialog displaying a list of reasons to report a particular cheat.
 *
 * @author Dominik
 */
public class ReportCheatDialog extends DialogFragment {

    public interface ReportCheatDialogListener {
        void onFinishReportDialog(int selectedReason);
    }

    public ReportCheatDialog() {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.report_cheat_title).setItems(R.array.report_reasons, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int selectedReason) {
                ReportCheatDialogListener activity = (ReportCheatDialogListener) getActivity();
                activity.onFinishReportDialog(selectedReason);
                dismiss();
            }
        });
        return builder.create();
    }

}
