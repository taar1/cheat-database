package com.cheatdatabase.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

import com.cheatdatabase.R;
import com.cheatdatabase.model.Member;
import com.cheatdatabase.helpers.Konstanten;
import com.google.gson.Gson;

public class AlreadyLoggedInDialog extends DialogFragment {

    public interface AlreadyLoggedInDialogListener {
        void onFinishDialog(boolean signOutNow);
    }

    public AlreadyLoggedInDialog() {
        // TODO abfangen, wenn der dialog ohne eingabe geschlossen wird.
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Member member = new Gson().fromJson(getActivity().getSharedPreferences(Konstanten.PREFERENCES_FILE, 0).getString(Konstanten.MEMBER_OBJECT, null), Member.class);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.already_logged_in, member.getEmail())).setPositiveButton(R.string.sign_out, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                AlreadyLoggedInDialogListener activity = (AlreadyLoggedInDialogListener) getActivity();
                activity.onFinishDialog(true);
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                AlreadyLoggedInDialogListener activity = (AlreadyLoggedInDialogListener) getActivity();
                activity.onFinishDialog(false);
            }
        });
        builder.setCancelable(false);
        return builder.create();
    }
}