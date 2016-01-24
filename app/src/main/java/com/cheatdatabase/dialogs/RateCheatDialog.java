package com.cheatdatabase.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.CheatDatabaseApplication;
import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.events.CheatRatingFinishedEvent;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.google.gson.Gson;

import org.androidannotations.annotations.EBean;

/**
 * Cheat rating dialog.
 */
@EBean
public class RateCheatDialog extends DialogFragment {

    private static final String TAG = RateCheatDialog.class.getSimpleName();

    Cheat cheatObj;
    Member member;
    int fixedRating;

//    public interface RateCheatDialogListener {
//        void onFinishRateCheatDialog(int selectedRating);
//    }

    public RateCheatDialog() {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Typeface latoFontBold = Tools.getFont(getActivity().getAssets(), Konstanten.FONT_BOLD);

        cheatObj = (Cheat) getArguments().getSerializable("cheatObj");
        member = new Gson().fromJson(getActivity().getSharedPreferences(Konstanten.PREFERENCES_FILE, 0).getString(Konstanten.MEMBER_OBJECT, null), Member.class);

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
                    fixedRating = (int) (rb.getRating() * 2);

                    new RateCheatBackgroundTask().execute(fixedRating);
                    dismiss();
                } catch (Exception e) {
                    Toast.makeText(getActivity(), R.string.no_internet, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogLayout);

        return builder.create();
    }

    private class RateCheatBackgroundTask extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected Integer doInBackground(Integer... rating) {

            try {
                Webservice.rateCheat(member.getMid(), cheatObj.getCheatId(), rating[0]);
                cheatObj.setMemberRating(rating[0]);
                return rating[0];
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
                return 0;
            }
        }

        @Override
        protected void onPostExecute(Integer rating) {
            CheatDatabaseApplication.getEventBus().post(new CheatRatingFinishedEvent(cheatObj, rating));
        }
    }

}
