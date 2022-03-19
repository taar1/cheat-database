package com.cheatdatabase.activity

import android.app.SearchManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.applovin.adview.AppLovinAdView
import com.cheatdatabase.R
import com.cheatdatabase.callbacks.GenericCallback
import com.cheatdatabase.callbacks.OnCheatRated
import com.cheatdatabase.data.model.Cheat
import com.cheatdatabase.data.model.ForumPost
import com.cheatdatabase.data.model.Game
import com.cheatdatabase.databinding.ActivityCheatForumBinding
import com.cheatdatabase.dialogs.CheatMetaDialog
import com.cheatdatabase.dialogs.RateCheatMaterialDialog
import com.cheatdatabase.dialogs.ReportCheatMaterialDialog
import com.cheatdatabase.events.CheatRatingFinishedEvent
import com.cheatdatabase.helpers.AeSimpleMD5
import com.cheatdatabase.helpers.Konstanten
import com.cheatdatabase.helpers.Reachability
import com.cheatdatabase.helpers.Tools
import com.cheatdatabase.rest.RestApi
import dagger.hilt.android.AndroidEntryPoint
import needle.Needle
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.inject.Inject

/**
 * Displaying the forum of one cheat.
 */
@AndroidEntryPoint
class CheatForumActivity : AppCompatActivity(), GenericCallback, OnCheatRated {
    private var cheatObj: Cheat? = null
    private var gameObj: Game? = null

    @Inject
    lateinit var tools: Tools

    @Inject
    lateinit var restApi: RestApi

    lateinit var outerLayout: LinearLayout
    lateinit var mToolbar: Toolbar
    lateinit var llForumMain: LinearLayout
    lateinit var tvCheatTitle: TextView
    lateinit var tvEmpty: TextView
    lateinit var sv: ScrollView
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

        cheatObj = intent.getParcelableExtra("cheatObj")
        gameObj = intent.getParcelableExtra("gameObj")

