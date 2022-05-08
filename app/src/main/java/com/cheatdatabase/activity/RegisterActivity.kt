package com.cheatdatabase.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.cheatdatabase.R
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.databinding.ActivityRegisterBinding
import com.cheatdatabase.helpers.*
import com.cheatdatabase.rest.RestApi
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

/**
 * Activity which displays a registration form.
 */
@Deprecated("use AuthenticationActivity.kt with RegistrationFragment.kt")
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
    lateinit var sendStatusMessage: TextView
    lateinit var registerButton: Button
    lateinit var registerSuccessTitle: TextView
    lateinit var registerSuccessText: TextView
    lateinit var progressBar: ProgressBar
    lateinit var toolbar: Toolbar

    lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindViews()
        init()

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
        sendStatusMessage = binding.sendStatusMessage
        progressBar = binding.progressBar
        registerButton = binding.registerButton
        toolbar = binding.includeToolbar.toolbar
        registerSuccessTitle = binding.registerSuccessTitle
        registerSuccessText = binding.registerSuccessText
    }

    private fun registerButtonClicked() {
        if (Reachability.reachability.isReachable) {
            attemptRegister()
        } else {
            Toast.makeText(this@RegisterActivity, R.string.no_internet, Toast.LENGTH_SHORT).show()
        }
    }

    private fun init() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

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

        var cancel = false
        var focusView: View? = null

        // Store values at the time of the login attempt.
        var mUsername = username.text.toString()
        mUsername = mUsername.replace("\\s".toRegex(), "")

        // Verify password
        val mPassword = password.text.toString().trim()
        val mPasswordRepeat = passwordRepeat.text.toString().trim()

        if (mPasswordRepeat.isEmpty()) {
            passwordRepeat.error = getString(R.string.error_password_too_short)
            focusView = passwordRepeat
            cancel = true
        } else if (mPasswordRepeat.length < 8) {
            passwordRepeat.error = getString(R.string.error_password_too_short)
            focusView = passwordRepeat
            cancel = true
        } else if (mPasswordRepeat != mPassword) {
            password.error = getString(R.string.error_password_not_identical)
            passwordRepeat.error = getString(R.string.error_password_not_identical)
            focusView = passwordRepeat
            cancel = true
        }

        if (mPassword.isEmpty()) {
            password.error = getString(R.string.error_password_too_short)
            focusView = password
            cancel = true
        } else if (mPassword.length < 8) {
            password.error = getString(R.string.error_password_too_short)
            focusView = password
            cancel = true
        }

        // Check for a valid email address.
        val mEmail = email.text.toString()
        if (mEmail.isEmpty()) {
            email.error = getString(R.string.error_field_required)
            focusView = email
            cancel = true
        } else if (mEmail.contains("@").not()) {
            email.error = getString(R.string.error_invalid_email)
            focusView = email
            cancel = true
        }

        // Check for a valid username
        if (mUsername.isEmpty()) {
            username.error = getString(R.string.error_field_required)
            focusView = username
            cancel = true
        } else if (mUsername.length < 4) {
            username.error = getString(R.string.err_username_too_short)
            focusView = username
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt register and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Delete locally saved member object
            tools.removeValue(Konstanten.MEMBER_OBJECT)

            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            sendStatusMessage.setText(R.string.register_progress_registering)
            showProgress(true)
            setRegisterSuccessVisibility(false)

            registerNewAccount(
                username.text.toString().trim { it <= ' ' },
                password.text.toString(),
                email.text.toString().trim { it <= ' ' })
        }
    }

    /**
     * Shows the progress UI and hides the register form.
     */
    private fun showProgress(showProgress: Boolean) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)
        progressBar.visibility = View.VISIBLE
        sendStatusMessage.visibility = View.VISIBLE

        progressBar.animate().setDuration(shortAnimTime.toLong())
            .alpha((if (showProgress) 1 else 0).toFloat())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    progressBar.visibility = if (showProgress) View.VISIBLE else View.GONE
                    sendStatusMessage.visibility = if (showProgress) View.VISIBLE else View.GONE
                }
            })


        setRegisterFormVisibility(!showProgress)
