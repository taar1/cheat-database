package com.cheatdatabase.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.cheatdatabase.CheatDatabaseApplication;
import com.cheatdatabase.R;
import com.cheatdatabase.data.model.Game;
import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.dialogs.PlainInformationDialog;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.rest.RestApi;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Form to submit a cheat for a game.
 *
 * @author erbsland
 */
public class SubmitCheatFormActivity extends AppCompatActivity {
    private static final String TAG = "SubmitCheatActivity";

    @BindView(R.id.outer_layout)
    ConstraintLayout outerLayout;
    @BindView(R.id.text_cheat_submission_title)
    TextView textCheatTitle;
    @BindView(R.id.edit_cheat_title)
    TextInputEditText cheatTitle;
    @BindView(R.id.edit_cheat_text)
    TextInputEditText cheatText;
    @BindView(R.id.checkbox_terms)
    CheckBox checkBoxTerms;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private Game gameObj;
    private Member member;

    private SharedPreferences settings;

    @Inject
    Retrofit retrofit;

    private RestApi apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_cheat_layout);
        ButterKnife.bind(this);

        // Dagger start
        ((CheatDatabaseApplication) getApplication()).getNetworkComponent().inject(this);
        apiService = retrofit.create(RestApi.class);
        // Dagger end

        gameObj = getIntent().getParcelableExtra("gameObj");
        toolbar.setTitle(gameObj.getGameName());
        toolbar.setSubtitle(gameObj.getSystemName());

        if (gameObj == null) {
            Toast.makeText(SubmitCheatFormActivity.this, R.string.err_somethings_wrong, Toast.LENGTH_LONG).show();
            finish();
        } else {
            init();
        }
    }

    private void init() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
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
                Tools.logout(SubmitCheatFormActivity.this, settings.edit());
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
                    checkMemberPermissions();
                } else {
                    Tools.showSnackbar(outerLayout, getString(R.string.no_internet));
                }
            }
        } else {
            login();
        }
    }

    private void checkMemberPermissions() {
        Log.d(TAG, "checkMemberPermissions: 1");
        Call<JsonObject> call = apiService.getMemberPermissions(member.getMid());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> games, Response<JsonObject> response) {
                Log.d(TAG, "checkMemberPermissions: 2");
                if (response.isSuccessful()) {
                    JsonObject permissions = response.body();

                    boolean banned = permissions.get("banned").getAsBoolean();
                    String username = permissions.get("username").getAsString(); // Ignore for now
                    int memberId = permissions.get("memberId").getAsInt(); // Ignore for now
                    boolean enabled = permissions.get("enabled").getAsBoolean(); // Ignore for now

                    if (!banned) {
                        submitCheatNow();
                    } else {
                        showAlertDialog(R.string.err, R.string.member_banned);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "checkMemberPermissions: 3");
                Log.e(TAG, "Get user permissions failed: " + t.getLocalizedMessage());
                Tools.showSnackbar(outerLayout, getString(R.string.no_internet));
            }
        });
    }

    private void submitCheatNow() {
        Log.d(TAG, "submitCheatNow: 1");
        Call<JsonObject> call = apiService.insertCheat(member.getMid(), gameObj.getGameId(), cheatTitle.getText().toString().trim(), cheatText.getText().toString().trim());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> games, Response<JsonObject> response) {
                Log.d(TAG, "submitCheatNow: 2");
                if (response.isSuccessful()) {
                    JsonObject submissionResponse = response.body();

                    String returnMessage = submissionResponse.get("returnMessage").getAsString();
                    if (returnMessage.equalsIgnoreCase("insert_ok")) {
                        cheatTitle.setText("");
                        cheatText.setText("");

                        showAlertDialog(R.string.thanks, R.string.cheat_submit_ok);
                    } else if (returnMessage.equalsIgnoreCase("missing_values")) {
                        showAlertDialog(R.string.err, R.string.cheat_submit_nok);
                    } else if (returnMessage.equalsIgnoreCase("invalid_member_id")) {
                        showAlertDialog(R.string.err, R.string.cheat_submit_nok);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "submitCheatNow: 3");
                Log.e(TAG, "Submitting the cheat has failed: " + t.getLocalizedMessage());
                Tools.showSnackbar(outerLayout, getString(R.string.no_internet));
            }
        });
    }

    private void showAlertDialog(int title, int text) {
        new MaterialDialog.Builder(SubmitCheatFormActivity.this)
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
        Intent loginIntent = new Intent(SubmitCheatFormActivity.this, LoginActivity.class);
        startActivityForResult(loginIntent, Konstanten.LOGIN_REGISTER_OK_RETURN_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);

        if (resultCode == Konstanten.LOGIN_SUCCESS_RETURN_CODE) {
            if (member != null) {
                Toast.makeText(SubmitCheatFormActivity.this, R.string.login_ok, Toast.LENGTH_LONG).show();
            }
        } else if (resultCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
            if (member != null) {
                Toast.makeText(SubmitCheatFormActivity.this, R.string.register_thanks, Toast.LENGTH_LONG).show();
            }
        }

        invalidateOptionsMenu();
    }

}