package com.cheatdatabase;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
@EActivity(R.layout.activity_register)
public class RegisterActivity extends AppCompatActivity {

    /**
     * The default email to populate the email field with.
     */
    public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

    // Values for email and password at the time of the login attempt.
    private String mEmail;
    private String mUsername;

    @BindView(R.id.email)
    EditText mEmailView;

    @BindView(R.id.username)
    EditText mUsernameView;

    @BindView(R.id.login_form)
    View mLoginFormView;

    @BindView(R.id.send_status)
    View mLoginStatusView;

    @BindView(R.id.send_status_message)
    TextView mLoginStatusMessageView;

    @BindView(R.id.register_button)
    Button registerButton;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Bean
    Tools tools;

    private Member member;
    private SharedPreferences settings;
    private Editor editor;

    @AfterViews
    void onCreate() {
        init();

        // Set up the login form.
        mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
        mEmailView.setText(mEmail);

        mUsernameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    if (Reachability.reachability.isReachable) {
                        attemptRegister();
                        return true;
                    } else {
                        Toast.makeText(RegisterActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                    }

                }
                return false;
            }
        });
        mUsernameView.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String fixedUsername = mUsernameView.getText().toString();
                    fixedUsername = fixedUsername.replaceAll("\\s", "");
                    mUsernameView.setText(fixedUsername);
                }
            }

        });


    }

    @Click(R.id.register_button)
    void registerButtonClicked() {
        if (Reachability.reachability.isReachable) {
            attemptRegister();
        } else {
            Toast.makeText(RegisterActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    private void init() {

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Typeface latoFontBold = tools.getFont(getAssets(), Konstanten.FONT_BOLD);
        Typeface latoFontLight = tools.getFont(getAssets(), Konstanten.FONT_LIGHT);

        mEmailView.setTypeface(latoFontLight);
        mUsernameView.setTypeface(latoFontLight);
        mLoginStatusMessageView.setTypeface(latoFontLight);
        registerButton.setTypeface(latoFontBold);

        settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        editor = settings.edit();
    }

    @Override
    public void onPause() {
        Reachability.unregister(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(this);
        }
        super.onResume();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptRegister() {
        // Reset errors.
        mEmailView.setError(null);
        mUsernameView.setError(null);

        // Store values at the time of the login attempt.
        mEmail = mEmailView.getText().toString();
        mUsername = mUsernameView.getText().toString();
        mUsername = mUsername.replaceAll("\\s", "");

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mUsername)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (mUsername.length() < 4) {
            mUsernameView.setError(getString(R.string.err_username_too_short));
            focusView = mUsernameView;
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
            // Delete locally saved member object
            editor.remove(Konstanten.MEMBER_OBJECT);
            editor.commit();

            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mLoginStatusMessageView.setText(R.string.register_progress_registering);
            showProgress(true);

            registerTask(mUsernameView.getText().toString().trim(), mEmailView.getText().toString().trim());
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

    /**
     * Represents an asynchronous registration task used to authenticate the
     * user.
     */
    @Background
    void registerTask(String username, String email) {
        boolean success = false;
        member = Webservice.register(username, email);
        if (member.getErrorCode() == 0) {
            member.writeMemberData(member, settings);

            success = true;
        } else {
            success = false;
        }

        registerTaskFinished(success);
    }

    @UiThread
    void registerTaskFinished(boolean success) {
        showProgress(false);

        if (success) {
            // REGISTER_SUCCESS_RETURN_CODE = Register success
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", Konstanten.REGISTER_SUCCESS_RETURN_CODE);
            setResult(RESULT_OK, returnIntent);
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
                    mUsernameView.setError(getString(R.string.err_username_used));
                    mUsernameView.requestFocus();
                    break;
                default:
                    Toast.makeText(RegisterActivity.this, R.string.err_other_problem, Toast.LENGTH_LONG).show();
            }
        }
    }

}
