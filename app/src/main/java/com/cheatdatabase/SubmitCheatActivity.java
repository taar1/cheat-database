package com.cheatdatabase;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.dialogs.PlainInformationDialog;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Form to submit a cheat for a game.
 *
 * @author erbsland
 */
public class SubmitCheatActivity extends AppCompatActivity {

    @BindView(R.id.outer_layout)
    RelativeLayout outerLayout;
    @BindView(R.id.text_cheat_submission_title)
    TextView textCheatTitle;
    @BindView(R.id.edit_cheat_title)
    TextInputEditText cheatTitle;
    @BindView(R.id.edit_cheat_text)
    TextInputEditText cheatText;
    @BindView(R.id.checkbox_terms)
    CheckBox checkBoxTerms;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private Game gameObj;
    private Member member;

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_cheat_layout);
        ButterKnife.bind(this);

        gameObj = getIntent().getParcelableExtra("gameObj");
        if (gameObj == null) {
            Toast.makeText(SubmitCheatActivity.this, R.string.err_somethings_wrong, Toast.LENGTH_LONG).show();
            finish();
        } else {
            init();
            textCheatTitle.setText(gameObj.getGameName() + " (" + gameObj.getSystemName() + ")");
        }
    }

    private void init() {
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_send:
                sendButtonClicked();
                return true;
            case R.id.action_login:
                login();
                return true;
            case R.id.action_logout:
                member = null;
                Tools.logout(SubmitCheatActivity.this, settings.edit());
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.guidelines)
    void guidelinesClicked() {
        PlainInformationDialog instructionsDialog = new PlainInformationDialog(this);
        instructionsDialog.setContent(R.string.submit_cheat_instructions_title, R.string.submit_cheat_guidelines, R.string.ok);
        instructionsDialog.show();
    }

    @OnClick(R.id.terms_conditions)
    void termsAndConditionsClicked() {
        PlainInformationDialog termsDialog = new PlainInformationDialog(this);
        termsDialog.setContent(R.string.guidelines, R.string.submit_cheat_consent_text, R.string.ok);
        termsDialog.show();
    }

    void sendButtonClicked() {
        if ((member != null) && (member.getMid() != 0)) {
            if ((cheatText.getText().toString().trim().length() < 5) || (cheatTitle.getText().toString().trim().length() < 2)) {
                showAlertDialog(R.string.err, R.string.fill_everything);
            } else if (!checkBoxTerms.isChecked()) {
                showAlertDialog(R.string.err, R.string.submit_cheat_error_accept_conditions);
            } else {

                if (Reachability.reachability.isReachable) {

                    new Thread(() -> {
                        if (Webservice.hasMemberPermissions(member)) {

                            Webservice.insertCheat(member.getMid(), gameObj.getGameId(), cheatTitle.getText().toString().trim(), cheatText.getText().toString().trim());
                            runOnUiThread(() -> {
                                try {
                                    cheatTitle.setText("");
                                    cheatText.setText("");

                                    showAlertDialog(R.string.thanks, R.string.cheat_submit_ok);
                                } catch (Exception e) {
                                    showAlertDialog(R.string.err, R.string.cheat_submit_nok);
                                }
                            });

                        } else {
                            showAlertDialog(R.string.err, R.string.member_banned);
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


    private void showAlertDialog(int title, int text) {
        new MaterialDialog.Builder(SubmitCheatActivity.this)
                .title(title)
                .content(text)
                .positiveText(R.string.ok)
                .onPositive((dialog, which) -> {
                    if (title == R.string.thanks) {
                        Handler handler = new Handler();
                        handler.postDelayed(() -> {
                            finish();
                        }, 100);
                    }
                })
                .theme(Theme.DARK)
                .show();
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

    private void login() {
        Intent loginIntent = new Intent(SubmitCheatActivity.this, LoginActivity.class);
        startActivityForResult(loginIntent, Konstanten.LOGIN_REGISTER_OK_RETURN_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);

        if (resultCode == Konstanten.LOGIN_SUCCESS_RETURN_CODE) {
            if (member != null) {
                Toast.makeText(SubmitCheatActivity.this, R.string.login_ok, Toast.LENGTH_LONG).show();
            }
        } else if (resultCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
            if (member != null) {
                Toast.makeText(SubmitCheatActivity.this, R.string.register_thanks, Toast.LENGTH_LONG).show();
            }
        }

        invalidateOptionsMenu();
    }

}