//        registerFormLayout.visibility = View.VISIBLE
//        registerFormLayout.animate().setDuration(shortAnimTime.toLong())
//            .alpha(if (showProgress) 0 else 1.toFloat()).setListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationEnd(animation: Animator) {
//                    handleRegisterFormVisibility(!showProgress)
//                }
//            })
    }

    private fun setRegisterFormVisibility(isVisible: Boolean) {
        email.isVisible = isVisible
        username.isVisible = isVisible
        password.isVisible = isVisible
        passwordRepeat.isVisible = isVisible
        registerButton.isVisible = isVisible
    }

    private fun setRegisterSuccessVisibility(isVisible: Boolean) {
        registerSuccessTitle.isVisible = isVisible
        registerSuccessText.isVisible = isVisible
    }

    /**
     * Represents an asynchronous registration task used to authenticate the
     * user.
     */
    private fun registerNewAccount(username: String?, password: String, email: String?) {

        var respondeCode: ResponseCode = ResponseCode.OTHER_ERROR

        lifecycleScope.launch(Dispatchers.IO) {

            val passwordMD5 = AeSimpleMD5.MD5(password.trim { it <= ' ' })
            val call = restApi.register(username, passwordMD5, email)

            val response: Response<JsonObject> = call.execute()
            val registerResponse: JsonObject? = response.body()

            // register_ok, username_already_exists, email_already_exists, parameters_too_short, other_error
            val returnValue = registerResponse!!["returnValue"].asString
            when {
                returnValue.equals("register_ok", ignoreCase = true) -> {
                    val member = Member()
                    member.mid = registerResponse["memberId"].asInt
                    member.username = registerResponse["username"].asString
                    member.email = registerResponse["email"].asString
                    member.passwordMd5 = passwordMD5
                    tools.putMember(member)

                    respondeCode = ResponseCode.REGISTER_OK
                }
                returnValue.equals("username_already_exists", ignoreCase = true) -> {
                    respondeCode = ResponseCode.USERNAME_ALREADY_EXISTS
                }
                returnValue.equals("email_already_exists", ignoreCase = true) -> {
                    respondeCode = ResponseCode.EMAIL_ALREADY_EXISTS
                }
                returnValue.equals("parameters_too_short", ignoreCase = true) -> {
                    respondeCode = ResponseCode.PARAMETERS_TOO_SHORT
                }
                returnValue.equals("other_error", ignoreCase = true) -> {
                    respondeCode = ResponseCode.OTHER_ERROR
                }
            }

            withContext(Dispatchers.Main) {
                registerTaskFinished(respondeCode)
            }
        }
    }

    private fun registerTaskFinished(respondeCode: ResponseCode) {
        showProgress(false)

        if (respondeCode == ResponseCode.REGISTER_OK) {
            tools.hideKeyboard(this, binding.outerLayout)
            setResult(Konstanten.REGISTER_SUCCESS_RETURN_CODE)
            setRegisterSuccessVisibility(true)
        } else {
            displayError(respondeCode)
        }
    }

    private fun displayError(respondeCode: ResponseCode) {
        when (respondeCode) {
            ResponseCode.USERNAME_ALREADY_EXISTS -> {
                username.error = getString(R.string.err_username_used)
                username.requestFocus()
            }
            ResponseCode.EMAIL_ALREADY_EXISTS -> {
                email.error = getString(R.string.err_email_used)
                email.requestFocus()
            }
            ResponseCode.PARAMETERS_TOO_SHORT -> {
                username.error = getString(R.string.err_parameter_too_short)
                username.requestFocus()
            }
            ResponseCode.OTHER_ERROR -> {
                username.error = getString(R.string.error_submit_security)
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