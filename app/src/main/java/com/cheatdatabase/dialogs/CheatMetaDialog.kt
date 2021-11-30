package com.cheatdatabase.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.cheatdatabase.R
import com.cheatdatabase.activity.CheatsByMemberListActivity
import com.cheatdatabase.data.RetrofitClientInstance
import com.cheatdatabase.data.model.Cheat
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.helpers.Tools
import com.cheatdatabase.rest.RestApi
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat

@SuppressLint("SimpleDateFormat")
class CheatMetaDialog(
    context: Context,
    private val cheat: Cheat,
    private val view: View,
    private val tools: Tools
) : Dialog(
    context
), View.OnClickListener {

    private val mMaterialDialog: MaterialDialog = MaterialDialog(context)
    private val restApi: RestApi =
        RetrofitClientInstance.getRetrofitInstance().create(RestApi::class.java)

    private var member: Member?
    private lateinit var outerLayout: ConstraintLayout
    private lateinit var averageRatingTitle: TextView
    private lateinit var averageRatingText: TextView
    private lateinit var lifetimeViewsText: TextView
    private lateinit var submissionDateText: TextView
    private lateinit var submittedByText: TextView
    private lateinit var viewsTodayText: TextView
    private lateinit var totalCheatsByMemberText: TextView
    private lateinit var showAllCheatsByMember: TextView
    private lateinit var websiteTitle: TextView
    private lateinit var websiteText: TextView
    private lateinit var lifetimeViewsTitle: TextView
    private lateinit var submissionDateTitle: TextView
    private lateinit var submittedByTitle: TextView
    private lateinit var viewsTodayTitle: TextView
    private lateinit var totalCheatsByMemberTitle: TextView
    private lateinit var divider3: View
    private lateinit var divider4: View
    private lateinit var divider5: View
    private lateinit var divider6: View

    companion object {
        private const val TAG = "CheatMetaDialog"
    }

    init {
        mMaterialDialog.apply {
            title(R.string.title_cheat_details)
            customView(R.layout.layout_cheatview_meta_dialog)
            positiveButton(R.string.ok)
            cancelable(true)
            show()
        }

        member = cheat.submittingMember

        bind(mMaterialDialog.view)

        loadMetaData()
    }

    private fun loadMetaData() {
        val call: Call<JsonObject> = restApi.getCheatMetaById(cheat.cheatId)
        call.enqueue(object : Callback<JsonObject?> {
            override fun onResponse(metaInfo: Call<JsonObject?>, response: Response<JsonObject?>) {
                val metaInfoData: JsonObject? = response.body()
                Log.d(TAG, "onResponse metaInfo: $metaInfo")


                if (metaInfoData?.get("viewsTotal") != null) {
                    cheat.viewsTotal = metaInfoData.get("viewsTotal").asInt
                }
                if (metaInfoData?.get("viewsToday") != null) {
                    cheat.viewsToday = metaInfoData.get("viewsToday").asInt
                }
                if (metaInfoData?.get("author") != null) {
                    cheat.authorName = metaInfoData.get("author").asString
                }
                if (metaInfoData?.get("created") != null) {
                    cheat.setCreated(metaInfoData.get("created").asString)
                }
                if (metaInfoData?.get("rating") != null) {
                    cheat.ratingAverage = metaInfoData.get("rating").asFloat
                }
                if (metaInfoData?.get("votes") != null) {
                    cheat.votes = metaInfoData.get("votes").asInt
                }

                member = member ?: Member()

                if (metaInfoData?.get("memberId") != null) {
                    member!!.mid = metaInfoData.get("memberId").asInt
                }
                if (metaInfoData?.get("website") != null) {
                    member!!.website = metaInfoData.get("website").asString
                }
                if (metaInfoData?.get("city") != null) {
                    member!!.city = metaInfoData.get("city").asString
                }
                cheat.setMember(member)
                updateUI()
            }

            override fun onFailure(call: Call<JsonObject?>, e: Throwable) {
                Log.e(TAG, "getCheatList onFailure: " + e.localizedMessage)
                tools.showSnackbar(view, context.getString(R.string.err_somethings_wrong), 5000)
            }
        })
    }

    private fun updateUI() {
        // 1: SUBMISSION DATE
        val dateObj = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(cheat.createdDate)
        val dateFormat = DateFormat.getDateFormat(
            context.applicationContext
        )
        val newDateStr = dateFormat.format(dateObj!!)
        submissionDateText.text = newDateStr

        // 2: LIFETIME VIEWS
        lifetimeViewsText.text = cheat.viewsTotal.toString()

        // 3: VIEWS TODAY
        viewsTodayText.text = cheat.viewsToday.toString()
        divider3.visibility = View.GONE
        divider4.visibility = View.GONE
        divider5.visibility = View.GONE
        divider6.visibility = View.GONE

        // 4: AVERAGE CHEAT RATING
        if (cheat.votes > 0) {
            averageRatingTitle.visibility = View.VISIBLE
            averageRatingText.visibility = View.VISIBLE
            val averageRatingText = context.getString(
                R.string.meta_average_rating1,
                Math.round(cheat.ratingAverage)
            ).plus(" ").plus(
                context.getString(
                    R.string.meta_average_rating2,
                    cheat.votes.toString()
                )
            )

            this.averageRatingText.text = averageRatingText
            divider3.visibility = View.VISIBLE
        } else {
            averageRatingTitle.visibility = View.GONE
            averageRatingText.visibility = View.GONE
        }


        // 5: SUBMITTED BY
        if (!this.member?.username.isNullOrEmpty()) {
            submittedByTitle.visibility = View.VISIBLE
            submittedByText.visibility = View.VISIBLE
            submittedByText.text = member!!.username
            submittedByText.setOnClickListener(this@CheatMetaDialog)
            divider4.visibility = View.VISIBLE
        } else {
            submittedByTitle.visibility = View.GONE
            submittedByText.visibility = View.GONE
        }

        // 6: AMOUNT OF CHEATS BY MEMBER
        member?.cheatSubmissionCount?.let {
            if (it > 0) {
                totalCheatsByMemberTitle.visibility = View.VISIBLE
                totalCheatsByMemberText.visibility = View.VISIBLE
                showAllCheatsByMember.visibility = View.VISIBLE
                totalCheatsByMemberText.text = it.toString()
                showAllCheatsByMember.text =
                    "[".plus(context.getString(R.string.meta_show_all_cheats)).plus("]")
                showAllCheatsByMember.setOnClickListener(this@CheatMetaDialog)
                divider5.visibility = View.VISIBLE
            } else {
                totalCheatsByMemberTitle.visibility = View.GONE
                totalCheatsByMemberText.visibility = View.GONE
                showAllCheatsByMember.visibility = View.GONE
            }
        }

        // 7: WEBSITE
        if (member!!.website != null && member!!.website.length > 3 && member!!.username != null) {
            websiteTitle.visibility = View.VISIBLE
            websiteText.visibility = View.VISIBLE
            websiteText.text = member!!.website
            websiteTitle.text = member!!.username
                .plus("'s ")
                .plus(context.getString(R.string.meta_member_homepage))
            websiteText.setOnClickListener(this@CheatMetaDialog)
            divider6.visibility = View.VISIBLE
        } else {
            websiteTitle.visibility = View.GONE
            websiteText.visibility = View.GONE
        }
        outerLayout.visibility = View.VISIBLE
    }

    override fun onClick(v: View) {
        if (v === showAllCheatsByMember) {
            val explicitIntent = Intent(context, CheatsByMemberListActivity::class.java)
            explicitIntent.putExtra("memberObj", member)
            context.startActivity(explicitIntent)
        } else if (v === websiteText) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(member!!.website)
            context.startActivity(intent)
        }
    }

    private fun bind(dialogView: View) {
        outerLayout = dialogView.findViewById(R.id.outer_layout)
        averageRatingTitle = dialogView.findViewById(R.id.average_rating_title)
        averageRatingText = dialogView.findViewById(R.id.average_rating_text)
        lifetimeViewsTitle = dialogView.findViewById(R.id.lifetime_views_title)
        lifetimeViewsText = dialogView.findViewById(R.id.lifetime_views_text)
        submissionDateTitle = dialogView.findViewById(R.id.submission_date_title)
        submissionDateText = dialogView.findViewById(R.id.submission_date_text)
        submittedByTitle = dialogView.findViewById(R.id.submitted_by_title)
        submittedByText = dialogView.findViewById(R.id.submitted_by_text)
        viewsTodayTitle = dialogView.findViewById(R.id.views_today_title)
        viewsTodayText = dialogView.findViewById(R.id.views_today_text)
        totalCheatsByMemberTitle =
            dialogView.findViewById(R.id.total_cheats_by_member_title)
        totalCheatsByMemberText =
            dialogView.findViewById(R.id.total_cheats_by_member_text)
        showAllCheatsByMember = dialogView.findViewById(R.id.show_all_cheats_by_member)
        websiteTitle = dialogView.findViewById(R.id.website_title)
        websiteText = dialogView.findViewById(R.id.website_text)
        divider3 = dialogView.findViewById(R.id.divider_3)
        divider4 = dialogView.findViewById(R.id.divider_4)
        divider5 = dialogView.findViewById(R.id.divider_5)
        divider6 = dialogView.findViewById(R.id.divider_6)

        lifetimeViewsText.setOnClickListener(this)
        viewsTodayText.setOnClickListener(this)
        totalCheatsByMemberText.setOnClickListener(this)
        showAllCheatsByMember.setOnClickListener(this)
        submissionDateText.setOnClickListener(this)
    }


}