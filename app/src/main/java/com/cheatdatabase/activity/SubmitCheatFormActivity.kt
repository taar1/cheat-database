package com.cheatdatabase.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import com.cheatdatabase.R
import com.cheatdatabase.data.model.Game
import com.cheatdatabase.data.model.UnpublishedCheat
import com.cheatdatabase.databinding.ActivitySubmitCheatLayoutBinding
import com.cheatdatabase.helpers.Konstanten
import com.cheatdatabase.helpers.Reachability
import com.cheatdatabase.helpers.Tools
import com.cheatdatabase.rest.RestApi
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

/**
 * Form to submit a cheat for a game.
 *
 * @author Dominik Erbsland
 */
@AndroidEntryPoint
class SubmitCheatFormActivity : AppCompatActivity() {

    @Inject
    lateinit var tools: Tools

    @Inject
    lateinit var restApi: RestApi

    private lateinit var gameObj: Game

    private lateinit var binding: ActivitySubmitCheatLayoutBinding

    lateinit var outerLayout: ConstraintLayout
    lateinit var textStaticCheatTitle: TextView
    lateinit var cheatTitle: TextInputEditText
    lateinit var cheatText: TextInputEditText
    lateinit var checkBoxTerms: CheckBox
    lateinit var toolbar: Toolbar

    companion object {
        private const val TAG = "SubmitCheatActivity"
    }

