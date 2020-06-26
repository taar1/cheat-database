package com.cheatdatabase.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.cheatdatabase.R;
import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.dialogs.AlreadyLoggedInDialog;
import com.cheatdatabase.dialogs.AlreadyLoggedInDialog.AlreadyLoggedInDialogListener;
import com.cheatdatabase.helpers.AeSimpleMD5;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.rest.RestApi;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import needle.Needle;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends AppCompatActivity implements AlreadyLoggedInDialogListener {

    private static final String TAG = "LoginActivity";
    public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

    // Values for email and password at the time of the login attempt.
    private String mEmail;

    @BindView(R.id.email)
    EditText mEmailView;
    @BindView(R.id.password)
    EditText mPasswordView;
    @BindView(R.id.login_form)
    LinearLayout mLoginFormView;
    @BindView(R.id.send_status)
    View mLoginStatusView;
    @BindView(R.id.send_status_message)
    TextView mLoginStatusMessageView;
    @BindView(R.id.txt_send_login)
    TextView mForgotPassword;
    @BindView(R.id.login_button)
    Button loginButton;
    @BindView(R.id.cancel_button)
    Button cancelButton;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private Member member;
    private SharedPreferences settings;
    private Editor editor;

    @Inject
    Retrofit retrofit;

    private RestApi restApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        init();

        // Set up the login form.
        mEmail = getIntent().getStringExtra(EXTRA_EMAIL);

        mEmailView.setText(mEmail);
        if ((member != null) && member.getEmail() != null) {
            mEmailView.setText(member.getEmail());
        }

        mPasswordView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == 111 || id == EditorInfo.IME_NULL) {
                if (Reachability.reachability.isReachable) {
                    attemptLogin();
                } else {
                    Toast.makeText(LoginActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });


        // If already logged in, show a popup.
        if ((member != null) && (member.getEmail().length() > 0)) {
            mEmailView.setEnabled(false);
            mPasswordView.setEnabled(false);
            loginButton.setEnabled(false);
            FragmentManager fm = getSupportFragmentManager();
            AlreadyLoggedInDialog alreadyLoggedInDialog = new AlreadyLoggedInDialog();
            alreadyLoggedInDialog.show(fm, "already_logged_in");
        } else {
            mEmailView.setEnabled(true);
            mPasswordView.setEnabled(true);
            loginButton.setEnabled(true);
        }
    }

    private void init() {
        //((CheatDatabaseApplication) getApplication()).getNetworkComponent().inject(this);
        restApi = retrofit.create(RestApi.class);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();

        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
    }

    @OnClick(R.id.txt_send_login)
    void forgotPasswordClicked() {
        Intent recoverIntent = new Intent(LoginActivity.this, RecoverActivity.class);
        startActivityForResult(recoverIntent, Konstanten.RECOVER_PASSWORD_ATTEMPT);
    }

    @OnClick(R.id.login_button)
    void loginButtonClicked() {
        if (Reachability.reachability.isReachable) {
            attemptLogin();
        } else {
            Toast.makeText(LoginActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.cancel_button)
    void cancelButtonClicked() {
        finish();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.register_menu, menu);
        if (member != null) {
            getMenuInflater().inflate(R.menu.signout_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar buttons
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_register:
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(registerIntent, Konstanten.REGISTER_ATTEMPT);
                return true;
            case R.id.action_forgot_password:
                Intent recoverIntent = new Intent(LoginActivity.this, RecoverActivity.class);
                startActivityForResult(recoverIntent, Konstanten.RECOVER_PASSWORD_ATTEMPT);
                return true;
            case R.id.action_logout:
                member = null;
                Tools.logout(LoginActivity.this, settings.edit());
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mEmail = mEmailView.getText().toString();
        String mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (mPassword.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mEmail)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!mEmail.contains("@")) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);

            loginTask(mEmailView.getText().toString().trim(), mPasswordView.getText().toString().trim());
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginStatusView.setVisibility(View.VISIBLE);
        mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });

        mLoginFormView.setVisibility(View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });
    }

    void loginTask(String username, String password) {
        String password_md5 = null;
        try {
            password_md5 = AeSimpleMD5.MD5(password.trim());
            //Log.d(TAG, "loginTask password_md5: " + password_md5);

            Call<JsonObject> call = restApi.login(username, password_md5);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> forum, Response<JsonObject> response) {
                    JsonObject registerResponse = response.body();

                    // login_ok, wrong_pw, member_not_found
                    String returnValue = registerResponse.get("returnValue").getAsString();

                    if (returnValue.equalsIgnoreCase("login_ok")) {
                        member = new Member();
                        member.setMid(registerResponse.get("memberId").getAsInt());
                        member.setUsername(registerResponse.get("username").getAsString());
                        member.setEmail(registerResponse.get("email").getAsString());
                        member.setPassword(password);
                        member.writeMemberData(member, settings);

                        afterLogin(true, 0);
                    } else if (returnValue.equalsIgnoreCase("wrong_pw")) {
                        afterLogin(false, 1);
                    } else if (returnValue.equalsIgnoreCase("member_not_found")) {
                        afterLogin(false, 2);
                    } else if (returnValue.equalsIgnoreCase("member_banned")) {
                        afterLogin(false, 3);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable e) {
                    afterLogin(false, 99);
                }
            });
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "loginTask: NoSuchAlgorithmException", e);
            afterLogin(false, 99);
        }
    }

    private void afterLogin(boolean loginResult, int errorCode) {
        Needle.onMainThread().execute(() -> {
            showProgress(false);

            // LOGIN_SUCCESS_RETURN_CODE = Login success
            if (loginResult) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", Konstanten.LOGIN_SUCCESS_RETURN_CODE);
                setResult(Konstanten.LOGIN_SUCCESS_RETURN_CODE, returnIntent);
                finish();
            } else {
                errorStuff(errorCode);
            }
        });
    }

    private void errorStuff(int errorCode) {
        switch (errorCode) {
            case 1: // wrong_pw
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
                break;
            case 2: // member_not_found
                mEmailView.setError(getString(R.string.err_no_member_data));
                mEmailView.requestFocus();
                break;
            case 3: // member_banned
                mEmailView.setError(getString(R.string.member_banned));
                mEmailView.requestFocus();
                break;
            default:
                Toast.makeText(LoginActivity.this, R.string.err_occurred, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            int intentReturnCode = data.getIntExtra("result", Konstanten.LOGIN_REGISTER_FAIL_RETURN_CODE);

            if (intentReturnCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", Konstanten.REGISTER_SUCCESS_RETURN_CODE);
                setResult(RESULT_OK, returnIntent);

                Toast.makeText(LoginActivity.this, R.string.register_thanks, Toast.LENGTH_LONG).show();
                finish();
            } else if (intentReturnCode == Konstanten.RECOVER_PASSWORD_SUCCESS_RETURN_CODE) {
                Toast.makeText(LoginActivity.this, R.string.recover_login_success, Toast.LENGTH_LONG).show();
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "onActivityResult data.getIntExtra is NULL: " + e.getLocalizedMessage());
            Crashlytics.logException(e);
            finish();
        }
    }

    @Override
    public void onFinishDialog(boolean signOutNow) {
        if (signOutNow) {
            editor.remove(Konstanten.MEMBER_OBJECT);
            editor.apply();

            mEmailView.setText("");
            mEmailView.setEnabled(true);
            mPasswordView.setText("");
            mPasswordView.setEnabled(true);
            loginButton.setEnabled(true);
            Toast.makeText(LoginActivity.this, R.string.logout_ok, Toast.LENGTH_LONG).show();
        } else {
            finish();
        }

    }
}
