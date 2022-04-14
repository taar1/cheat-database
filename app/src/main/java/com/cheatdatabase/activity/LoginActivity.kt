package com.cheatdatabase.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.cheatdatabase.R
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.databinding.ActivityLoginBinding
import com.cheatdatabase.dialogs.AlreadyLoggedInDialog
import com.cheatdatabase.dialogs.AlreadyLoggedInDialog.AlreadyLoggedInDialogListener
import com.cheatdatabase.helpers.*
import com.cheatdatabase.rest.RestApi
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
@AndroidEntryPoint
class LoginActivity : AppCompatActivity(), AlreadyLoggedInDialogListener {

    @Inject
    lateinit var tools: Tools

    @Inject
    lateinit var restApi: RestApi

    lateinit var emailView: EditText
    lateinit var passwordView: EditText
    lateinit var loginFormView: LinearLayout
    lateinit var loginStatusView: View
    lateinit var loginStatusMessageView: TextView
    lateinit var forgotPassword: TextView
    lateinit var loginButton: Button
    lateinit var cancelButton: Button
    lateinit var toolbar: Toolbar
    lateinit var progressBar: ProgressBar

    // Values for email and password at the time of the login attempt.
    private var mEmail: String? = null

    lateinit var binding: ActivityLoginBinding

