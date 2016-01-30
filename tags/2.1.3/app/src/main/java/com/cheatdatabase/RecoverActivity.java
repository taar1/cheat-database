package com.cheatdatabase;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
@EActivity(R.layout.activity_recover)
public class RecoverActivity extends AppCompatActivity {

    /**
     * The default email to populate the email field with.
     */
    public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

    // UI references.
    @ViewById(R.id.email)
    EditText mEmailView;

    @ViewById(R.id.login_form)
    View mLoginFormView;

    @ViewById(R.id.send_status)
    View mLoginStatusView;

    @ViewById(R.id.send_status_message)
    TextView mLoginStatusMessageView;

    @ViewById(R.id.recover_return_message)
    TextView mResponseMessageView;

    @ViewById(R.id.recover_button)
    Button recoverButton;

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    @Bean
    Tools tools;

    private String mEmail;

    private int successMessage;

    @AfterViews
    public void onCreate() {
        init();

        mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
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
    }

    @Click(R.id.recover_button)
    void recoverButtonClicked() {
        if (Reachability.reachability.isReachable) {
            attemptRecover();
        } else {
            Toast.makeText(RecoverActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
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

        mLoginStatusMessageView.setTypeface(latoFontLight);
        mResponseMessageView.setTypeface(latoFontBold);
        recoverButton.setTypeface(latoFontBold);
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


    public void attemptRecover() {
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
            recoverTask(mEmailView.getText().toString().trim());
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
    void recoverTask(String email) {
        successMessage = Webservice.sendLoginData(email);

        boolean success = successMessage != R.string.err_email_invalid;
        afterRecover(success, successMessage);
    }

    @UiThread
    void afterRecover(boolean success, int successMessage) {
        showProgress(false);

        if (success) {
            mResponseMessageView.setText(getString(successMessage));
            mResponseMessageView.setVisibility(View.VISIBLE);

            if (successMessage != R.string.err_email_user_not_found) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", Konstanten.RECOVER_PASSWORD_SUCCESS_RETURN_CODE);
                setResult(RESULT_OK, returnIntent);
                finish();
            }

        } else {
            mEmailView.setError(getString(successMessage));
            mEmailView.requestFocus();
        }
    }

}
