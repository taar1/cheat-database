package com.cheatdatabase.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.cheatdatabase.R;
import com.cheatdatabase.activity.MainActivity;
import com.cheatdatabase.data.RetrofitClientInstance;
import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.events.GenericEvent;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.rest.RestApi;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ContactFormFragment extends Fragment {

    private static final String TAG = "ContactFormFragment";

    @Inject
    Tools tools;

    @BindView(R.id.outer_layout)
    LinearLayout outerLayout;
    @BindView(R.id.email)
    TextInputEditText mEmailView;
    @BindView(R.id.form_message)
    TextInputEditText mMessageView;
    @BindView(R.id.emailaddress)
    TextView mEmailaddressView;
    @BindView(R.id.send_status_message)
    TextView mLoginStatusMessageView;
    @BindView(R.id.thankyou_text)
    TextView mThankyouText;
    @BindView(R.id.contact_form)
    RelativeLayout mContactFormView;
    @BindView(R.id.send_status)
    View mContactStatusView;
    @BindView(R.id.thank_you)
    View mThankyouView;

    private MainActivity mainActivity;

    /**
     * The default email to populate the email field with.
     */
    private static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

    // Values for email and password at the time of the login attempt.
    private String mEmail;

    public static ContactFormFragment newInstance() {
        return new ContactFormFragment();
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_form, container, false);
        ButterKnife.bind(this, view);

        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(mainActivity);
        }

        SharedPreferences settings = mainActivity.getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        Member member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);

        // Update action bar menu items?
        setHasOptionsMenu(true);

        // Set up the login form.
        mEmail = mainActivity.getIntent().getStringExtra(EXTRA_EMAIL);

        mEmailView.setText(mEmail);
        if ((member != null) && member.getEmail() != null) {
            mEmailView.setText(member.getEmail());
        }

        mMessageView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == 444 || id == EditorInfo.IME_NULL) {
                attemptSendForm();
                return true;
            }
            return false;
        });
        Linkify.addLinks(mEmailaddressView, Linkify.ALL);

        tools.showKeyboard(mainActivity, outerLayout);

        return view;
    }

    @Override
    public void onPause() {
        Reachability.unregister(mainActivity);
        super.onPause();
    }

    /**
     * Attempts to send the data in the contact form. If there are form errors
     * (invalid email, missing fields, etc.), the errors are presented and no
     * actual login attempt is made.
     */
    private void attemptSendForm() {
        // Reset errors.
        mEmailView.setError(null);
        mMessageView.setError(null);

        // Store values at the time of the login attempt.
        mEmail = mEmailView.getText().toString();
        String mMessage = mMessageView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mMessage)) {
            mMessageView.setError(getString(R.string.error_field_required));
            focusView = mMessageView;
            cancel = true;
        } else if (mMessage.length() < 11) {
            mMessageView.setError(getString(R.string.error_value_too_short));
            focusView = mMessageView;
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
            // Show a progress spinner, and kick off a background task to perform the user login attempt.
            mLoginStatusMessageView.setText(R.string.sending_message_progress);
            showProgress(true, 1);
            submitContactForm();
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show, final int step) {

        if ((step == 1) || (step == 2)) {
            // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which
            // allow for very easy animations. If available, use these APIs to
            // fade-in the progress spinner.
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mContactStatusView.setVisibility(View.VISIBLE);
            mContactStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mContactStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });

            mContactFormView.setVisibility(View.VISIBLE);
            mContactFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mContactFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
        } else if (step == 3) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mContactStatusView.setVisibility(View.VISIBLE);
            mContactStatusView.animate().setDuration(shortAnimTime).alpha(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mContactStatusView.setVisibility(View.GONE);
                }
            });

            mContactFormView.setVisibility(View.GONE);

            mThankyouView.setVisibility(View.VISIBLE);
            mThankyouView.animate().setDuration(shortAnimTime).alpha(1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mThankyouView.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        selectMenu(menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        selectMenu(menu);
    }

    private void selectMenu(Menu menu) {
        menu.clear();
        mainActivity.getMenuInflater().inflate(R.menu.contactform_send_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_send) {
            attemptSendForm();
            return true;
        }
        return false;
    }

    private void submitContactForm() {
        RestApi restApi = RetrofitClientInstance.getRetrofitInstance().create(RestApi.class);
        Call<Void> call = restApi.submitContactForm(mEmailView.getText().toString().trim(), "Contact through Android App", mMessageView.getText().toString().trim());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> voidValue, Response<Void> response) {
                actionAfterSendForm();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable e) {
                Log.e(TAG, "submitContactForm onFailure: " + e.getLocalizedMessage());

                tools.showSnackbar(outerLayout, mainActivity.getString(R.string.err_submit_contactform), 5000);
            }
        });
    }

    private void actionAfterSendForm() {
        View view = mainActivity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        tools.showSnackbar(outerLayout, mainActivity.getString(R.string.contactform_thanks), 5000);

        Handler handler = new Handler();
        handler.postDelayed(() -> EventBus.getDefault().post(new GenericEvent(GenericEvent.Action.CLICK_CHEATS_DRAWER)), 1500);
    }

}