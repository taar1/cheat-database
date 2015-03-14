package com.cheatdatabase.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.google.analytics.tracking.android.Log;
import com.google.gson.Gson;

/**
 * Cheat rating dialog.
 */
public class RateCheatDialog extends DialogFragment {

    public interface RateCheatDialogListener {
        void onFinishRateCheatDialog(int selectedRating);
    }

    private Typeface latoFontBold;
    private Typeface latoFontLight;

    public RateCheatDialog() {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        latoFontBold = Tools.getFont(getActivity().getAssets(), "Lato-Bold.ttf");
        latoFontLight = Tools.getFont(getActivity().getAssets(), "Lato-Light.ttf");

        final Cheat cheatObj = (Cheat) getArguments().getSerializable("cheatObj");
        final Member member = new Gson().fromJson(getActivity().getSharedPreferences(Konstanten.PREFERENCES_FILE, 0).getString(Konstanten.MEMBER_OBJECT, null), Member.class);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.layout_rate_cheat, null);

        final RatingBar rb = (RatingBar) dialogLayout.findViewById(R.id.ratingbar);
        rb.setRating(cheatObj.getMemberRating() / 2);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getActivity().getString(R.string.rate_cheat_title, cheatObj.getCheatTitle()));
        builder.setPositiveButton(R.string.rate_cheat_plain, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    final int fixedRating = (int) (rb.getRating() * 2);

                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            int status = Webservice.rateCheat(member.getMid(), cheatObj.getCheatId(), fixedRating);
                            cheatObj.setMemberRating(fixedRating);

                            // getActivity().runOnUiThread(new Runnable() {
                            //
                            // @Override
                            // public void run() {
                            // // 1 = Insert, 2 = Update
                            // if (status == 2) {
                            // Toast.makeText(getActivity(),
                            // R.string.rating_updated,
                            // Toast.LENGTH_SHORT).show();
                            // } else {
                            // Toast.makeText(getActivity(),
                            // R.string.rating_inserted,
                            // Toast.LENGTH_SHORT).show();
                            // }
                            // }
                            // });

                        }
                    }).start();
                    RateCheatDialogListener activity = (RateCheatDialogListener) getActivity();
                    activity.onFinishRateCheatDialog(fixedRating);
                } catch (Exception e) {
                    Log.e(e);
                    Toast.makeText(getActivity(), R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
                dismiss();
            }

        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }

        });

        builder.setView(dialogLayout);

        return builder.create();
    }

}
