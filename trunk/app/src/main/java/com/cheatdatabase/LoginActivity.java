package com.cheatdatabase;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.dialogs.AlreadyLoggedInDialog;
import com.cheatdatabase.dialogs.AlreadyLoggedInDialog.AlreadyLoggedInDialogListener;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.google.gson.Gson;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */

@EActivity(R.layout.activity_login)
public class LoginActivity extends AppCompatActivity implements AlreadyLoggedInDialogListener {

    public static final String TAG = LoginActivity.class.getSimpleName();

    /**
     * The default email to populate the email field with.
     */
    public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
//    private UserLoginTask mAuthTask = null;

    // Values for email and password at the time of the login attempt.
    private String mEmail;
    private String mPassword;

    @ViewById(R.id.email)
    EditText mEmailView;

    @ViewById(R.id.password)
    EditText mPasswordView;

    @ViewById(R.id.login_form)
    View mLoginFormView;

    @ViewById(R.id.send_status)
    View mLoginStatusView;

    @ViewById(R.id.send_status_message)
    TextView mLoginStatusMessageView;

    @ViewById(R.id.txt_send_login)
    TextView mForgotPassword;

    @ViewById(R.id.login_button)
    Button loginButton;

    @ViewById(R.id.cancel_button)
    Button cancelButton;

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    @Bean
    Tools tools;

    private Member member;
    private SharedPreferences settings;
    private Editor editor;

    private Typeface latoFontBold;
    private Typeface latoFontLight;


    @AfterViews
    protected void onCreateView() {
        init();

        // Set up the login form.
        mEmail = getIntent().getStringExtra(EXTRA_EMAIL);

        mEmailView.setText(mEmail);
        if ((member != null) && member.getEmail() != null) {
            mEmailView.setText(member.getEmail());
        }

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    if (Reachability.reachability.isReachable) {
                        attemptLogin();
                    } else {
                        Toast.makeText(LoginActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
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
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();

        member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);

        latoFontBold = tools.getFont(getAssets(), Konstanten.FONT_BOLD);
        latoFontLight = tools.getFont(getAssets(), Konstanten.FONT_LIGHT);

        mEmailView.setTypeface(latoFontLight);
        mPasswordView.setTypeface(latoFontLight);
        mLoginStatusMessageView.setTypeface(latoFontLight);
        loginButton.setTypeface(latoFontBold);
        cancelButton.setTypeface(latoFontBold);
        mForgotPassword.setTypeface(latoFontLight);
    }

    @Click(R.id.txt_send_login)
    void forgotPasswordClicked() {
        Intent recoverIntent = new Intent(LoginActivity.this, RecoverActivity_.class);
        startActivityForResult(recoverIntent, Konstanten.RECOVER_PASSWORD_ATTEMPT);
    }

    @Click(R.id.login_button)
    void loginButtonClicked() {
        if (Reachability.reachability.isReachable) {
            attemptLogin();
        } else {
            Toast.makeText(LoginActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    @Click(R.id.cancel_button)
    void cancelButtonClicked() {
        finish();
    }

    @Override
    public void onPause() {
        Reachability.unregister(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Reachability.registerReachability(this);
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
                Intent recoverIntent = new Intent(LoginActivity.this, RecoverActivity_.class);
                startActivityForResult(recoverIntent, Konstanten.RECOVER_PASSWORD_ATTEMPT);
                return true;
            case R.id.action_logout:
                member = null;
                tools.logout(LoginActivity.this, settings.edit());
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
        mPassword = mPasswordView.getText().toString();

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

            loginBackgroundTask(mEmailView.getText().toString().trim(), mPasswordView.getText().toString().trim());
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
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
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Background
    void loginBackgroundTask(String email, String password) {

        boolean loginResult = false;

        member = Webservice.login(email, password);
        if (member != null) {
            if (member.getErrorCode() == 0) {
                member.writeMemberData(member, settings);
                loginResult = true;
            } else {
                loginResult = false;
            }
        } else {
            loginResult = false;
        }

        afterLogin(loginResult);
    }

    @UiThread
    public void afterLogin(boolean loginResult) {
        showProgress(false);

        if (loginResult) {
            // LOGIN_SUCCESS_RETURN_CODE = Login success
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", Konstanten.LOGIN_SUCCESS_RETURN_CODE);
            setResult(Konstanten.LOGIN_SUCCESS_RETURN_CODE, returnIntent);
            finish();
        } else {
            errorStuff();
        }
    }

    private void errorStuff() {
        if (member != null) {
            switch (member.getErrorCode()) {
                case 1:
                    mEmailView.setError(getString(R.string.err_email_invalid));
                    mEmailView.requestFocus();
                    break;
                case 2:
                    mEmailView.setError(getString(R.string.err_email_used));
                    mEmailView.requestFocus();
                    break;
                case 3:
                    mEmailView.setError(getString(R.string.err_username_used));
                    mEmailView.requestFocus();
                    break;
                default:
                    Toast.makeText(LoginActivity.this, R.string.err_other_problem, Toast.LENGTH_LONG).show();
            }
        } else {
            mPasswordView.setError(getString(R.string.error_incorrect_password));
            mPasswordView.requestFocus();
        }

    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        Log.d("requestCode", requestCode + "");
//        Log.d("resultCode", resultCode + "");
//
//        // User registered an account. Therefore the login activity has to be
//        // closed and the user returns to the previous screen.
//        if (resultCode == RESULT_OK) {
//            int intentReturnCode = data.getIntExtra("result", Konstanten.LOGIN_REGISTER_FAIL_RETURN_CODE);
//
//            if (intentReturnCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
//                Intent returnIntent = new Intent();
//                returnIntent.putExtra("result", Konstanten.REGISTER_SUCCESS_RETURN_CODE);
//                setResult(RESULT_OK, returnIntent);
//                finish();
//            } else if (intentReturnCode == Konstanten.RECOVER_PASSWORD_SUCCESS_RETURN_CODE) {
//                Toast.makeText(LoginActivity.this, R.string.recover_login_success, Toast.LENGTH_LONG).show();
//            }
//        }
//    }

    @OnActivityResult(Konstanten.LOGIN_SUCCESS_RETURN_CODE)
    void onResult(Intent data) {
        Log.d(TAG, "ONRESULT AAAAAAAAAA");
        int intentReturnCode = data.getIntExtra("result", Konstanten.LOGIN_REGISTER_FAIL_RETURN_CODE);

        if (intentReturnCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", Konstanten.REGISTER_SUCCESS_RETURN_CODE);
            setResult(RESULT_OK, returnIntent);
            finish();
        } else if (intentReturnCode == Konstanten.RECOVER_PASSWORD_SUCCESS_RETURN_CODE) {
            Toast.makeText(LoginActivity.this, R.string.recover_login_success, Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onFinishDialog(boolean signOutNow) {
        if (signOutNow) {
            editor.remove(Konstanten.MEMBER_OBJECT);
            editor.commit();

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
