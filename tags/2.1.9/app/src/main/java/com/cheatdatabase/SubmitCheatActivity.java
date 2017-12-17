package com.cheatdatabase;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.gson.Gson;
import com.splunk.mint.Mint;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

/**
 * Liste mit allen Cheats eines Games.
 * http://developer.android.com/guide/tutorials/views/hello-tablelayout.html
 *
 * @author erbsland
 */
@EActivity(R.layout.activity_submit_cheat_layout)
public class SubmitCheatActivity extends AppCompatActivity {

    @ViewById(R.id.text_cheat_submission_title)
    TextView textCheatTitle;

    @ViewById(R.id.edit_cheat_title)
    EditText cheatTitle;

    @ViewById(R.id.edit_cheat_text)
    EditText cheatText;

    @ViewById(R.id.checkbox_terms)
    CheckBox checkBoxTerms;

    @ViewById(R.id.send_button)
    Button sendButton;

    @Extra
    Game gameObj;

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    @Bean
    Tools tools;

    Member member;

    private SharedPreferences settings;
    private Typeface latoFontBold;
    private Typeface latoFontLight;

    @AfterViews
    public void onCreate() {
        //handleIntent(getIntent());
        init();

        changeSubmitButtonText();
    }

    @Click(R.id.send_button)
    void sendButtonClick() {
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


    private void init() {
        //CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "Submit Cheat Form").setLabel("activity").build());
        Mint.initAndStartSession(this, Konstanten.SPLUNK_MINT_API_KEY);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(gameObj.getGameName());
        getSupportActionBar().setSubtitle(gameObj.getSystemName());

        settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);

        latoFontLight = tools.getFont(getAssets(), Konstanten.FONT_LIGHT);
        latoFontBold = tools.getFont(getAssets(), Konstanten.FONT_BOLD);

        textCheatTitle.setTypeface(latoFontBold);
        cheatTitle.setTypeface(latoFontLight);
        cheatText.setTypeface(latoFontLight);
        checkBoxTerms.setTypeface(latoFontLight);
        sendButton.setTypeface(latoFontBold);
    }

    @Override
    public void onPause() {
        super.onPause();
        Reachability.unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(this);
        }
        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
                tools.logout(SubmitCheatActivity.this, settings.edit());
                changeSubmitButtonText();
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.submitcheat_menu, menu);

        if (member != null) {
            getMenuInflater().inflate(R.menu.signout_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.signin_menu, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    private void changeSubmitButtonText() {
        if (member == null) {
            sendButton.setText(getString(R.string.login_to_submit));
        } else {
            sendButton.setText(getString(R.string.submit_cheat_review));
        }
    }

    private void login() {
        Intent loginIntent = new Intent(SubmitCheatActivity.this, LoginActivity_.class);
        startActivityForResult(loginIntent, Konstanten.LOGIN_REGISTER_OK_RETURN_CODE);
    }

    @OnActivityResult(Konstanten.LOGIN_SUCCESS_RETURN_CODE)
    void onResult(int resultCode, Intent data_login) {
        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
        changeSubmitButtonText();

        invalidateOptionsMenu();
        if (member != null) {
            Toast.makeText(SubmitCheatActivity.this, R.string.login_ok, Toast.LENGTH_LONG).show();
        }
    }

    @OnActivityResult(Konstanten.REGISTER_SUCCESS_RETURN_CODE)
    void onResult(int resultCode) {
        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
        changeSubmitButtonText();

        invalidateOptionsMenu();
        if (member != null) {
            Toast.makeText(SubmitCheatActivity.this, R.string.register_thanks, Toast.LENGTH_LONG).show();
        }
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == RESULT_OK) {
//            // Return result code. Login success, Register success etc.
//            int intentReturnCode = data.getIntExtra("result", Konstanten.LOGIN_REGISTER_FAIL_RETURN_CODE);
//
//            if (requestCode == Konstanten.LOGIN_REGISTER_OK_RETURN_CODE) {
//                member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
//                changeSubmitButtonText();
//
//                invalidateOptionsMenu();
//                if ((member != null) && intentReturnCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
//                    Toast.makeText(SubmitCheatActivity.this, R.string.register_thanks, Toast.LENGTH_LONG).show();
//                } else if ((member != null) && intentReturnCode == Konstanten.LOGIN_SUCCESS_RETURN_CODE) {
//                    Toast.makeText(SubmitCheatActivity.this, R.string.login_ok, Toast.LENGTH_LONG).show();
//                }
//            }
//        }
//    }

}