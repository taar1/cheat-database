package com.cheatdatabase.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cheatdatabase.R;
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
public class RecoverActivity extends AppCompatActivity {

    /**
     * The default email to populate the email field with.
     */
    public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

    @BindView(R.id.email)
    EditText mEmailView;
    @BindView(R.id.login_form)
    View mLoginFormView;
    @BindView(R.id.send_status)
    View mLoginStatusView;
    @BindView(R.id.send_status_message)
    TextView mLoginStatusMessageView;
    @BindView(R.id.recover_return_message)
    TextView mResponseMessageView;
    @BindView(R.id.recover_button)
    Button recoverButton;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private String mEmail;

    private int successMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover);
        ButterKnife.bind(this);

        init();

        mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
        mEmailView.setText(mEmail);

        mEmailView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == 333 || id == EditorInfo.IME_NULL) {
                if (Reachability.reachability.isReachable) {
                    attemptRecover();
                    return true;
                } else {
                    Toast.makeText(RecoverActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }

            }
            return false;
        });
    }

    @OnClick(R.id.recover_button)
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
    }

    @Override
    public void onPause() {
        Reachability.unregister(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(this);
        }
    }

    private void attemptRecover() {
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
            sendRecoveryEmail(mEmailView.getText().toString().trim());
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     *
     * @param show
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

    void sendRecoveryEmail(String email) {
        Needle.onBackgroundThread().execute(() -> {
            successMessage = Webservice.sendLoginData(email);

            boolean success = successMessage != R.string.err_email_invalid;
            updateUIAfterRecovery(success, successMessage);
        });
    }

    void updateUIAfterRecovery(boolean success, int successMessage) {
        Needle.onMainThread().execute(() -> {
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
        });


    }

}