        if (cheatObj == null || gameObj == null) {
            Toast.makeText(this, R.string.err_somethings_wrong, Toast.LENGTH_LONG).show()
            finish()
        } else {
            bindViews()
            init()
            tvCheatTitle.text = getString(
                R.string.text_before_and_in_braces,
                cheatObj!!.cheatTitle,
                getString(R.string.forum)
            )
            if (Reachability.reachability.isReachable) {
                reloadView.visibility = View.GONE
                fetchForumPosts()
            } else {
                reloadView.visibility = View.VISIBLE
                reloadView.setOnClickListener {
                    if (Reachability.reachability.isReachable) {
                        fetchForumPosts()
                    } else {
                        Toast.makeText(
                            this@CheatForumActivity,
                            R.string.no_internet,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                Toast.makeText(this@CheatForumActivity, R.string.no_internet, Toast.LENGTH_SHORT)
                    .show()
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
                    Toast.makeText(
                        this@CheatForumActivity,
                        R.string.no_internet,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun bindViews() {
        outerLayout = binding.outerLayout
        mToolbar = binding.toolbar
        llForumMain = binding.mainForum
        tvCheatTitle = binding.textCheatTitle
        tvEmpty = binding.textviewEmpty
        sv = binding.sv
        reloadView = binding.reload
        editText = binding.forumTextInput
        postButton = binding.submitButton
        appLovinAdView = binding.adContainer
    }

    private fun init() {
        if (!Reachability.isRegistered()) {
            Reachability.registerReachability(this)
        }

        setSupportActionBar(mToolbar)
        supportActionBar!!.title = gameObj?.gameName ?: ""
        supportActionBar!!.subtitle = gameObj?.systemName ?: ""
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
                llForumMain.addView(
                    createForumPosts(forumPost),
                    TableLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )

                // Führt die ScrollView bis ganz nach unten zum neusten Post
                sv.post { sv.fullScroll(View.FOCUS_DOWN) }
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
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show()
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

        // TODO hier noch programmatisch die FONT auf LATO setzen
//        tvFirstThCol.setTypeface(latoFontBold);
//        tvSecondThCol.setTypeface(latoFontBold);

        // Headerinfo of Forumpost
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
        // TODO hier noch programmatisch die FONT auf LATO setzen
//        tvForumPost.setTypeface(latoFontLight);
        tvForumPost.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        tl.addView(tvForumPost)

//        cardLayoutPortrait.addView(tl)
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
        menuInflater.inflate(R.menu.handset_forum_menu, menu)
        if (tools.member != null) {
            menuInflater.inflate(R.menu.signout_menu, menu)
        } else {
            menuInflater.inflate(R.menu.signin_menu, menu)
        }

        // Search
        menuInflater.inflate(R.menu.search_menu, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home, R.id.action_cheatview -> {
                onBackPressed()
                true
            }
            R.id.action_rate -> {
                showRatingDialog()
                true
            }
            R.id.action_add_to_favorites -> {
                tools.showSnackbar(outerLayout, getString(R.string.favorite_adding))
                var memberId = 0
                if (tools.member != null) {
                    memberId = tools.member.mid
                }
                tools.addFavorite(cheatObj, memberId, this)
                true
            }
            R.id.action_share -> {
                tools.shareCheat(cheatObj)
                true
            }
            R.id.action_metainfo -> {
                CheatMetaDialog(this@CheatForumActivity, cheatObj!!, outerLayout, tools).show()
                true
            }
            R.id.action_report -> {
                showReportDialog()
                true
            }
            R.id.action_submit_cheat -> {
                val explicitIntent = Intent(this, SubmitCheatFormActivity::class.java)
                explicitIntent.putExtra("gameObj", gameObj)
                startActivity(explicitIntent)
                true
            }
            R.id.action_login -> {
                resultContract.launch(Intent(this, LoginActivity::class.java))
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

    private fun showReportDialog() {
        if (tools.member == null || tools.member.mid == 0) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_LONG).show()
        } else {
            ReportCheatMaterialDialog(this, cheatObj!!, tools.member, outerLayout, tools)
        }
    }

    private fun showRatingDialog() {
        if (tools.member == null || tools.member.mid == 0) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_LONG).show()
        } else {
            RateCheatMaterialDialog(this, cheatObj!!, outerLayout, tools, restApi, this)
        }
    }

    private fun submitForumPost(forumPost: ForumPost) {
        val call: Call<Void>
        try {
            call = restApi.insertForum(
                tools.member.mid, cheatObj!!.cheatId, AeSimpleMD5.MD5(
                    tools.member.password
                ), forumPost.text
            )
            call.enqueue(object : Callback<Void?> {
                override fun onResponse(forumPost: Call<Void?>, response: Response<Void?>) {
                    Log.d(TAG, "submit forum post SUCCESS")
                    val output = Intent()
                    output.putExtra("newForumCount", cheatObj!!.forumCount + 1)
                    setResult(RESULT_OK, output)
                    updateUI()
                }

                override fun onFailure(call: Call<Void?>, e: Throwable) {
                    Log.e(TAG, "Submit forum post FAIL: " + e.localizedMessage)
                    Toast.makeText(
                        this@CheatForumActivity,
                        R.string.err_occurred,
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "postForumEntry: ", e)
        }
    }

    private fun updateUI() {
        Needle.onMainThread().execute {
            editText.setText("")
            tvEmpty.visibility = View.GONE
            Toast.makeText(this@CheatForumActivity, R.string.forum_submit_ok, Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun fetchForumPosts() {
        val call = restApi.getForum(
            cheatObj!!.cheatId
        )
        call.enqueue(object : Callback<List<ForumPost>> {
            override fun onResponse(
                forum: Call<List<ForumPost>>,
                response: Response<List<ForumPost>>
            ) {
                val forumThread = response.body()!!
                reloadView.visibility = View.GONE
                llForumMain.removeAllViews()
                if (forumThread.isNotEmpty()) {
                    tvEmpty.visibility = View.GONE
                    for (forumPost in forumThread) {
                        val linearLayout = createForumPosts(forumPost)
                        llForumMain.addView(
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

            override fun onFailure(call: Call<List<ForumPost>>, e: Throwable) {
                tvEmpty.visibility = View.VISIBLE
            }
        })
    }

    override fun success() {
        Log.d(TAG, "CheatForumActivity ADD FAV success: ")
        tools.showSnackbar(outerLayout, getString(R.string.add_favorite_ok))
    }

    override fun fail(e: Exception) {
        Log.d(TAG, "CheatForumActivity ADD FAV fail: ")
        tools.showSnackbar(outerLayout, getString(R.string.error_adding_favorite))
    }

    override fun onCheatRated(cheatRatingFinishedEvent: CheatRatingFinishedEvent) {
        Log.d(TAG, "OnEvent result: " + cheatRatingFinishedEvent.rating)
        cheatObj!!.memberRating = cheatRatingFinishedEvent.rating.toFloat()
    }

    companion object {
        private const val TAG = "CheatForumActivity"
    }
}