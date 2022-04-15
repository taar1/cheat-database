package com.cheatdatabase.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.applovin.adview.AppLovinAdView
import com.cheatdatabase.R
import com.cheatdatabase.data.model.ForumPost
import com.cheatdatabase.databinding.ActivityCheatForumBinding
import com.cheatdatabase.helpers.AeSimpleMD5
import com.cheatdatabase.helpers.Konstanten
import com.cheatdatabase.helpers.Reachability
import com.cheatdatabase.helpers.Tools
import com.cheatdatabase.rest.RestApi
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject

/**
 * Displaying the forum of one cheat.
 */
@AndroidEntryPoint
class CheatForumActivity : AppCompatActivity() {
    private var cheatId: Int = 0
    private var forumCount: Int = 0

    @Inject
    lateinit var tools: Tools

    @Inject
    lateinit var restApi: RestApi

    lateinit var outerLayout: LinearLayout
    lateinit var mToolbar: Toolbar
    lateinit var mainLayout: LinearLayout
    lateinit var tvCheatTitle: TextView
    lateinit var tvEmpty: TextView
    lateinit var scrollView: ScrollView
    lateinit var reloadView: ImageView
    lateinit var editText: EditText
    lateinit var postButton: Button
    lateinit var appLovinAdView: AppLovinAdView

    private lateinit var binding: ActivityCheatForumBinding

