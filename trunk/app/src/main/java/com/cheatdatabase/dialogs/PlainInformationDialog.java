package com.cheatdatabase.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.Window;
import android.widget.TextView;

import com.cheatdatabase.R;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;

/**
 * Dialog to display a title and text.
 *
 * @author Dominik
 */
public class PlainInformationDialog extends Dialog {

    private TextView mTitle;
    private TextView mText;

    public PlainInformationDialog(Context context) {
        super(context);

        Typeface latoFontBold = Tools.getFont(context.getAssets(), Konstanten.FONT_BOLD);
        Typeface latoFontLight = Tools.getFont(context.getAssets(), Konstanten.FONT_LIGHT);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_information_dialog);
        setTitle(R.string.guidelines);

        mTitle = (TextView) findViewById(R.id.termsandconditions_title);
        mTitle.setTypeface(latoFontBold);
        mText = (TextView) findViewById(R.id.termsandconditions_text);
        mText.setTypeface(latoFontLight);
    }

    public void setContent(int title, int text, int button) {
        mTitle.setText(title);
        mText.setText(text);
    }
}
