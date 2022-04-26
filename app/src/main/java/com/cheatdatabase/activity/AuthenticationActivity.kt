package com.cheatdatabase.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.cheatdatabase.R
import com.cheatdatabase.databinding.AuthenticationActivityBinding
import com.cheatdatabase.dialogs.AlreadyLoggedInDialog
import com.cheatdatabase.fragments.LoginFragment
import com.cheatdatabase.fragments.RecoverFragment
import com.cheatdatabase.fragments.RegistrationFragment
import com.cheatdatabase.helpers.Konstanten
import com.cheatdatabase.helpers.Tools
import com.cheatdatabase.rest.RestApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class AuthenticationActivity : AppCompatActivity(),
    AlreadyLoggedInDialog.AlreadyLoggedInDialogListener {

    @Inject
    lateinit var tools: Tools

    @Inject
    lateinit var restApi: RestApi

    lateinit var toolbarMenu: Menu
    lateinit var searchView: SearchView

    lateinit var binder: AuthenticationActivityBinding

    private val resultContract = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
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

        binder = AuthenticationActivityBinding.inflate(layoutInflater)

        setContentView(binder.root)
        setSupportActionBar(binder.includeToolbar.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.container, LoginFragment.newInstance(this), "LoginFragment"
                )
                .commitNow()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)

        toolbarMenu = menu
        menu.clear()

        when (supportFragmentManager.fragments[0].tag) {
            "LoginFragment" -> {
                menuInflater.inflate(R.menu.register_menu, menu)
            }
            "RegistrationFragment" -> {
                menuInflater.inflate(R.menu.login_menu, menu)
            }
            "RecoverFragment" -> {
                menu.clear()
            }
            else -> {
                if (tools.member != null) {
                    menuInflater.inflate(R.menu.signout_menu, menu)
                }
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            R.id.action_register -> {
                registrationFragment()
                true
            }
            R.id.action_login -> {
                loginFragment()
                true
            }

            R.id.action_forgot_password -> {
                resetPasswordFragment()
                true
            }
            R.id.action_logout -> {
                tools.logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun resetPasswordFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, RecoverFragment.newInstance(this), "RecoverFragment")
            .commitNow()
        title = getString(R.string.reset_password)
        invalidateOptionsMenu()
    }

    fun loginFragment() {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.container, LoginFragment.newInstance(this), "LoginFragment"
            )
            .commitNow()
        title = getString(R.string.action_sign_in_short)
        invalidateOptionsMenu()
    }

    fun registrationFragment() {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.container, RegistrationFragment.newInstance(this), "RegistrationFragment"
            )
            .commitNow()
        title = getString(R.string.register)
        invalidateOptionsMenu()
    }

    /**
     * Close dialog and logout.
     */
    override fun onFinishDialog(signOutNow: Boolean) {
        if (signOutNow) {
            tools.removeValue(Konstanten.MEMBER_OBJECT)
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, LoginFragment.newInstance(this), "LoginFragment")
                .commitNow()
            Toast.makeText(this, R.string.logout_ok, Toast.LENGTH_LONG).show()
        } else {
            finish()
        }
    }

}