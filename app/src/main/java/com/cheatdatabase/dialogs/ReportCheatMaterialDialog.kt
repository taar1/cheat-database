package com.cheatdatabase.dialogs

import android.app.Activity
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.cheatdatabase.R
import com.cheatdatabase.data.RetrofitClientInstance
import com.cheatdatabase.data.model.Cheat
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.helpers.Tools
import com.cheatdatabase.rest.RestApi
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Material Design Cheat Reporting Dialog.
 */
class ReportCheatMaterialDialog(
    val activity: Activity,
    val cheat: Cheat,
    val member: Member,
    val view: View,
    val tools: Tools
) {

    private val mMaterialDialog: MaterialDialog = MaterialDialog(activity)
    private val restApi: RestApi =
        RetrofitClientInstance.getRetrofitInstance().create(RestApi::class.java)

    init {
        val reasons: Array<String> = activity.resources.getStringArray(R.array.report_reasons)

        mMaterialDialog.apply {
            title(R.string.report_cheat_title)
            listItemsSingleChoice(
                R.array.report_reasons,
                waitForPositiveButton = false,
                selection = { _, index, _ ->
                    reportCheat(cheat.cheatId, member.mid, reasons[index])
                }
            )
            negativeButton(R.string.cancel) { dialog ->
                dialog.dismiss()
            }
            show()
        }
    }

    fun reportCheat(cheatId: Int, memberId: Int, reason: String?) {
        val call: Call<JsonObject> = restApi.reportCheat(cheatId, memberId, reason)
        call.enqueue(object : Callback<JsonObject?> {
            override fun onResponse(forum: Call<JsonObject?>, response: Response<JsonObject?>) {
                val cheatRatingResponse: JsonObject? = response.body()
                val cheatRatingResponseValue: String =
                    cheatRatingResponse?.get("returnValue")!!.asString // inserted|updated|invalid_parameters

                if (cheatRatingResponseValue.equals(
                        "inserted", ignoreCase = true
                    ) || cheatRatingResponseValue.equals("updated", ignoreCase = true)
                ) {
                    postReporting(true)
                } else {
                    postReporting(false)
                }
            }

            override fun onFailure(call: Call<JsonObject?>, e: Throwable) {
                postReporting(false)
            }
        })
    }

    private fun postReporting(isSuccess: Boolean) {
        if (isSuccess) {
            tools.showSnackbar(view, activity.getString(R.string.thanks_for_reporting))
            mMaterialDialog.dismiss()
        } else {
            tools.showSnackbar(view, activity.getString(R.string.err_occurred))
        }
    }


}