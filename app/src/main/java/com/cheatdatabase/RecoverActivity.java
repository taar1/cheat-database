package com.cheatdatabase;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class RecoverActivity extends Activity {

    /**
     * The default email to populate the email field with.
     */
    public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private RecoverTask mAuthTask = null;

    // Values for email and password at the time of the login attempt.

    // UI references.
    private EditText mEmailView;

    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;
    private TextView mResponseMessageView;
    private Button recoverButton;

    private String mEmail;

    public int successMessage;

    private Typeface latoFontBold;
    private Typeface latoFontLight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover);
        Reachability.registerReachability(this.getApplicationContext());

        Tools.styleActionbar(this);
        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);

        latoFontBold = Tools.getFont(getAssets(), "Lato-Bold.ttf");
        latoFontLight = Tools.getFont(getAssets(), "Lato-Light.ttf");

        mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
        mEmailView = (EditText) findViewById(R.id.email);
        mEmailView.setText(mEmail);

        mEmailView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    if (Reachability.reachability.isReachable) {
                        attemptRecover();
                        return true;
                    } else {
                        Toast.makeText(RecoverActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                    }

                }
                return false;
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.send_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.send_status_message);
        mLoginStatusMessageView.setTypeface(latoFontLight);
        mResponseMessageView = (TextView) findViewById(R.id.recover_return_message);
        mResponseMessageView.setTypeface(latoFontBold);

        recoverButton = (Button) findViewById(R.id.recover_button);
        recoverButton.setTypeface(latoFontBold);
        recoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Reachability.reachability.isReachable) {
                    attemptRecover();
                } else {
                    Toast.makeText(RecoverActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void attemptRecover() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);

        // Store values at the time of the login attempt.
        mEmail = mEmailView.getText().toString();

        boolean cancel = false;
        View focusView = null;

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
            mLoginStatusMessageView.setText(R.string.recover_login);
            showProgress(true);
            mAuthTask = new RecoverTask();
            mAuthTask.execute((Void) null);
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
     * Represents an asynchronous recovering task used to recover the login data
     * and sending it by email.
     */
    public class RecoverTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {

            successMessage = Webservice.sendLoginData(mEmailView.getText().toString().trim());
            if (successMessage == R.string.err_email_invalid) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                mResponseMessageView.setText(getString(successMessage));
                mResponseMessageView.setVisibility(View.VISIBLE);

                if (successMessage != R.string.err_email_user_not_found) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("result", Konstanten.RECOVER_PASSWORD_SUCCESS_RETURN_CODE);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                    // recoverButton.setText(R.string.ok);
                    // recoverButton.setOnClickListener(new OnClickListener() {
                    //
                    // @Override
                    // public void onClick(View v) {
                    // Intent returnIntent = new Intent();
                    // returnIntent.putExtra("result",
                    // Konstanten.RECOVER_PASSWORD_SUCCESS_RETURN_CODE);
                    // setResult(RESULT_OK, returnIntent);
                    // finish();
                    // }
                    //
                    // });
                }

            } else {
                mEmailView.setError(getString(successMessage));
                mEmailView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

}
