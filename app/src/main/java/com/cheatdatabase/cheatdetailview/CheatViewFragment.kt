package com.cheatdatabase.cheatdetailview

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cheatdatabase.R
import com.cheatdatabase.adapters.CheatViewGalleryListAdapter
import com.cheatdatabase.callbacks.CheatViewGalleryImageClickListener
import com.cheatdatabase.data.model.Cheat
import com.cheatdatabase.data.model.Game
import com.cheatdatabase.data.model.Screenshot
import com.cheatdatabase.databinding.FragmentCheatDetailViewBinding
import com.cheatdatabase.helpers.Konstanten
import com.cheatdatabase.helpers.Reachability
import com.cheatdatabase.helpers.Tools
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

/**
 * Cheat Detail View Fragment.
 *
 * @author Dominik Erbsland
 * @version 1.2
 */
@AndroidEntryPoint
class CheatViewFragment : Fragment(), CheatViewGalleryImageClickListener {

    private var _binding: FragmentCheatDetailViewBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var tools: Tools

    lateinit var mainLayout: LinearLayout
    lateinit var mainTable: TableLayout
    lateinit var tvCheatText: TextView
    lateinit var tvTextBeforeTable: TextView
    lateinit var tvCheatTitle: TextView
    lateinit var tvSwipeHorizontallyInfoText: TextView
    lateinit var galleryRecyclerView: RecyclerView
    lateinit var progressBar: ProgressBar
    lateinit var reloadView: ImageView

    private lateinit var cheatObj: Cheat
    private var cheatList: List<Cheat>?
    private var game: Game? = null
    private var offset = 0

    private var cheatViewPageIndicatorActivity: CheatViewPageIndicatorActivity? = null

    companion object {
        private const val TAG = "CheatViewFragment"

        @JvmStatic
        fun newInstance(gameObj: Game, offset: Int): CheatViewFragment {
            val cheatViewFragment = CheatViewFragment()
            cheatViewFragment.game = gameObj
            cheatViewFragment.offset = offset
            return cheatViewFragment
        }
    }

