package com.cheatdatabase.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.google.gson.Gson;

/**
 * Cheat rating dialog.
 */
public class RateCheatDialog extends DialogFragment {

    public interface RateCheatDialogListener {
        void onFinishRateCheatDialog(int selectedRating);
    }

    public RateCheatDialog() {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Typeface latoFontBold = Tools.getFont(getActivity().getAssets(), Konstanten.FONT_BOLD);

        final Cheat cheatObj = (Cheat) getArguments().getSerializable("cheatObj");
        final Member member = new Gson().fromJson(getActivity().getSharedPreferences(Konstanten.PREFERENCES_FILE, 0).getString(Konstanten.MEMBER_OBJECT, null), Member.class);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.layout_rate_cheat, null);

        final RatingBar rb = (RatingBar) dialogLayout.findViewById(R.id.ratingbar);
        rb.setRating(cheatObj.getMemberRating() / 2);

        TextView title = (TextView) dialogLayout.findViewById(R.id.title);
        title.setTypeface(latoFontBold);

        Button cancelButton = (Button) dialogLayout.findViewById(R.id.btn_cancel);
        cancelButton.setTypeface(latoFontBold);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button rateButton = (Button) dialogLayout.findViewById(R.id.btn_rate);
        rateButton.setTypeface(latoFontBold);
        rateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final int fixedRating = (int) (rb.getRating() * 2);

                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            Webservice.rateCheat(member.getMid(), cheatObj.getCheatId(), fixedRating);
                            cheatObj.setMemberRating(fixedRating);
                        }
                    }).start();
                    RateCheatDialogListener activity = (RateCheatDialogListener) getActivity();
                    activity.onFinishRateCheatDialog(fixedRating);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), R.string.no_internet, Toast.LENGTH_SHORT).show();
                    throw e;
                }
                dismiss();
            }
        });


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogLayout);

        return builder.create();
    }

}
