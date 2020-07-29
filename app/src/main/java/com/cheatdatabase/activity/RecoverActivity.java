package com.cheatdatabase.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cheatdatabase.R;
import com.cheatdatabase.helpers.AeSimpleMD5;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.rest.RestApi;
import com.google.gson.JsonObject;

import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class RecoverActivity extends AppCompatActivity {
    private static final String TAG = "RecoverActivity";
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

    @Inject
    RestApi restApi;

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
     * @param show or not show
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
        try {
            String email_md5 = AeSimpleMD5.MD5(email.trim());

            Call<JsonObject> call = restApi.sendLoginData(email_md5);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> forum, Response<JsonObject> response) {
                    JsonObject registerResponse = response.body();

                    // email_sent, no_user_found, invalid_email
                    String returnValue = registerResponse.get("returnValue").getAsString();

                    if (returnValue.equalsIgnoreCase("email_sent")) {
                        updateUIAfterRecovery(true, R.string.login_sent_ok);
                    } else if (returnValue.equalsIgnoreCase("no_user_found")) {
                        updateUIAfterRecovery(false, R.string.err_email_user_not_found);
                    } else if (returnValue.equalsIgnoreCase("invalid_email")) {
                        updateUIAfterRecovery(false, R.string.error_invalid_email);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable e) {
                    updateUIAfterRecovery(false, 99);
                }
            });

        } catch (NoSuchAlgorithmException e) {
            updateUIAfterRecovery(false, 99);
            Log.e(TAG, "sendRecoveryEmail: ", e);
        }
    }

    void updateUIAfterRecovery(boolean success, int successMessage) {
        showProgress(false);

        if (success) {
            Toast.makeText(this, getString(R.string.login_sent_ok), Toast.LENGTH_LONG).show();
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
