package com.cheatdatabase;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.dialogs.PlainInformationDialog;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.google.analytics.tracking.android.Tracker;
import com.google.gson.Gson;
import com.splunk.mint.Mint;

/**
 * Liste mit allen Cheats eines Games.
 * http://developer.android.com/guide/tutorials/views/hello-tablelayout.html
 *
 * @author erbsland
 */
public class SubmitCheatActivity extends ActionBarActivity implements OnClickListener {

    private TextView textCheatTitle;
    private EditText cheatTitle;
    private EditText cheatText;
    private CheckBox checkBoxTerms;
    private Button sendButton;

    private Member member;
    private Game gameObj;

    private SharedPreferences settings;
    private Typeface latoFontBold;
    private Typeface latoFontLight;

    private static final String SCREEN_LABEL = "Submit Cheat Screen";
    protected Tracker tracker;
    private Toolbar mToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_cheat_layout);

        handleIntent(getIntent());
        init();

        textCheatTitle = (TextView) findViewById(R.id.text_cheat_submission_title);
        textCheatTitle.setTypeface(latoFontBold);
        cheatTitle = (EditText) findViewById(R.id.edit_cheat_title);
        cheatTitle.setTypeface(latoFontLight);
        cheatText = (EditText) findViewById(R.id.edit_cheat_text);
        cheatText.setTypeface(latoFontLight);
        checkBoxTerms = (CheckBox) findViewById(R.id.checkbox_terms);
        checkBoxTerms.setTypeface(latoFontLight);

        sendButton = (Button) findViewById(R.id.send_button);
        sendButton.setOnClickListener(this);
        sendButton.setTypeface(latoFontBold);
        changeButton();
    }

    private void init() {
        Reachability.registerReachability(this.getApplicationContext());
        Mint.initAndStartSession(this, Konstanten.SPLUNK_MINT_API_KEY);

        Tools.initToolbarBase(this, mToolbar);

        settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);

        latoFontLight = Tools.getFont(getAssets(), Konstanten.FONT_LIGHT);
        latoFontBold = Tools.getFont(getAssets(), Konstanten.FONT_BOLD);

        Tools.initGA(SubmitCheatActivity.this, tracker, SCREEN_LABEL, "Submit Cheat", "Cheat submission form");
    }

    private void handleIntent(final Intent intent) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                gameObj = (Game) intent.getSerializableExtra("gameObj");

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        getSupportActionBar().setTitle(gameObj.getGameName());
                        getSupportActionBar().setSubtitle(gameObj.getSystemName());

                    }
                });
            }
        }).start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_termsandconditions:
                PlainInformationDialog termsDialog = new PlainInformationDialog(this);
                termsDialog.setContent(R.string.guidelines, R.string.submit_cheat_consent_text, R.string.ok);
                termsDialog.show();
                return true;
            case R.id.action_guidelines:
                PlainInformationDialog instructionsDialog = new PlainInformationDialog(this);
                instructionsDialog.setContent(R.string.submit_cheat_instructions_title, R.string.submit_cheat_guidelines, R.string.ok);
                instructionsDialog.show();
                return true;
            case R.id.action_login:
                login();
                return true;
            case R.id.action_logout:
                member = null;
                Tools.logout(SubmitCheatActivity.this, settings.edit());
                changeButton();
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // Return result code. Login success, Register success etc.
            int intentReturnCode = data.getIntExtra("result", Konstanten.LOGIN_REGISTER_FAIL_RETURN_CODE);

            if (requestCode == Konstanten.LOGIN_REGISTER_OK_RETURN_CODE) {
                member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
                changeButton();

                invalidateOptionsMenu();
                if ((member != null) && intentReturnCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
                    Toast.makeText(SubmitCheatActivity.this, R.string.register_thanks, Toast.LENGTH_LONG).show();
                } else if ((member != null) && intentReturnCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
                    Toast.makeText(SubmitCheatActivity.this, R.string.login_ok, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void showAlertDialog(int title, int text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SubmitCheatActivity.this);
        builder.setMessage(text).setTitle(title);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.create();
        builder.show();
    }

    @Override
    public void onClick(View v) {

        if (v == sendButton) {

            if ((member != null) && (member.getMid() != 0)) {
                if ((cheatText.getText().toString().trim().length() < 5) || (cheatTitle.getText().toString().trim().length() < 2)) {
                    showAlertDialog(R.string.err, R.string.fill_everything);
                } else if (!checkBoxTerms.isChecked()) {
                    showAlertDialog(R.string.err, R.string.submit_cheat_error_accept_conditions);
                } else {

                    if (Reachability.reachability.isReachable) {

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (Webservice.hasMemberPermissions(member)) {

                                    Webservice.insertCheat(member.getMid(), gameObj.getGameId(), cheatTitle.getText().toString().trim(), cheatText.getText().toString().trim());
                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            try {
                                                cheatTitle.setText("");
                                                cheatText.setText("");

                                                showAlertDialog(R.string.thanks, R.string.cheat_submit_ok);
                                            } catch (Exception e) {
                                                showAlertDialog(R.string.err, R.string.cheat_submit_nok);
                                            }
                                        }
                                    });

                                } else {
                                    showAlertDialog(R.string.err, R.string.member_banned);
                                }
                            }
                        }).start();
                    } else {
                        Toast.makeText(SubmitCheatActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                login();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.submitcheat_menu, menu);

        if (member != null) {
            getMenuInflater().inflate(R.menu.signout_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.signin_menu, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    private void changeButton() {
        if (member == null) {
            sendButton.setText(getString(R.string.login_to_submit));
            // sendButton.setLeftIcon("fa-sign-in");
        } else {
            sendButton.setText(getString(R.string.submit_cheat_review));
            // sendButton.setLeftIcon("fa-check");
        }
    }

    private void login() {
        Intent loginIntent = new Intent(SubmitCheatActivity.this, LoginActivity.class);
        startActivityForResult(loginIntent, Konstanten.LOGIN_REGISTER_OK_RETURN_CODE);
    }

}