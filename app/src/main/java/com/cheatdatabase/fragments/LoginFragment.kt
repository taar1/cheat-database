package com.cheatdatabase.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cheatdatabase.R
import com.cheatdatabase.activity.AuthenticationActivity
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.databinding.FragmentLoginBinding
import com.cheatdatabase.dialogs.AlreadyLoggedInDialog
import com.cheatdatabase.helpers.AeSimpleMD5
import com.cheatdatabase.helpers.Konstanten
import com.cheatdatabase.helpers.ResponseCode
import com.cheatdatabase.helpers.Tools
import com.cheatdatabase.rest.RestApi
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LoginFragment(val activity: AuthenticationActivity) : Fragment() {

    @Inject
    lateinit var tools: Tools

    @Inject
    lateinit var restApi: RestApi

    lateinit var emailEditText: EditText
    lateinit var passwordEditText: EditText
    lateinit var forgotPassword: TextView
    lateinit var loginButton: Button
    lateinit var progressBar: ProgressBar
    lateinit var signingInMessage: TextView

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews()
        init()

        if (tools.member != null && tools.member.email != null) {
            emailEditText.setText(tools.member.email)
        }

        // If already logged in, show a popup.
        if (tools.member != null && tools.member.email.isNotEmpty()) {
            emailEditText.isEnabled = false
            passwordEditText.isEnabled = false
            loginButton.isEnabled = false

            AlreadyLoggedInDialog(tools.member).show(
                childFragmentManager,
                AlreadyLoggedInDialog.TAG
            )

        } else {
            emailEditText.isEnabled = true
            passwordEditText.isEnabled = true
            loginButton.isEnabled = true
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "LoginFragment"

        fun newInstance(activity: AuthenticationActivity): LoginFragment {
            return LoginFragment(activity)
        }
    }

    private fun bindViews() {
        emailEditText = binding.email
        passwordEditText = binding.password
        progressBar = binding.progressBar
        signingInMessage = binding.signingInMessage
        forgotPassword = binding.txtSendLogin
        loginButton = binding.loginButton
    }

    private fun init() {
        loginButton.setOnClickListener {
            attemptLogin()
        }

        forgotPassword.setOnClickListener {
            activity.forgotPassword()
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        // Reset errors.
        emailEditText.error = null
        passwordEditText.error = null

        // Store values at the time of the login attempt.
        val mEmail = emailEditText.text.toString()
        val mPassword = passwordEditText.text.toString()
        var cancel = false
        var focusView: View? = null

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            passwordEditText.error = getString(R.string.error_field_required)
            focusView = passwordEditText
            cancel = true
        } else if (mPassword.length < 4) {
            passwordEditText.error = getString(R.string.error_password_too_short)
            focusView = passwordEditText
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mEmail)) {
            emailEditText.error = getString(R.string.error_field_required)
            focusView = emailEditText
            cancel = true
        } else if (mEmail.contains("@").not()) {
            emailEditText.error = getString(R.string.error_invalid_email)
            focusView = emailEditText
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            signingInMessage.setText(R.string.login_progress_signing_in)
            showProgress(true)
            loginTask(
                emailEditText.text.toString().trim { it <= ' ' },
                passwordEditText.text.toString().trim { it <= ' ' })
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private fun showProgress(showProgress: Boolean) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)
        progressBar.visibility = View.VISIBLE
        signingInMessage.visibility = View.VISIBLE

        progressBar.animate().setDuration(shortAnimTime.toLong())
            .alpha((if (showProgress) 1 else 0).toFloat())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    progressBar.visibility = if (showProgress) View.VISIBLE else View.GONE
                    signingInMessage.visibility = if (showProgress) View.VISIBLE else View.GONE
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
            activity.setResult(Konstanten.LOGIN_SUCCESS_RETURN_CODE)
            activity.finish()
        } else {
            displayError(respondeCode)
        }
    }

    private fun displayError(respondeCode: ResponseCode) {
        when (respondeCode) {
            ResponseCode.WRONG_PASSWORD -> {
                passwordEditText.error = getString(R.string.error_incorrect_password)
                passwordEditText.requestFocus()
            }
            ResponseCode.MEMBER_NOT_FOUND -> {
                emailEditText.error = getString(R.string.err_no_member_data)
                emailEditText.requestFocus()
            }
            ResponseCode.MEMBER_BANNED -> {
                emailEditText.error = getString(R.string.member_banned)
                emailEditText.requestFocus()
            }
            else -> Toast.makeText(activity, R.string.err_occurred, Toast.LENGTH_LONG).show()
        }
    }
}