    init {
        cheatList = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If the screen has been rotated we re-set the values
        if (savedInstanceState != null) {
            game = savedInstanceState.getParcelable("game")
            offset = savedInstanceState.getInt("offset")
        }
        cheatViewPageIndicatorActivity = activity as CheatViewPageIndicatorActivity?
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("game", game)
        outState.putInt("offset", offset)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheatDetailViewBinding.inflate(inflater, container, false)

        mainLayout = binding.mainLayout
        mainTable = binding.tableCheatListMain
        tvCheatText = binding.cheatContent
        tvTextBeforeTable = binding.textCheatBeforeTable
        tvCheatTitle = binding.textCheatTitle
        tvSwipeHorizontallyInfoText = binding.galleryInfo
        galleryRecyclerView = binding.galleryRecyclerView
        reloadView = binding.reload
        progressBar = binding.progressBar

        reloadView.setOnClickListener { clickReload() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (cheatList != null && game != null) {
            cheatList = game?.cheatList
            cheatObj = cheatList!![offset]
            tvCheatTitle.text = cheatObj.cheatTitle
            tvTextBeforeTable.visibility = View.VISIBLE
            tvSwipeHorizontallyInfoText.visibility = View.INVISIBLE
            progressBar.visibility = View.INVISIBLE
            if (Reachability.reachability.isReachable) {
                onlineContent
            } else {
                reloadView.visibility = View.VISIBLE
                Toast.makeText(
                    cheatViewPageIndicatorActivity,
                    R.string.no_internet,
                    Toast.LENGTH_SHORT
                ).show()
            }
            countForumPosts()
        } else {
            tools.showSnackbar(mainLayout, getString(R.string.err_data_not_accessible))
        }
    }

    private fun clickReload() {
        if (Reachability.reachability.isReachable) {
            onlineContent
        } else {
            Toast.makeText(cheatViewPageIndicatorActivity, R.string.no_internet, Toast.LENGTH_SHORT)
                .show()
        }
    }

    /**
     * If the user came from the search results the cheat-text might not be
     * complete (trimmed for the search results) and therefore has to be
     * re-fetched in a background process.
     */
    private val onlineContent: Unit
        get() {
            reloadView.visibility = View.GONE

            // Get thumbnails if there are screenshots.
            if (cheatObj.hasScreenshots()) {
                val cheatViewGalleryListAdapter = CheatViewGalleryListAdapter()
                cheatViewGalleryListAdapter.setScreenshotList(cheatObj.screenshotList)
                cheatViewGalleryListAdapter.setClickListener(this)
                galleryRecyclerView.adapter = cheatViewGalleryListAdapter
                val gridLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(
                    cheatViewPageIndicatorActivity,
                    2,
                    GridLayoutManager.HORIZONTAL,
                    false
                )
                galleryRecyclerView.layoutManager = gridLayoutManager
                galleryRecyclerView.visibility = View.VISIBLE
                if (cheatObj.screenshotList == null || cheatObj.screenshotList.size <= 3) {
                    tvSwipeHorizontallyInfoText.visibility = View.GONE
                } else {
                    tvSwipeHorizontallyInfoText.visibility = View.VISIBLE
                }
            } else {
                tvSwipeHorizontallyInfoText.visibility = View.GONE
                galleryRecyclerView.visibility = View.GONE
            }
            /**
             * If the user came from the search results the cheat-text might not be
             * complete (trimmed for the search results) and therefore has to be
             * re-fetched in a background process.
             */
            if (cheatObj.cheatText == null || cheatObj.cheatText.length < 10) {
                progressBar.visibility = View.VISIBLE
                cheatBody
            } else {
                populateView()
            }
            tools.putString("cheat$offset", Gson().toJson(cheatObj))
        }

    private fun populateView() {
        try {
            if (cheatObj.cheatText.contains("</td>")) {
                fillTableContent()
            } else {
                fillSimpleContent()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Cheat " + cheatObj.cheatId + " contains(</td>) - Error creating table")
            fillSimpleContent()
        }
    }

    private fun fillTableContent() {
        mainTable.visibility = View.VISIBLE
        mainTable.isHorizontalScrollBarEnabled = true

        // Cheat text before the table
        var textBeforeTable: Array<String>?

        // Some cheats start right with a table
        if (cheatObj.cheatText.startsWith("<br><table")) {
            tvTextBeforeTable.visibility = View.GONE
        } else {
            textBeforeTable = cheatObj.cheatText.split("<br><br>").toTypedArray()
            if (textBeforeTable[0].trim { it <= ' ' }.length > 2) {
                tvTextBeforeTable.text =
                    textBeforeTable[0].replace("<br>".toRegex(), "\n").trim { it <= ' ' }
            }
        }
        val trs: Array<String> =
            cheatObj.cheatText.split("</tr><tr valign='top'>").toTypedArray()

        // Check, ob die Tabelle ein TH Element besitzt.
        var firstTag = "th"
        if (!trs[0].contains("</$firstTag>")) {
            firstTag = "td"
        }
        val ths = trs[0].split("</$firstTag><$firstTag>").toTypedArray()
        val th1 = ths[0].split("<$firstTag>").toTypedArray()
        val th2 = ths[1].split("</$firstTag>").toTypedArray()
        val firstThColumn = "<b>" + th1[1].trim { it <= ' ' } + "</b>"
        val secondThColumn = "<b>" + th2[0].trim { it <= ' ' } + "</b>"

        /* Create a new row to be added. */
        val trTh = TableRow(cheatViewPageIndicatorActivity)
        trTh.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val tvFirstThCol = TextView(cheatViewPageIndicatorActivity)
        tvFirstThCol.text = Html.fromHtml(firstThColumn)
        tvFirstThCol.setPadding(1, 1, 5, 1)
        tvFirstThCol.minimumWidth = Konstanten.TABLE_ROW_MINIMUM_WIDTH
        tvFirstThCol.setTextAppearance(cheatViewPageIndicatorActivity, R.style.NormalText)
        trTh.addView(tvFirstThCol)
        val tvSecondThCol = TextView(cheatViewPageIndicatorActivity)
        tvSecondThCol.text = Html.fromHtml(secondThColumn)
        tvSecondThCol.setPadding(5, 1, 1, 1)
        tvSecondThCol.setTextAppearance(context, R.style.NormalText)
        trTh.addView(tvSecondThCol)

        /* Add row to TableLayout. */mainTable.addView(
            trTh,
            TableLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        for (i in 1 until trs.size) {
            val tds = trs[i].split("</td><td>").toTypedArray()
            val td1 = tds[0].split("<td>").toTypedArray()
            val td2 = tds[1].split("</td>").toTypedArray()
            val firstTdColumn = td1[1].replace("<br>".toRegex(), "\n").trim { it <= ' ' }
            val secondTdColumn = td2[0].replace("<br>".toRegex(), "\n").trim { it <= ' ' }

            /* Create a new row to be added. */
            val trTd = TableRow(cheatViewPageIndicatorActivity)
            trTd.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val tvFirstTdCol = TextView(cheatViewPageIndicatorActivity)
            tvFirstTdCol.isSingleLine = false
            tvFirstTdCol.text = firstTdColumn
            tvFirstTdCol.setPadding(1, 1, 10, 1)
            tvFirstTdCol.minimumWidth = Konstanten.TABLE_ROW_MINIMUM_WIDTH
            tvFirstTdCol.setTextAppearance(context, R.style.NormalText)
            trTd.addView(tvFirstTdCol)
            val tvSecondTdCol = TextView(cheatViewPageIndicatorActivity)
            tvSecondTdCol.isSingleLine = false
            tvSecondTdCol.text = secondTdColumn
            tvSecondTdCol.canScrollHorizontally(1)
            tvSecondTdCol.setPadding(10, 1, 30, 1)
            tvSecondTdCol.setTextAppearance(context, R.style.NormalText)
            trTd.addView(tvSecondTdCol)

            /* Add row to TableLayout. */mainTable.addView(
                trTd,
                TableLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
        mainTable.setOnClickListener { displayTableInWebview() }
        progressBar.visibility = View.GONE
    }

    private fun fillSimpleContent() {
        mainTable.visibility = View.GONE
        tvTextBeforeTable.visibility = View.GONE
        if (cheatObj != null) {
            val styledText: CharSequence = Html.fromHtml(cheatObj.cheatText)
            tvCheatText.text = styledText
            if (cheatObj.isWalkthroughFormat) {
                tvCheatText.setTextAppearance(context, R.style.WalkthroughText)
            }
        }
        progressBar.visibility = View.GONE
    }

    private fun displayTableInWebview() {
        val madb = MaterialAlertDialogBuilder(requireContext())
        madb.setTitle(R.string.report_cheat_title)
            .setView(R.layout.webview_container)
            .setPositiveButton(R.string.close) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
        val materialDialog = madb.create()
        materialDialog.show()

        val webview = materialDialog.findViewById<WebView>(R.id.webview)
        webview?.loadDataWithBaseURL("", cheatObj.cheatText, "text/html", "UTF-8", "")
    }

    private val cheatBody: Unit
        get() {
            progressBar.visibility = View.VISIBLE
            val call: Call<Cheat> =
                cheatViewPageIndicatorActivity!!.getRestApi().getCheatById(cheatObj.cheatId)
            call.enqueue(object : Callback<Cheat?> {
                override fun onResponse(metaInfo: Call<Cheat?>, response: Response<Cheat?>) {
                    setCheatText(response.body())
                }

                override fun onFailure(call: Call<Cheat?>, e: Throwable) {
                    tools.showSnackbar(
                        mainLayout, getString(R.string.err_somethings_wrong), 5000
                    )
                }
            })
        }

    private fun countForumPosts() {
        val call: Call<JsonObject> =
            cheatViewPageIndicatorActivity!!.getRestApi().countForumPosts(cheatObj.cheatId)
        call.enqueue(object : Callback<JsonObject?> {
            override fun onResponse(forum: Call<JsonObject?>, response: Response<JsonObject?>) {
                val forumPostsCount: JsonObject? = response.body()
                val forumCount: Int? = forumPostsCount?.get("forumCount")?.asInt
                if (forumCount != null) {
                    cheatObj.forumCount = forumCount
                }
            }

            override fun onFailure(call: Call<JsonObject?>, e: Throwable) {
                // Do nothing
            }
        })
    }

    private fun setCheatText(fullCheatText: Cheat?) {
        if (fullCheatText != null && fullCheatText.cheatText.length > 1) {
            cheatObj.cheatText = fullCheatText.cheatText
            populateView()
        }
    }


    override fun onScreenshotClicked(screenshot: Screenshot, position: Int) {
//        StfalconImageViewer.Builder(            cheatViewPageIndicatorActivity,            cheatObj.screenshotList        )
//        { imageView: ImageView?, image: Screenshot ->
//            Picasso.get().load(image.fullPath).placeholder(R.drawable.image_placeholder)
//                .into(imageView)
//        }.withStartPosition(position).show()

        // TODO replace with some image viewer at some point...
        val intent = Intent(activity, SingleImageViewerActivity::class.java)
        intent.putExtra("image_full_path", screenshot.fullPath)
        intent.putExtra("cheat_title", cheatObj.cheatTitle)
        startActivity(intent)
    }


}