    private val resultContract = registerForActivityResult(
        StartActivityForResult(),
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

        binding = ActivityCheatForumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cheatId = intent.getIntExtra("cheatId", 0)

        bindViews()
        init()

        if (Reachability.reachability.isReachable) {
            reloadView.visibility = View.GONE
            fetchForumPosts()
        } else {
            reloadView.visibility = View.VISIBLE
            reloadView.setOnClickListener {
                if (Reachability.reachability.isReachable) {
                    fetchForumPosts()
                } else {
                    showToast()
                }
            }
            showToast()
        }
        postButton.setOnClickListener {
            if (Reachability.reachability.isReachable) {
                if (editText.text.toString().trim { it <= ' ' }.isNotEmpty()) {
                    submitPost()
                } else {
                    Toast.makeText(
                        this@CheatForumActivity,
                        R.string.fill_everything,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                showToast()
            }
        }
    }

    private fun showToast() {
        Toast.makeText(this@CheatForumActivity, R.string.no_internet, Toast.LENGTH_SHORT).show()
    }

    private fun bindViews() {
        outerLayout = binding.outerLayout
        mToolbar = binding.toolbar
        mainLayout = binding.mainForum
        tvCheatTitle = binding.textCheatTitle
        tvEmpty = binding.textviewEmpty
        scrollView = binding.sv
        reloadView = binding.reload
        editText = binding.forumTextInput
        postButton = binding.submitButton
        appLovinAdView = binding.adContainer
    }

    private fun init() {
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        appLovinAdView.loadNextAd()
    }

    /**
     * Submits the forum post and scrolls down to the bottom of the list.
     */
    private fun submitPost() {
        if (tools.member == null) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_LONG).show()
        } else {
            // get the current date
            val c = Calendar.getInstance()
            val mYear = c[Calendar.YEAR]
            val mMonth = c[Calendar.MONTH]
            val mDay = c[Calendar.DAY_OF_MONTH]
            val mHour = c[Calendar.HOUR_OF_DAY]
            var leadingZeroHour = mHour.toString()
            if (leadingZeroHour.length == 1) {
                leadingZeroHour = "0$mHour"
            }
            val mMin = c[Calendar.MINUTE]
            var leadingZeroMin = mMin.toString()
            if (leadingZeroMin.length == 1) {
                leadingZeroMin = "0$mMin"
            }
            // int mdate = c.get(Calendar.DATE);
            val months = resources.getStringArray(R.array.months)
            val forumPost = ForumPost()
            forumPost.text = editText.text.toString().trim { it <= ' ' }
            forumPost.username = tools.member.username
            forumPost.name = tools.member.username
            forumPost.email = tools.member.email
            forumPost.created =
                months[mMonth].toString() + " " + mDay + ", " + mYear + " / " + leadingZeroHour + ":" + leadingZeroMin
            if (Reachability.reachability.isReachable) {
                mainLayout.addView(
                    createForumPosts(forumPost),
                    TableLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )

                // Scroll down to the newest forum post
                scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
                editText.isEnabled = false
                postButton.isEnabled = false
                object : CountDownTimer(5000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        postButton.isEnabled = true
                        editText.isEnabled = true
                    }
                }.start()

                submitForumPost(forumPost)
            } else {
                showToast()
            }
        }
    }

    /**
     * Fills the table of the forum
     *
     * @param forumPost
     * @return
     */
    private fun createForumPosts(forumPost: ForumPost): LinearLayout {
        // TODO FIXME hier das layout auf cards ändern? hm..
        // TODO FIXME hier das layout auf cards ändern? hm..
        // TODO FIXME hier das layout auf cards ändern? hm..
//        val cardLayoutPortrait = CardLayoutPortrait(this, null)
//        cardLayoutPortrait.layoutParams = ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
//        cardLayoutPortrait.setBackgroundColor(Color.RED)


        val tl = LinearLayout(this)
        tl.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        tl.gravity = Gravity.TOP
        tl.setBackgroundColor(Color.BLACK)
        tl.orientation = LinearLayout.VERTICAL
        val tvFirstThCol = TextView(this)
        val tvSecondThCol = TextView(this)

        // Header info of forum post
        val rowForumPostHeader = LinearLayout(this)
        rowForumPostHeader.setBackgroundColor(Color.DKGRAY)
        rowForumPostHeader.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        rowForumPostHeader.gravity = Gravity.CENTER_HORIZONTAL
        rowForumPostHeader.setPadding(5, 5, 10, 5)
        rowForumPostHeader.orientation = LinearLayout.HORIZONTAL
        if (!forumPost.username.equals("null", ignoreCase = true)) {
            tvFirstThCol.text = forumPost.username.trim { it <= ' ' }
        } else {
            tvFirstThCol.text = forumPost.name.trim { it <= ' ' }
        }
        tvFirstThCol.setTextColor(Color.WHITE)
        tvFirstThCol.gravity = Gravity.START
        tvFirstThCol.isSingleLine = true
        tvFirstThCol.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        tvSecondThCol.text = forumPost.created
        tvSecondThCol.setTextColor(Color.LTGRAY)
        tvSecondThCol.gravity = Gravity.END
        tvSecondThCol.isSingleLine = true
        tvSecondThCol.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        rowForumPostHeader.addView(tvFirstThCol)
        rowForumPostHeader.addView(tvSecondThCol)
        tl.addView(rowForumPostHeader)

        // Forum-Post
        val tvForumPost = TextView(this)
        tvForumPost.text = forumPost.text
        tvForumPost.setBackgroundColor(Color.BLACK)
        tvForumPost.setTextColor(Color.WHITE)
        tvForumPost.setPadding(10, 10, 10, 40)
        tvForumPost.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        tl.addView(tvForumPost)

        return tl
    }

    override fun onResume() {
        super.onResume()
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(this)
        }
    }

    override fun onStop() {
        Reachability.unregister(this)
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (tools.member != null) {
            menuInflater.inflate(R.menu.signout_menu, menu)
        } else {
            menuInflater.inflate(R.menu.signin_menu, menu)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home, R.id.action_cheatview -> {
                onBackPressed()
                true
            }
            R.id.action_login -> {
                resultContract.launch(Intent(this, AuthenticationActivity::class.java))
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

    private fun submitForumPost(forumPost: ForumPost) {
        val call: Call<Void> = restApi.insertForum(
            tools.member.mid, cheatId, AeSimpleMD5.MD5(
                tools.member.password
            ), forumPost.text
        )
        call.enqueue(object : Callback<Void?> {
            override fun onResponse(forumPost: Call<Void?>, response: Response<Void?>) {
                val output = Intent()
                output.putExtra("newForumCount", forumCount + 1)
                setResult(RESULT_OK, output)

                editText.setText("")
                tvEmpty.visibility = View.GONE
                Toast.makeText(
                    this@CheatForumActivity,
                    R.string.forum_submit_ok,
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onFailure(call: Call<Void?>, e: Throwable) {
                Toast.makeText(
                    this@CheatForumActivity,
                    R.string.err_occurred,
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun fetchForumPosts() {
        val call = restApi.getCheatWithForum(cheatId)
        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(
                forum: Call<JsonObject>, response: Response<JsonObject>
            ) {
                val cheatAndForum = response.body()!!
                val cheatJsonObject = cheatAndForum["cheat"].asJsonObject

                updateUi(
                    cheatJsonObject.get("title").asString,
                    cheatJsonObject.get("gameName").asString,
                    cheatJsonObject.get("systemName").asString
                )

                val forumPostsAsJsonArray = cheatAndForum["forum"].asJsonArray
                val gson = GsonBuilder().create()
                val forumPosts =
                    gson.fromJson(forumPostsAsJsonArray, Array<ForumPost>::class.java).toList()

                reloadView.visibility = View.GONE
                mainLayout.removeAllViews()

                if (forumPosts.isNotEmpty()) {
                    tvEmpty.visibility = View.GONE
                    for (forumPost in forumPosts) {
                        val linearLayout = createForumPosts(forumPost)
                        mainLayout.addView(
                            linearLayout,
                            TableLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        )
                    }
                } else {
                    tvEmpty.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<JsonObject>, e: Throwable) {
                tvEmpty.visibility = View.VISIBLE
            }
        })
    }

    private fun updateUi(cheatTitle: String, gameName: String, systemName: String) {
        tvCheatTitle.text = getString(
            R.string.text_before_and_in_braces, cheatTitle, getString(R.string.forum)
        )
        supportActionBar!!.title = gameName
        supportActionBar!!.subtitle = systemName
    }
}