package com.cheatdatabase.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.TextView;

import com.cheatdatabase.R;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ActivityContext;

/**
 * Simple dialog to display a title and text.
 *
 * @author Dominik Erbsland
 */
public class PlainInformationDialog extends Dialog {
    private TextView mTitle;
    private TextView mText;

    @Inject
    public PlainInformationDialog(@ActivityContext Context context) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_information);
        setTitle(R.string.guidelines);

        mTitle = findViewById(R.id.termsandconditions_title);
        mText = findViewById(R.id.termsandconditions_text);
    }

    public void setContent(int title, int text, int button) {
        mTitle.setText(title);
        mText.setText(text);

        show();
    }
}
