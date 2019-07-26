package com.cheatdatabase.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.textfield.TextInputEditText;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.events.GenericEvent;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import needle.Needle;

public class ContactFormFragment extends Fragment {

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
    View mContactFormView;
    @BindView(R.id.send_status)
    View mContactStatusView;
    @BindView(R.id.thank_you)
    View mThankyouView;

    /**
     * The default email to populate the email field with.
     */
    public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

    // Values for email and password at the time of the login attempt.
    private String mEmail;

    private boolean isFormSent = false;

    public static ContactFormFragment newInstance() {
        ContactFormFragment contactFormFragment = new ContactFormFragment();
        return contactFormFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_form, container, false);
        ButterKnife.bind(this, view);

        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(getActivity());
        }

        SharedPreferences settings = getActivity().getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
        Member member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);

        Typeface latoFontLight = Tools.getFont(getActivity().getAssets(), Konstanten.FONT_LIGHT);

        // Update action bar menu items?
        setHasOptionsMenu(true);

        // Set up the login form.
        mEmail = getActivity().getIntent().getStringExtra(EXTRA_EMAIL);

        mEmailView.setText(mEmail);
        if ((member != null) && member.getEmail() != null) {
            mEmailView.setText(member.getEmail());
        }

        mMessageView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptSendForm();
                    return true;
                }
                return false;
            }
        });
        Linkify.addLinks(mEmailaddressView, Linkify.ALL);

        mEmailView.setTypeface(latoFontLight);
        mMessageView.setTypeface(latoFontLight);
        mEmailaddressView.setTypeface(latoFontLight);
        mThankyouText.setTypeface(latoFontLight);
        mLoginStatusMessageView.setTypeface(latoFontLight);

        // TODO FIXME keyboard anzeigen
        // TODO FIXME keyboard anzeigen
        // TODO FIXME keyboard anzeigen
        // TODO FIXME keyboard anzeigen
//        Tools.showKeyboard(getActivity(), outerLayout);

        return view;
    }

    @Override
    public void onPause() {
        Reachability.unregister(getActivity());
        super.onPause();
    }

    /**
     * Attempts to send the data in the contact form. If there are form errors
     * (invalid email, missing fields, etc.), the errors are presented and no
     * actual login attempt is made.
     */
    public void attemptSendForm() {
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
            sendForm();
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show, final int step) {

        if ((step == 1) || (step == 2)) {
            // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which
            // allow for very easy animations. If available, use these APIs to
            // fade-in the progress spinner.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
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
            } else {
                // The ViewPropertyAnimator APIs are not available, so simply
                // show and hide the relevant UI components.
                mContactStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                mContactFormView.setVisibility(show ? View.GONE : View.VISIBLE);

                mThankyouView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        } else if (step == 3) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
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
            } else {
                mContactStatusView.setVisibility(View.GONE);
                mContactFormView.setVisibility(View.GONE);
                mThankyouView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (Build.VERSION.SDK_INT >= 11) {
            selectMenu(menu);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        selectMenu(menu);
    }

    private void selectMenu(Menu menu) {
        menu.clear();
        if (!isFormSent) {
            getActivity().getMenuInflater().inflate(R.menu.contactform_send_menu, menu);
        } else {
            // change actionbar menu how when contact form is sent?
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send:
                attemptSendForm();
                return true;
            default:
                break;
        }
        return false;
    }

    public void sendForm() {
        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
                Webservice.submitContactForm(mEmailView.getText().toString().trim(), mMessageView.getText().toString().trim());
                actionAfterSendForm();
            }
        });

    }

    public void actionAfterSendForm() {
        Needle.onMainThread().execute(new Runnable() {
            @Override
            public void run() {
                View view = getActivity().getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                Tools.showSnackbar(outerLayout, getActivity().getString(R.string.contactform_thanks));
                forwardToMainView();
            }
        });

    }

    public void forwardToMainView() {
        Needle.onMainThread().execute((Runnable) () -> {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    EventBus.getDefault().post(new GenericEvent(GenericEvent.Action.CLICK_CHEATS_DRAWER));
                }
            }, 1500);
        });
    }
}