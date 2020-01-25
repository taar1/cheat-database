package com.cheatdatabase.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cheatdatabase.R;
import com.cheatdatabase.model.Member;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Webservice;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import needle.Needle;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class RegisterActivity extends AppCompatActivity {

    /**
     * The default email to populate the email field with.
     */
    public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

    // Values for email and password at the time of the login attempt.
    private String mEmail;

    @BindView(R.id.email)
    EditText mEmailView;
    @BindView(R.id.username)
    EditText mUsernameView;
    @BindView(R.id.login_form)
    LinearLayout mLoginFormView;
    @BindView(R.id.send_status)
    View mLoginStatusView;
    @BindView(R.id.send_status_message)
    TextView mLoginStatusMessageView;
    @BindView(R.id.register_button)
    Button registerButton;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private Member member;
    private SharedPreferences settings;
    private Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        init();

        // Set up the login form.
        mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
        mEmailView.setText(mEmail);

        mUsernameView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == 222 || id == EditorInfo.IME_NULL) {
                if (Reachability.reachability.isReachable) {
                    attemptRegister();
                    return true;
                } else {
                    Toast.makeText(RegisterActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }

            }
            return false;
        });
        mUsernameView.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String fixedUsername = mUsernameView.getText().toString();
                fixedUsername = fixedUsername.replaceAll("\\s", "");
                mUsernameView.setText(fixedUsername);
            }
        });

    }

    @OnClick(R.id.register_button)
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
        String mUsername = mUsernameView.getText().toString();
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

    /**
     * Represents an asynchronous registration task used to authenticate the
     * user.
     */
    void registerTask(String username, String email) {
        Needle.onBackgroundThread().execute(() -> {
            boolean success;
            member = Webservice.register(username, email);

            if (member.getErrorCode() == 0) {
                member.writeMemberData(member, settings);

                success = true;
            } else {
                success = false;
            }

            registerTaskFinished(success);
        });


    }

    void registerTaskFinished(boolean success) {
        Needle.onMainThread().execute(() -> {
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
        });

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