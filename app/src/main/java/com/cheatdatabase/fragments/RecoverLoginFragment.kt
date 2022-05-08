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
class RecoverLoginFragment(val activity: AuthenticationActivity) : Fragment() {

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

        fun newInstance(activity: AuthenticationActivity): RecoverLoginFragment {
            return RecoverLoginFragment(activity)
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
        step = RecoverySteps.STEP_ENTER_EMAIL
        updateUi()

        recoveryButton.setOnClickListener {
            attemptRecovery()
        }
    }

    private fun updateUi() {

        when (step) {
            RecoverySteps.STEP_ENTER_EMAIL -> {
                emailInputLayout.isVisible = true
                recoveryCodeInputLayout.isVisible = false
                passwordInputLayout.isVisible = false
                passwordRepeatInputLayout.isVisible = false

                emailInputLayout.isEnabled = true

                recoveryButton.text = getString(R.string.send_recovery_code)
            }
            RecoverySteps.STEP_RECOVERY_CODE_SENT -> {
                emailInputLayout.isVisible = true
                recoveryCodeInputLayout.isVisible = false
                passwordInputLayout.isVisible = false
                passwordRepeatInputLayout.isVisible = false

                emailInputLayout.isEnabled = false

                recoveryButton.text = getString(R.string.reset_password)
            }
            RecoverySteps.STEP_RESET_PASSWORD -> {
                emailInputLayout.isVisible = true
                recoveryCodeInputLayout.isVisible = true
                passwordInputLayout.isVisible = true
                passwordRepeatInputLayout.isVisible = true

                emailInputLayout.isEnabled = false

                recoveryButton.text = getString(R.string.reset_password)
            }
            RecoverySteps.STEP_FINISHED -> {
                emailInputLayout.isVisible = true
                recoveryCodeInputLayout.isVisible = true
                passwordInputLayout.isVisible = true
                passwordRepeatInputLayout.isVisible = true

                emailInputLayout.isEnabled = false
                recoveryCodeInputLayout.isEnabled = false
                passwordInputLayout.isEnabled = false
                passwordRepeatInputLayout.isEnabled = false
                recoveryButton.isEnabled = false
            }
        }
    }

    private fun attemptRecovery() {
        // Reset errors.
        email.error = null
        recoveryCode.error = null
        password.error = null
        passwordRepeat.error = null

        var cancel = false
        var focusView: View? = null

        if (step == RecoverySteps.STEP_ENTER_EMAIL) {
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

            if (step == RecoverySteps.STEP_ENTER_EMAIL) {
                sendRecoveryCodeByEmail(email.text.toString().trim())
            } else if (step == RecoverySteps.STEP_RESET_PASSWORD) {
                resetPassword(
                    email.text.toString().trim(),
                    recoveryCode.text.toString().trim(),
                    password.text.toString().trim()
                )
            }
        }
    }

    private fun sendRecoveryCodeByEmail(email: String) {
        var respondeCode: ResponseCode = ResponseCode.OTHER_ERROR

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val call = restApi.sendRecoveryCode(AeSimpleMD5.MD5(email))

            val response: Response<JsonObject> = call.execute()
            val registerResponse: JsonObject? = response.body()

            //no_user_found|invalid_email|recovery_code_sent|sending_email_failed
            val returnValue = registerResponse!!["returnValue"].asString
            when {
                returnValue.equals("no_user_found", ignoreCase = true) -> {
                    respondeCode = ResponseCode.NO_USER_FOUND
                }
                returnValue.equals("invalid_email", ignoreCase = true) -> {
                    respondeCode = ResponseCode.INVALID_EMAIL
                }
                returnValue.equals("recovery_code_sent", ignoreCase = true) -> {
                    respondeCode = ResponseCode.RECOVERY_CODE_SENT
                }
                returnValue.equals("sending_email_failed", ignoreCase = true) -> {
                    respondeCode = ResponseCode.SENDING_EMAIL_FAILED
                }
            }

            withContext(Dispatchers.Main) {
                step = RecoverySteps.STEP_RECOVERY_CODE_SENT
                updateUi()

                sendRecoveryCodeTaskFinished(respondeCode)
            }
        }
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


