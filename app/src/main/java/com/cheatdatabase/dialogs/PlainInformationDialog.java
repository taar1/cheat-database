package com.cheatdatabase.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.cheatdatabase.R;
import com.cheatdatabase.helpers.Tools;

/**
 * Dialog to display a title, text and one OK button.
 *
 * @author Dominik
 */
public class PlainInformationDialog extends Dialog implements OnClickListener {

    private TextView mTitle;
    private TextView mText;
    private Button mButton;

    public PlainInformationDialog(Context context) {
        super(context);

        Typeface latoFontBold = Tools.getFont(context.getAssets(), "Lato-Bold.ttf");
        Typeface latoFontLight = Tools.getFont(context.getAssets(), "Lato-Light.ttf");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_information_dialog);
        setTitle(R.string.guidelines);

        mTitle = (TextView) findViewById(R.id.termsandconditions_title);
        mTitle.setTypeface(latoFontBold);
        mText = (TextView) findViewById(R.id.termsandconditions_text);
        mText.setTypeface(latoFontLight);

        mButton = (Button) findViewById(R.id.okbutton);
        mButton.setTypeface(latoFontBold);
        mButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == mButton) {
            this.dismiss();
        }
    }

    public void setContent(int title, int text, int button) {
        mTitle.setText(title);
        mText.setText(text);
        mButton.setText(button);
    }
}