    private val resultContract = registerForActivityResult(
        StartActivityForResult(),
        activityResultRegistry
    ) { activityResult: ActivityResult ->
        val intentReturnCode = activityResult.resultCode
        when {
            intentReturnCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE -> {
                setResult(Konstanten.REGISTER_SUCCESS_RETURN_CODE)
                finish()
            }
            intentReturnCode == Konstanten.RECOVER_PASSWORD_SUCCESS_RETURN_CODE -> {
                setResult(Konstanten.RECOVER_PASSWORD_SUCCESS_RETURN_CODE)
            }
            activityResult.resultCode == Konstanten.RECOVER_PASSWORD_ATTEMPT -> {
                setResult(Konstanten.RECOVER_PASSWORD_ATTEMPT)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindViews()
        init()

        // Set up the login form.
        mEmail = intent.getStringExtra(EXTRA_EMAIL)
        emailView.setText(mEmail)
        if (tools.member != null && tools.member.email != null) {
            emailView.setText(tools.member.email)
        }
        passwordView.setOnEditorActionListener { textView: TextView?, id: Int, keyEvent: KeyEvent? ->
            if (id == 111 || id == EditorInfo.IME_NULL) {
                if (Reachability.reachability.isReachable) {
                    attemptLogin()
                } else {
                    Toast.makeText(this@LoginActivity, R.string.no_internet, Toast.LENGTH_SHORT)
                        .show()
                }
                return@setOnEditorActionListener true
            }
            false
        }


        // If already logged in, show a popup.
        if (tools.member != null && tools.member.email.isNotEmpty()) {
            emailView.isEnabled = false
            passwordView.isEnabled = false
            loginButton.isEnabled = false
            val fm = supportFragmentManager
            val alreadyLoggedInDialog = AlreadyLoggedInDialog()
            alreadyLoggedInDialog.show(fm, "already_logged_in")
        } else {
            emailView.isEnabled = true
            passwordView.isEnabled = true
            loginButton.isEnabled = true
        }
    }

    private fun bindViews() {
        emailView = binding.email
        passwordView = binding.password
        loginFormView = binding.loginForm
        loginStatusView = binding.sendStatus
        loginStatusMessageView = binding.sendStatusMessage
        forgotPassword = binding.txtSendLogin
        loginButton = binding.loginButton
        cancelButton = binding.cancelButton
        toolbar = binding.includeToolbar.toolbar
    }

    private fun init() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        loginButton.setOnClickListener {
            loginButtonClicked()
        }

        forgotPassword.setOnClickListener {
            forgotPasswordClicked()
        }

        cancelButton.setOnClickListener {
            cancelButtonClicked()
        }

    }

    fun forgotPasswordClicked() {
        resultContract.launch(Intent(this, RecoverActivity::class.java))
    }

    fun loginButtonClicked() {
        if (Reachability.reachability.isReachable) {
            attemptLogin()
        } else {
            Toast.makeText(this@LoginActivity, R.string.no_internet, Toast.LENGTH_SHORT).show()
        }
    }

    fun cancelButtonClicked() {
        setResult(RESULT_CANCELED)
        finish()
    }

    public override fun onPause() {
        super.onPause()
        Reachability.unregister(this)
    }

    override fun onResume() {
        super.onResume()
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.register_menu, menu)
        if (tools.member != null) {
            menuInflater.inflate(R.menu.signout_menu, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar buttons
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_register -> {
                resultContract.launch(Intent(this, RegisterActivity::class.java))
                true
            }
            R.id.action_forgot_password -> {
                resultContract.launch(Intent(this, RecoverActivity::class.java))
                true
            }
            R.id.action_logout -> {
                tools.logout()
                invalidateOptionsMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        // Reset errors.
        emailView.error = null
        passwordView.error = null

        // Store values at the time of the login attempt.
        mEmail = emailView.text.toString()
        val mPassword = passwordView.text.toString()
        var cancel = false
        var focusView: View? = null

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            passwordView.error = getString(R.string.error_field_required)
            focusView = passwordView
            cancel = true
        } else if (mPassword.length < 4) {
            passwordView.error = getString(R.string.error_password_too_short)
            focusView = passwordView
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mEmail)) {
            emailView.error = getString(R.string.error_field_required)
            focusView = emailView
            cancel = true
        } else if (!mEmail.contains("@")) {

            // TODO FIXME...
            // TODO FIXME...
            // TODO FIXME...
            // TODO FIXME...
            emailView.error = getString(R.string.error_invalid_email)
            focusView = emailView
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            loginStatusMessageView.setText(R.string.login_progress_signing_in)
            showProgress(true)
            loginTask(
                emailView.text.toString().trim { it <= ' ' },
                passwordView.text.toString().trim { it <= ' ' })
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private fun showProgress(showProgress: Boolean) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)
        loginStatusView.visibility = View.VISIBLE

        loginStatusView.animate().setDuration(shortAnimTime.toLong())
            .alpha((if (showProgress) 1 else 0).toFloat())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    loginStatusView.visibility = if (showProgress) View.VISIBLE else View.GONE
                }
            })

        loginFormView.visibility = View.VISIBLE
        loginFormView.animate().setDuration(shortAnimTime.toLong())
            .alpha((if (showProgress) 0 else 1).toFloat())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    loginFormView.visibility = if (showProgress) View.GONE else View.VISIBLE
                }
            })
    }

    private fun loginTask(username: String?, password: String) {

        var respondeCode: ResponseCode = ResponseCode.OTHER_ERROR

        lifecycleScope.launch(Dispatchers.IO) {
            val passwordMD5 = AeSimpleMD5.MD5(password.trim { it <= ' ' })
            val call = restApi.login(username, passwordMD5)

            val response: Response<JsonObject> = call.execute()
            val registerResponse: JsonObject? = response.body()

            // login_ok, wrong_pw, member_not_found
            val returnValue = registerResponse!!["returnValue"].asString
            when {
                returnValue.equals("login_ok", ignoreCase = true) -> {
                    val member = Member()
                    member.mid = registerResponse["memberId"].asInt
                    member.username = registerResponse["username"].asString
                    member.email = registerResponse["email"].asString
                    member.password = password
                    //member.writeMemberData(member, tools.getSharedPreferences());
                    tools.putMember(member)

                    respondeCode = ResponseCode.LOGIN_OK
                }
                returnValue.equals("wrong_pw", ignoreCase = true) -> {
                    respondeCode = ResponseCode.WRONG_PASSWORD
                }
                returnValue.equals("member_not_found", ignoreCase = true) -> {
                    respondeCode = ResponseCode.MEMBER_NOT_FOUND
                }
                returnValue.equals("member_banned", ignoreCase = true) -> {
                    respondeCode = ResponseCode.MEMBER_BANNED
                }
            }

            withContext(Dispatchers.Main) {
                afterLogin(respondeCode)
            }
        }
    }

    private fun afterLogin(respondeCode: ResponseCode) {
        showProgress(false)

        if (respondeCode == ResponseCode.LOGIN_OK) {
            setResult(Konstanten.LOGIN_SUCCESS_RETURN_CODE)
            finish()
        } else {
            displayError(respondeCode)
        }
    }

    private fun displayError(respondeCode: ResponseCode) {
        when (respondeCode) {
            ResponseCode.WRONG_PASSWORD -> {
                passwordView.error = getString(R.string.error_incorrect_password)
                passwordView.requestFocus()
            }
            ResponseCode.MEMBER_NOT_FOUND -> {
                emailView.error = getString(R.string.err_no_member_data)
                emailView.requestFocus()
            }
            ResponseCode.MEMBER_BANNED -> {
                emailView.error = getString(R.string.member_banned)
                emailView.requestFocus()
            }
            else -> Toast.makeText(this@LoginActivity, R.string.err_occurred, Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onFinishDialog(signOutNow: Boolean) {
        if (signOutNow) {
            tools.removeValue(Konstanten.MEMBER_OBJECT)
            emailView.setText("")
            emailView.isEnabled = true
            passwordView.setText("")
            passwordView.isEnabled = true
            loginButton.isEnabled = true
            Toast.makeText(this@LoginActivity, R.string.logout_ok, Toast.LENGTH_LONG).show()
        } else {
            finish()
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
        const val EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL"
    }
}