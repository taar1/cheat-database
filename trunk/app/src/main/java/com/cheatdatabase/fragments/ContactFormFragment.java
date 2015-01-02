package com.cheatdatabase.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.TextView;

import com.cheatdatabase.R;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.businessobjects.Member;
import com.google.gson.Gson;

public class ContactFormFragment extends Fragment {

	public static final String IMAGE_RESOURCE_ID = "iconResourceID";
	public static final String ITEM_NAME = "itemName";

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private SendFormTask mContactTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mMessage;

	// UI references.
	private EditText mEmailView;
	private EditText mMessageView;
	private TextView mEmailaddressView;
	private View mContactFormView;
	private View mContactStatusView;
	private View mThankyouView;
	private TextView mLoginStatusMessageView;
	private TextView mThankyouText;

	private Member member;
	private SharedPreferences settings;

	private boolean isFormSent = false;
	private Activity ca;
	private Typeface latoFontLight;
	private View rootView;

	public ContactFormFragment() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ca = getActivity();
		Reachability.registerReachability(ca.getApplicationContext());

		settings = ca.getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
		member = new Gson().fromJson(settings.getString(Konstanten.MEMBER_OBJECT, null), Member.class);

		latoFontLight = Tools.getFont(ca.getAssets(), "Lato-Light.ttf");

		// Update action bar menu items?
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_contact_form, container, false);

		// Set up the login form.
		mEmail = ca.getIntent().getStringExtra(EXTRA_EMAIL);
		mEmailView = (EditText) rootView.findViewById(R.id.email);
		mEmailView.setText(mEmail);
		mEmailView.setTypeface(latoFontLight);
		if ((member != null) && member.getEmail() != null) {
			mEmailView.setText(member.getEmail());
		}

		mMessageView = (EditText) rootView.findViewById(R.id.form_message);
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
		mMessageView.setTypeface(latoFontLight);

		mEmailaddressView = (TextView) rootView.findViewById(R.id.emailaddress);
		mEmailaddressView.setTypeface(latoFontLight);
		Linkify.addLinks(mEmailaddressView, Linkify.ALL);

		mContactFormView = rootView.findViewById(R.id.contact_form);
		mContactStatusView = rootView.findViewById(R.id.send_status);
		mThankyouView = rootView.findViewById(R.id.thankyou);
		mThankyouText = (TextView) rootView.findViewById(R.id.thankyou_text);
		mThankyouText.setTypeface(latoFontLight);
		mLoginStatusMessageView = (TextView) rootView.findViewById(R.id.send_status_message);
		mLoginStatusMessageView.setTypeface(latoFontLight);

		return rootView;
	}

	/**
	 * Attempts to send the data in the contact form. If there are form errors
	 * (invalid email, missing fields, etc.), the errors are presented and no
	 * actual login attempt is made.
	 */
	public void attemptSendForm() {
		if (mContactTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mMessageView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mMessage = mMessageView.getText().toString();

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
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.sending_message_progress);
			showProgress(true, 1);
			mContactTask = new SendFormTask();
			mContactTask.execute((Void) null);
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
			// fade-in
			// the progress spinner.
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
		if (!isFormSent) {
			ca.getMenuInflater().inflate(R.menu.contactform_send_menu, menu);
		} else {
			// change actionbar menu how when contact form is sent?
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar buttons
		switch (item.getItemId()) {
		case R.id.action_send:
			attemptSendForm();
			return true;
		default:
			break;
		}
		return false;
	}

	public class SendFormTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			Webservice.submitContactForm(mEmailView.getText().toString().trim(), mMessageView.getText().toString().trim());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mContactTask = null;
			showProgress(false, 3);

			isFormSent = true;

			mThankyouView.setVisibility(View.VISIBLE);
			if (Build.VERSION.SDK_INT >= 11) {
				ca.invalidateOptionsMenu();
			}

		}

		@Override
		protected void onCancelled() {
			mContactTask = null;
			showProgress(false, 2);
		}
	}

}