    private val resultContract: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            activityResultRegistry
        ) { activityResult: ActivityResult ->
            val intentReturnCode = activityResult.resultCode
            when {
                intentReturnCode == Konstanten.REGISTER_SUCCESS_RETURN_CODE -> {
                    tools.showSnackbar(outerLayout, getString(R.string.register_thanks))
                }
                intentReturnCode == Konstanten.LOGIN_SUCCESS_RETURN_CODE -> {
                    tools.showSnackbar(outerLayout, getString(R.string.login_ok))
                }
                activityResult.resultCode == Konstanten.RECOVER_PASSWORD_ATTEMPT -> {
                    tools.showSnackbar(outerLayout, getString(R.string.recover_login_success))
                }
            }
            invalidateOptionsMenu()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySubmitCheatLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindViews()
        init()

        // TODO "verstanden" button farbe ist noch violett...
        // TODO "verstanden" button farbe ist noch violett...

        showAlertDialog(
            R.string.before_you_post,
            R.string.rules_for_submissions,
            R.string.understood
        )

        gameObj = intent.getParcelableExtra("gameObj")!!
        val unpublishedCheat: UnpublishedCheat? = intent.getParcelableExtra("unpublishedCheat")

        title = gameObj.gameName
        toolbar.subtitle = gameObj.systemName

        if (unpublishedCheat != null) {
            cheatTitle.setText(unpublishedCheat.getTitle())
            cheatText.setText(unpublishedCheat.getCheat())
        }
    }

    private fun bindViews() {
        outerLayout = binding.outerLayout
        textStaticCheatTitle = binding.textCheatSubmissionTitle
        cheatTitle = binding.editCheatTitle
        cheatText = binding.editCheatText
        checkBoxTerms = binding.checkboxTerms
        toolbar = binding.toolbarLayout.toolbar

        binding.guidelines.setOnClickListener {
            showAlertDialog(
                R.string.submit_cheat_instructions_title,
                R.string.submit_cheat_guidelines,
                R.string.ok
            )
        }

        binding.termsConditions.setOnClickListener {
            showAlertDialog(
                R.string.submit_cheat_consent_title,
                R.string.submit_cheat_consent_text,
                R.string.ok
            )
        }
    }

    private fun init() {
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_send -> {
                sendButtonClicked()
                true
            }
            R.id.action_login -> {
                login()
                true
            }
            R.id.action_logout -> {
                tools.logout()
                tools.showSnackbar(outerLayout, getString(R.string.logout_ok))
                invalidateOptionsMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun sendButtonClicked() {
        if (tools.member != null && tools.member.mid != 0) {
            if (cheatText.text.toString().trim { it <= ' ' }.length < 5 || cheatTitle.text
                    .toString().trim { it <= ' ' }.length < 2
            ) {
                showAlertDialog(R.string.err, R.string.fill_everything, R.string.ok)
            } else if (!checkBoxTerms.isChecked) {
                showAlertDialog(
                    R.string.err,
                    R.string.submit_cheat_error_accept_conditions,
                    R.string.understood
                )
            } else {
                if (Reachability.reachability.isReachable) {
                    checkMemberPermissions()
                } else {
                    tools.showSnackbar(outerLayout, getString(R.string.no_internet))
                }
            }
        } else {
            login()
        }
    }

    private fun checkMemberPermissions() {
        val call: Call<JsonObject> = restApi.getMemberPermissions(tools.member.mid)
        call.enqueue(object : Callback<JsonObject?> {
            override fun onResponse(games: Call<JsonObject?>, response: Response<JsonObject?>) {
                if (response.isSuccessful) {
                    val permissions: JsonObject? = response.body()
                    val banned: Boolean? = permissions?.get("banned")?.asBoolean
                    if (banned != true) {
                        submitCheatNow()
                    } else {
                        showAlertDialog(R.string.err, R.string.member_banned, R.string.understood)
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                Log.e(TAG, "Get user permissions failed: " + t.localizedMessage)
                tools.showSnackbar(outerLayout, getString(R.string.no_internet))
            }
        })
    }

    private fun submitCheatNow() {
        val cheatTitleTrimmed: String = cheatTitle.text.toString().trim { it <= ' ' }
        val cheatTextTrimmed: String = cheatText.text.toString().trim { it <= ' ' }
        if (cheatTitleTrimmed.length < 2 || cheatTextTrimmed.length < 2) {
            finish()
        } else {
            val call: Call<JsonObject> = restApi.insertCheat(
                tools.member.mid,
                gameObj.gameId,
                cheatTitleTrimmed,
                cheatTextTrimmed
            )
            call.enqueue(object : Callback<JsonObject?> {
                override fun onResponse(games: Call<JsonObject?>, response: Response<JsonObject?>) {
                    if (response.isSuccessful) {
                        val submissionResponse: JsonObject? = response.body()
                        val returnMessage: String? =
                            submissionResponse?.get("returnMessage")?.asString

                        when {
                            returnMessage.equals("insert_ok", ignoreCase = true) -> {
                                cheatTitle.setText("")
                                cheatText.setText("")
                                showAlertDialog(
                                    R.string.thanks,
                                    R.string.cheat_submit_ok,
                                    R.string.understood
                                )
                            }
                            returnMessage.equals("missing_values", ignoreCase = true) -> {
                                showAlertDialog(
                                    R.string.err,
                                    R.string.cheat_submit_nok,
                                    R.string.understood
                                )
                            }
                            returnMessage.equals("invalid_member_id", ignoreCase = true) -> {
                                showAlertDialog(
                                    R.string.err,
                                    R.string.cheat_submit_nok,
                                    R.string.understood
                                )
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Log.e(TAG, "Submitting the cheat has failed: " + t.localizedMessage)
                    tools.showSnackbar(outerLayout, getString(R.string.no_internet))
                }
            })
        }
    }

    private fun showAlertDialog(title: Int, bodyText: Int, buttonText: Int) {

        MaterialAlertDialogBuilder(this, R.style.SimpleAlertDialog)
            .setTitle(title)
            .setMessage(bodyText)
            .setPositiveButton(buttonText) { _, _ ->
                if (title == R.string.thanks) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        finish()
                    }, 100)
                }
            }.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.submitcheat_menu, menu)
        if (tools.member != null) {
            menuInflater.inflate(R.menu.signout_menu, menu)
        } else {
            menuInflater.inflate(R.menu.signin_menu, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun login() {
        resultContract.launch(Intent(this, LoginActivity::class.java))
    }

}