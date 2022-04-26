package com.cheatdatabase.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cheatdatabase.R
import com.cheatdatabase.activity.AuthenticationActivity
import com.cheatdatabase.databinding.FragmentRecoverBinding
import com.cheatdatabase.helpers.AeSimpleMD5
import com.cheatdatabase.helpers.ResponseCode
import com.cheatdatabase.helpers.Tools
import com.cheatdatabase.rest.RestApi
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
class RecoverFragment(val activity: AuthenticationActivity) : Fragment() {

    @Inject
    lateinit var tools: Tools

    @Inject
    lateinit var restApi: RestApi

    lateinit var email: TextInputEditText
    lateinit var emailInputLayout: TextInputLayout
    lateinit var recoveryCode: TextInputEditText
    lateinit var recoveryCodeInputLayout: TextInputLayout
    lateinit var password: TextInputEditText
    lateinit var passwordInputLayout: TextInputLayout
    lateinit var passwordRepeat: TextInputEditText
    lateinit var passwordRepeatInputLayout: TextInputLayout
    lateinit var sendStatusMessage: TextView
    lateinit var recoveryButton: Button
    lateinit var progressBar: ProgressBar

    lateinit var step: RecoverySteps

    private var _binding: FragmentRecoverBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews()
        init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "RecoverFragment"

        fun newInstance(activity: AuthenticationActivity): RecoverFragment {
            return RecoverFragment(activity)
        }
    }

    private fun bindViews() {
        email = binding.email
        emailInputLayout = binding.emailInputLayout
        recoveryCode = binding.recoveryCode
        recoveryCodeInputLayout = binding.recoveryCodeInputLayout
        password = binding.password
        passwordInputLayout = binding.passwordInputLayout
        passwordRepeat = binding.passwordRepeat
        passwordRepeatInputLayout = binding.passwordRepeatInputLayout
        sendStatusMessage = binding.sendStatusMessage
        progressBar = binding.progressBar
        recoveryButton = binding.recoveryButton
    }

    private fun init() {
        step = RecoverySteps.STEP_SEND_RECOVERY_CODE

        emailInputLayout.isVisible = true
        recoveryCodeInputLayout.isVisible = false
        passwordInputLayout.isVisible = false
        passwordRepeatInputLayout.isVisible = false

        recoveryButton.setOnClickListener {
            attemptRegister()
        }
    }

    private fun attemptRegister() {

        // Reset errors.
        email.error = null
        recoveryCode.error = null
        password.error = null
        passwordRepeat.error = null

        var cancel = false
        var focusView: View? = null

        if (step == RecoverySteps.STEP_SEND_RECOVERY_CODE) {
            // Check for a valid email address.
            val mEmail = email.text.toString().trim()
            if (mEmail.isEmpty()) {
                email.error = getString(R.string.error_field_required)
                focusView = email
                cancel = true
            } else if (mEmail.contains("@").not()) {
                email.error = getString(R.string.error_invalid_email)
                focusView = email
                cancel = true
            }
        } else if (step == RecoverySteps.STEP_RESET_PASSWORD) {
            val mRecoveryCode = recoveryCode.text.toString().trim()
            val mPassword = password.text.toString().trim()
            val mPasswordRepeat = passwordRepeat.text.toString().trim()

            when {
                mPasswordRepeat.isEmpty() -> {
                    passwordRepeat.error = getString(R.string.error_password_too_short)
                    focusView = passwordRepeat
                    cancel = true
                }
                mPasswordRepeat.length < 8 -> {
                    passwordRepeat.error = getString(R.string.error_password_too_short)
                    focusView = passwordRepeat
                    cancel = true
                }
                mPasswordRepeat != mPassword -> {
                    password.error = getString(R.string.error_password_not_identical)
                    passwordRepeat.error = getString(R.string.error_password_not_identical)
                    focusView = passwordRepeat
                    cancel = true
                }
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

            // Check for a valid username
            if (mRecoveryCode.isEmpty()) {
                recoveryCode.error = getString(R.string.error_field_required)
                focusView = recoveryCode
                cancel = true
            } else if (mRecoveryCode.length < 4) {
                recoveryCode.error = getString(R.string.error_value_too_short)
                focusView = recoveryCode
                cancel = true
            }
        }



        if (cancel) {
            // There was an error; don't proceed and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            showProgress(true)
            setRegisterSuccessVisibility(false)

            if (step == RecoverySteps.STEP_SEND_RECOVERY_CODE) {
                // TODO....
                sendRecoveryCodeByEmail(email.text.toString().trim())
            } else if (step == RecoverySteps.STEP_RESET_PASSWORD) {
                resetPassword(
                    email.text.toString().trim(),
                    recoveryCode.text.toString().trim(),
                    password.text.toString().trim(),
                    passwordRepeat.text.toString().trim()
                )
            }
        }
    }

    private fun validateRecoveryCode(recoveryCode: String) {
        // TODO if invalid, show error message in form
        // TODO if valid, proceed with resetPassword()
    }

    private fun resetPassword(
        email: String,
        recoveryCode: String,
        password: String,
        passwordRepeat: String
    ) {
        // TODO check/validate recovery code, if okay, proceed, otherwise show error message
        validateRecoveryCode(recoveryCode)

        // TODO reset password
        // TODO show dialog with success message
        // TODO go back to login screen
    }

    private fun sendRecoveryCodeByEmail(email: String) {
        // TODO send recovery code by email
        // TODO show dialog telling the user to enter recovery code and new password
        // TODO show remaining fields (recovery code, password, password repeat)
    }

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
    }

    private fun setRegisterFormVisibility(isVisible: Boolean) {
        email.isVisible = isVisible
        recoveryCode.isVisible = isVisible
        password.isVisible = isVisible
        passwordRepeat.isVisible = isVisible
        recoveryButton.isVisible = isVisible
    }

    private fun setRegisterSuccessVisibility(isVisible: Boolean) {
//        registerSuccessTitle.isVisible = isVisible
//        registerSuccessText.isVisible = isVisible
    }

    /**
     * Represents an asynchronous registration task used to authenticate the
     * user.
     */
    private fun registerNewAccount(username: String, password: String, email: String) {

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
            showRegistrationSuccessfulDialog()
        } else {
            displayError(respondeCode)
        }
    }

    private fun showRegistrationSuccessfulDialog() {
        val builder = AlertDialog.Builder(activity)
        with(builder) {
            setTitle(getString(R.string.registration_successful))
                .setMessage("You can now login.")
                .setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int ->
                    activity.loginFragment()
                    activity.invalidateOptionsMenu()
                }
            setCancelable(false)
            create().show()
        }
    }

    private fun displayError(respondeCode: ResponseCode) {
        when (respondeCode) {
            ResponseCode.EMAIL_NOT_FOUND -> {
                email.error = getString(R.string.email_not_found)
                email.requestFocus()
            }
            ResponseCode.RECOVERY_CODE_WRONG -> {
                recoveryCode.error = getString(R.string.wrong_recovery_code)
                recoveryCode.requestFocus()
            }
            else -> Toast.makeText(
                activity,
                "Account could not be restored.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    enum class RecoverySteps {
        STEP_SEND_RECOVERY_CODE,
        STEP_RESET_PASSWORD
    }

}