    private fun sendRecoveryCodeTaskFinished(respondeCode: ResponseCode) {
        showProgress(false)

        if (respondeCode == ResponseCode.RECOVERY_CODE_SENT) {
            step = RecoverySteps.STEP_RESET_PASSWORD
            updateUi()

            showDialog(
                getString(R.string.recovery_code_sent),
                getString(R.string.recover_code_sent_text),
                false
            )
        } else {
            displayError(respondeCode)
        }
    }

    private fun showDialog(title: String, message: String, goBackToLogin: Boolean) {
        val builder = AlertDialog.Builder(activity)
        with(builder) {
            setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int ->
                    if (goBackToLogin) {
                        activity.loginFragment()
                    }
                }
            setCancelable(false)
            create().show()
        }
    }

    private fun displayError(respondeCode: ResponseCode) {
        when (respondeCode) {
            ResponseCode.NO_USER_FOUND -> {
                email.error = getString(R.string.no_user_found_with_email)
                email.requestFocus()
            }
            ResponseCode.INVALID_EMAIL -> {
                email.error = getString(R.string.email_not_found)
                email.requestFocus()
            }
            ResponseCode.SENDING_EMAIL_FAILED -> {
                email.error = getString(R.string.error_sending_recovery_code)
                email.requestFocus()
            }
            ResponseCode.EMAIL_NOT_FOUND -> {
                email.error = getString(R.string.email_not_found)
                email.requestFocus()
            }
            ResponseCode.INVALID_DATA -> {
                email.error = getString(R.string.invalid_data)
                email.requestFocus()
            }
            ResponseCode.RECOVERY_CODE_WRONG -> {
                recoveryCode.error = getString(R.string.recovery_code_wrong)
                recoveryCode.requestFocus()
            }

            else -> Toast.makeText(
                activity,
                getString(R.string.no_internet),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun resetPassword(email: String, recoveryCode: String, newPassword: String) {
        var respondeCode: ResponseCode = ResponseCode.OTHER_ERROR

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val call = restApi.resetPassword(
                AeSimpleMD5.MD5(email),
                AeSimpleMD5.MD5(recoveryCode),
                AeSimpleMD5.MD5(newPassword)
            )

            val response: Response<JsonObject> = call.execute()
            val registerResponse: JsonObject? = response.body()

            //email_not_found|invalid_data|recovery_code_wrong|reset_success
            val returnValue = registerResponse!!["returnValue"].asString
            when {
                returnValue.equals("email_not_found", ignoreCase = true) -> {
                    respondeCode = ResponseCode.EMAIL_NOT_FOUND
                }
                returnValue.equals("invalid_data", ignoreCase = true) -> {
                    respondeCode = ResponseCode.INVALID_DATA
                }
                returnValue.equals("recovery_code_wrong", ignoreCase = true) -> {
                    respondeCode = ResponseCode.RECOVERY_CODE_WRONG
                }
                returnValue.equals("reset_success", ignoreCase = true) -> {
                    respondeCode = ResponseCode.PASSWORD_RESET_SUCCESS
                }
            }

            withContext(Dispatchers.Main) {
                resetPasswordTaskFinished(respondeCode)
            }
        }
    }

    private fun resetPasswordTaskFinished(respondeCode: ResponseCode) {
        showProgress(false)

        if (respondeCode == ResponseCode.PASSWORD_RESET_SUCCESS) {
            step = RecoverySteps.STEP_FINISHED
            updateUi()

            showDialog(
                getString(R.string.password_reset_success),
                getString(R.string.password_reset_success_text),
                true
            )
        } else {
            displayError(respondeCode)
        }
    }

    enum class RecoverySteps {
        STEP_ENTER_EMAIL,
        STEP_RECOVERY_CODE_SENT,
        STEP_RESET_PASSWORD,
        STEP_FINISHED
    }

}