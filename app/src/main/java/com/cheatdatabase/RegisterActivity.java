package com.cheatdatabase;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.helpers.Webservice;
import com.cheatdatabase.businessobjects.Member;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class RegisterActivity extends Activity {

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserRegisterTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mUsername;

	// UI references.
	private EditText mEmailView;
	private EditText mUsernameView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	private Member member;
	private SharedPreferences settings;

	private Editor editor;

	private Typeface latoFontBold;

	private Typeface latoFontLight;

	private Button registerButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		Reachability.registerReachability(this.getApplicationContext());

		Tools.styleActionbar(this);
		ActionBar actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(true);

		latoFontBold = Tools.getFont(getAssets(), "Lato-Bold.ttf");
		latoFontLight = Tools.getFont(getAssets(), "Lato-Light.ttf");

		settings = getSharedPreferences(Konstanten.PREFERENCES_FILE, 0);
		editor = settings.edit();

		// Set up the login form.
		mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);
		mEmailView.setTypeface(latoFontBold);

		mUsernameView = (EditText) findViewById(R.id.username);
		mUsernameView.setTypeface(latoFontBold);
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

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.send_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.send_status_message);
		mLoginStatusMessageView.setTypeface(latoFontLight);

		registerButton = (Button) findViewById(R.id.register_button);
		registerButton.setTypeface(latoFontBold);
		registerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (Reachability.reachability.isReachable) {
					attemptRegister();
				} else {
					Toast.makeText(RegisterActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
				}
			}
		});

	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptRegister() {
		if (mAuthTask != null) {
			return;
		}

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
			mAuthTask = new UserRegisterTask();
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
	 * Represents an asynchronous registration task used to authenticate the
	 * user.
	 */
	public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {

			member = Webservice.register(mUsernameView.getText().toString().trim(), mEmailView.getText().toString().trim());
			if (member.getErrorCode() == 0) {
				member.writeMemberData(member, settings);

				return true;
			} else {
				return false;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
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

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
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
