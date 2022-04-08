package com.cheatdatabase.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.cheatdatabase.R
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.databinding.ActivityRegisterBinding
import com.cheatdatabase.helpers.AeSimpleMD5
import com.cheatdatabase.helpers.Konstanten
import com.cheatdatabase.helpers.Reachability
import com.cheatdatabase.helpers.Tools
import com.cheatdatabase.rest.RestApi
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import needle.Needle
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.NoSuchAlgorithmException
import javax.inject.Inject

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    @Inject
    lateinit var tools: Tools

    @Inject
    lateinit var restApi: RestApi

    lateinit var email: TextInputEditText
    lateinit var username: TextInputEditText
    lateinit var password: TextInputEditText
    lateinit var passwordRepeat: TextInputEditText
    lateinit var registerFormLayout: LinearLayout
    lateinit var sendStatusLayout: LinearLayout
    lateinit var sendStatusMessage: TextView
    lateinit var registerButton: Button
    lateinit var toolbar: Toolbar

    lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindViews()
        init()

        // TODO diese klasse fixen....
        // TODO diese klasse fixen....
        // TODO diese klasse fixen....
        // TODO diese klasse fixen....
        // TODO diese klasse fixen....

        // Set up the login form.
        val mEmail = intent.getStringExtra(EXTRA_EMAIL)
        email.setText(mEmail)
        username.setOnEditorActionListener { textView: TextView?, id: Int, keyEvent: KeyEvent? ->
            if (id == 222 || id == EditorInfo.IME_NULL) {
                if (Reachability.reachability.isReachable) {
                    attemptRegister()
                    return@setOnEditorActionListener true
                } else {
                    Toast.makeText(this@RegisterActivity, R.string.no_internet, Toast.LENGTH_SHORT)
                        .show()
                }
            }
            false
        }
        username.onFocusChangeListener = OnFocusChangeListener { v: View?, hasFocus: Boolean ->
            if (!hasFocus) {
                var fixedUsername = username.text.toString()
                fixedUsername = fixedUsername.replace("\\s".toRegex(), "")
                username.setText(fixedUsername)
            }
        }
    }

    private fun bindViews() {
        email = binding.email
        username = binding.username
        password = binding.password
        passwordRepeat = binding.passwordRepeat
        registerFormLayout = binding.registerForm
        sendStatusLayout = binding.sendStatus
        sendStatusMessage = binding.sendStatusMessage
        registerButton = binding.registerButton
        toolbar = binding.includeToolbar.toolbar
    }

    fun registerButtonClicked() {
        if (Reachability.reachability.isReachable) {
            attemptRegister()
        } else {
            Toast.makeText(this@RegisterActivity, R.string.no_internet, Toast.LENGTH_SHORT).show()
        }
    }

    private fun init() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        registerButton.setOnClickListener {
            registerButtonClicked()
        }
    }

    public override fun onPause() {
        Reachability.unregister(this)
        super.onPause()
    }

    override fun onResume() {
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(this)
        }
        super.onResume()
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptRegister() {
        // Reset errors.
        email.error = null
        username.error = null

        // Store values at the time of the login attempt.
        val mEmail = email.text.toString()
        var mUsername = username.text.toString()
        mUsername = mUsername.replace("\\s".toRegex(), "")
        var cancel = false
        var focusView: View? = null

        // Check for a valid password.
        if (TextUtils.isEmpty(mUsername)) {
            username.error = getString(R.string.error_field_required)
            focusView = username
            cancel = true
        } else if (mUsername.length < 4) {
            username.error = getString(R.string.err_username_too_short)
            focusView = username
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mEmail)) {
            email.error = getString(R.string.error_field_required)
            focusView = email
            cancel = true
        } else if (mEmail.contains("@").not()) {
            email.error = getString(R.string.error_invalid_email)
            focusView = email
            cancel = true
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            // Delete locally saved member object
            tools.removeValue(Konstanten.MEMBER_OBJECT)

            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            sendStatusMessage.setText(R.string.register_progress_registering)
            showProgress(true)

            registerTask(
                username.text.toString().trim { it <= ' ' },
                password.text.toString(),
                email.text.toString().trim { it <= ' ' })
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)
        sendStatusLayout.visibility = View.VISIBLE
        sendStatusLayout.animate().setDuration(shortAnimTime.toLong())
            .alpha(if (show) 1 else 0.toFloat()).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    sendStatusLayout.visibility = if (show) View.VISIBLE else View.GONE
                }
            })
        registerFormLayout.visibility = View.VISIBLE
        registerFormLayout.animate().setDuration(shortAnimTime.toLong())
            .alpha(if (show) 0 else 1.toFloat()).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    registerFormLayout.visibility = if (show) View.GONE else View.VISIBLE
                }
            })
    }

    /**
     * Represents an asynchronous registration task used to authenticate the
     * user.
     */
    private fun registerTask(username: String?, password: String, email: String?) {
        val password_md5: String
        try {
            password_md5 = AeSimpleMD5.MD5(password.trim { it <= ' ' })
            val call = restApi.register(username, password_md5, email)
            call.enqueue(object : Callback<JsonObject?> {
                override fun onResponse(forum: Call<JsonObject?>, response: Response<JsonObject?>) {
                    val registerResponse = response.body()

                    // register_ok, username_already_exists, email_already_exists, parameters_too_short, other_error
                    val returnValue = registerResponse!!["returnValue"].asString
                    when {
                        returnValue.equals("register_ok", ignoreCase = true) -> {
                            val member = Member()
                            member.mid = registerResponse["memberId"].asInt
                            member.username = registerResponse["username"].asString
                            member.email = registerResponse["email"].asString
                            member.password = registerResponse["pw"].asString
                            //member.writeMemberData(member, tools.getSharedPreferences());
                            tools.putMember(member)
                            registerTaskFinished(true, 0)
                        }
                        returnValue.equals("username_already_exists", ignoreCase = true) -> {
                            registerTaskFinished(false, 1)
                        }
                        returnValue.equals("email_already_exists", ignoreCase = true) -> {
                            registerTaskFinished(false, 2)
                        }
                        returnValue.equals("parameters_too_short", ignoreCase = true) -> {
                            registerTaskFinished(false, 4)
                        }
                        returnValue.equals("other_error", ignoreCase = true) -> {
                            registerTaskFinished(false, 99)
                        }
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, e: Throwable) {
                    registerTaskFinished(false, 99)
                }
            })
        } catch (e: NoSuchAlgorithmException) {
            registerTaskFinished(false, 9)
        }
    }

    fun registerTaskFinished(success: Boolean, errorCode: Int) {
        Needle.onMainThread().execute {
            showProgress(false)
            if (success) {
                setResult(Konstanten.REGISTER_SUCCESS_RETURN_CODE)
                finish()
            } else {
                displayError(errorCode)
            }
        }
    }

    private fun displayError(errorCode: Int) {
        when (errorCode) {
            1 -> {
                username.error = getString(R.string.err_username_used)
                username.requestFocus()
            }
            2 -> {
                email.error = getString(R.string.err_email_used)
                email.requestFocus()
            }
            4 -> {
                username.error = getString(R.string.err_parameter_too_short)
                username.requestFocus()
            }
            9 -> {
                username.error = "Error submitting registration data securely. Please try again."
                username.requestFocus()
            }
            else -> Toast.makeText(
                this@RegisterActivity,
                R.string.err_creating_user_account,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    companion object {
        /**
         * The default email to populate the email field with.
         */
        const val EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL"
    }
}