package com.cheatdatabase.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.cheatdatabase.R;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;

/**
 * Dialog displaying a list of reasons to report a particular cheat.
 *
 * @author Dominik
 */
public class ReportCheatDialog extends DialogFragment {

    int selectedItem = 0;

    public interface ReportCheatDialogListener {
        void onFinishReportDialog(int selectedReason);
    }

    public ReportCheatDialog() {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Typeface latoFontBold = Tools.getFont(getActivity().getAssets(), Konstanten.FONT_BOLD);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.layout_report_cheat, null);

        TextView title = (TextView) dialogLayout.findViewById(R.id.title);
        title.setTypeface(latoFontBold);

        final ListView lv = (ListView) dialogLayout.findViewById(R.id.listView);
        String[] reportReasons = getResources().getStringArray(R.array.report_reasons);
        lv.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, reportReasons));
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItem = position;
                for (int a = 0; a < parent.getChildCount(); a++) {
                    parent.getChildAt(a).setBackgroundColor(getResources().getColor(R.color.color_primary_dark));
                }
                parent.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.color_primary_light));
                parent.invalidate();
            }
        });

        Button cancelButton = (Button) dialogLayout.findViewById(R.id.btn_cancel);
        cancelButton.setTypeface(latoFontBold);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button reportButton = (Button) dialogLayout.findViewById(R.id.btn_report);
        reportButton.setTypeface(latoFontBold);
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReportCheatDialogListener activity = (ReportCheatDialogListener) getActivity();
                activity.onFinishReportDialog(selectedItem);
                dismiss();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogLayout);

        return builder.create();


    }

}
