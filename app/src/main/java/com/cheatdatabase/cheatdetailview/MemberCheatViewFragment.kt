package com.cheatdatabase.cheatdetailview

import android.content.SharedPreferences
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
import com.cheatdatabase.data.helper.CheatArrayHolder
import com.cheatdatabase.data.model.Cheat
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.data.model.Screenshot
import com.cheatdatabase.databinding.FragmentMemberCheatDetailBinding
import com.cheatdatabase.helpers.Konstanten
import com.cheatdatabase.helpers.Tools
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@AndroidEntryPoint
class MemberCheatViewFragment : Fragment(), CheatViewGalleryImageClickListener {

    private var _binding: FragmentMemberCheatDetailBinding? = null
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

    lateinit var cheatObj: Cheat
    private var cheats: List<Cheat>? = null
    private var offset = 0
    private var member: Member? = null
    private var cheatViewPageIndicatorActivity: MemberCheatViewPageIndicator? = null

    lateinit var editor: SharedPreferences.Editor


    companion object {
        private const val TAG = "MemberCheatViewFragment"

        @JvmStatic
        fun newInstance(
            cheats: List<Cheat>?, offset: Int
        ): MemberCheatViewFragment {
            val fragment = MemberCheatViewFragment()
            fragment.cheats = cheats
            fragment.offset = offset
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If the screen has been rotated we re-set the values
        if (savedInstanceState != null) {
            val cheatArrayHolder: CheatArrayHolder? =
                savedInstanceState.getParcelable("cheatArrayHolder")
            cheats = cheatArrayHolder?.cheatList
            offset = savedInstanceState.getInt("offset")
        }
        cheatViewPageIndicatorActivity = activity as MemberCheatViewPageIndicator?
        val settings = tools.sharedPreferences
        editor = settings.edit()

        member = Gson().fromJson(
            settings.getString(Konstanten.MEMBER_OBJECT, null),
            Member::class.java
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemberCheatDetailBinding.inflate(inflater, container, false)

        mainLayout = binding.mainLayout
        mainTable = binding.tableCheatListMain
        tvCheatText = binding.cheatContent
        tvTextBeforeTable = binding.textCheatBeforeTable
        tvCheatTitle = binding.textCheatTitle
        tvSwipeHorizontallyInfoText = binding.galleryInfo
        galleryRecyclerView = binding.galleryRecyclerView
        progressBar = binding.progressBar

        //reloadView.setOnClickListener { clickReload() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cheatObj = cheats!![offset]
        cheatRating
        tvCheatTitle.text = cheatObj.cheatTitle

        tvTextBeforeTable.visibility = View.VISIBLE
        tvSwipeHorizontallyInfoText.visibility = View.INVISIBLE
        progressBar.visibility = View.INVISIBLE

        for (s in cheatObj.screenshotList) {
            Log.d(TAG, "XXXXX onCreateView: screenshot: " + s.fullPath)
        }
        /**
         * Get thumbnails if there are screenshots.
         */
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
            if (cheatObj.screenshotList == null || cheatObj.screenshotList.size < 3) {
                tvSwipeHorizontallyInfoText.visibility = View.GONE
            } else {
                tvSwipeHorizontallyInfoText.visibility = View.VISIBLE
            }
            progressBar.visibility = View.VISIBLE
        } else {
            tvSwipeHorizontallyInfoText.visibility = View.GONE
            galleryRecyclerView.visibility = View.GONE
        }
        /**
         * If the user came from the search results the cheat-text might not
         * be complete (trimmed for the search results) and therefore has to
         * be re-fetched in a background process.
         */
        if (cheatObj.cheatText == null || cheatObj.cheatText.length < 10) {
            progressBar.visibility = View.VISIBLE
            cheatBody
        } else {
            progressBar.visibility = View.GONE
            populateView()
        }
        editor.putString("cheat$offset", Gson().toJson(cheatObj))
        editor.apply()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("cheatArrayHolder", CheatArrayHolder(cheats))
        outState.putInt("offset", offset)
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

        // Text before the table
        val textBeforeTable: Array<String>

        // Einige tabellarische Cheats beginnen direkt mit der Tabelle
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
        tvFirstThCol.setTextAppearance(R.style.NormalText)
        trTh.addView(tvFirstThCol)
        val tvSecondThCol = TextView(cheatViewPageIndicatorActivity)
        tvSecondThCol.text = Html.fromHtml(secondThColumn)
        tvSecondThCol.setPadding(5, 1, 1, 1)
        tvSecondThCol.setTextAppearance(R.style.NormalText)
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
            tvFirstTdCol.text = firstTdColumn
            tvFirstTdCol.setPadding(1, 1, 10, 1)
            tvFirstTdCol.minimumWidth = Konstanten.TABLE_ROW_MINIMUM_WIDTH
            tvFirstTdCol.setTextAppearance(R.style.NormalText)
            trTd.addView(tvFirstTdCol)
            val tvSecondTdCol = TextView(cheatViewPageIndicatorActivity)
            tvSecondTdCol.isSingleLine = false
            tvSecondTdCol.text = secondTdColumn
            tvSecondTdCol.canScrollHorizontally(1)
            tvSecondTdCol.setPadding(10, 1, 30, 1)
            tvSecondTdCol.setTextAppearance(R.style.NormalText)
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
    }

    private fun fillSimpleContent() {
        mainTable.visibility = View.GONE
        tvTextBeforeTable.visibility = View.GONE
        val styledText: CharSequence = Html.fromHtml(cheatObj.cheatText)
        tvCheatText.text = styledText
        if (cheatObj.isWalkthroughFormat) {
            tvCheatText.setTextAppearance(R.style.WalkthroughText)
        }
    }

    private fun displayTableInWebview() {
        val madb = MaterialAlertDialogBuilder(requireContext())
        madb.setView(R.layout.webview_container)
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
        private get() {
            val call: Call<Cheat> =
                cheatViewPageIndicatorActivity!!.restApi.getCheatById(cheatObj.cheatId)
            call.enqueue(object : Callback<Cheat?> {
                override fun onResponse(metaInfo: Call<Cheat?>, response: Response<Cheat?>) {
                    updateUI(response.body())
                }

                override fun onFailure(call: Call<Cheat?>, e: Throwable) {
                    Log.e(TAG, "getCheatBody onFailure: " + e.localizedMessage)
                    tools.showSnackbar(
                        mainLayout,
                        requireContext().getString(R.string.err_somethings_wrong),
                        5000
                    )
                }
            })
        }

    private fun updateUI(cheat: Cheat?) {
        if (cheat?.style == Konstanten.CHEAT_TEXT_FORMAT_WALKTHROUGH) {
            cheatObj.isWalkthroughFormat = true
        }
        progressBar.visibility = View.GONE
        cheatObj.cheatText = cheat?.cheatText
        populateView()
    }

    private val cheatRating: Unit
        get() {
            if (member != null) {
                val call: Call<JsonObject> =
                    cheatViewPageIndicatorActivity!!.restApi.getMemberRatingByCheatId(
                        member!!.mid, cheatObj.cheatId
                    )
                call.enqueue(object : Callback<JsonObject?> {
                    override fun onResponse(
                        ratingInfo: Call<JsonObject?>,
                        response: Response<JsonObject?>
                    ) {
                        val ratingJsonObj: JsonObject? = response.body()
                        Log.d(
                            TAG, "getCheatRating SUCCESS: " + ratingJsonObj?.get("rating")?.asFloat
                        )
                        val cheatRating: Float? = ratingJsonObj?.get("rating")?.asFloat
                        if (cheatRating != null && cheatRating > 0) {
                            cheatViewPageIndicatorActivity!!.setRating(offset, cheatRating)
                        }
                    }

                    override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    }
                })
            }
        }

    // TODO FIXME gallery viewer ersetzen
    // TODO FIXME gallery viewer ersetzen
    // TODO FIXME gallery viewer ersetzen
    // TODO FIXME gallery viewer ersetzen
    override fun onScreenshotClicked(screenshot: Screenshot, position: Int) {
//        StfalconImageViewer.Builder(
//            cheatViewPageIndicatorActivity,
//            cheatObj.screenshotList
//        ) { imageView: ImageView?, image: Screenshot ->
//            Picasso.get().load(image.fullPath).placeholder(R.drawable.image_placeholder)
//                .into(imageView)
//        }.withStartPosition(position).show()
    }


}