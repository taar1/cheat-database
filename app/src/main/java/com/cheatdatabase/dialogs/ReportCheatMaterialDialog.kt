package com.cheatdatabase.dialogs

import android.app.Activity
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.cheatdatabase.R
import com.cheatdatabase.data.RetrofitClientInstance
import com.cheatdatabase.data.model.Cheat
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.helpers.Tools
import com.cheatdatabase.rest.RestApi
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Cheat Reporting Dialog.
 */
class ReportCheatMaterialDialog(
    val activity: Activity,
    val cheat: Cheat,
    val member: Member,
    val view: View,
    val tools: Tools
) {

    private var madb: MaterialAlertDialogBuilder =
        MaterialAlertDialogBuilder(activity, R.style.SimpleAlertDialog)
    private var materialDialog: AlertDialog

    private val restApi: RestApi =
        RetrofitClientInstance.getRetrofitInstance().create(RestApi::class.java)

    init {
        val reasons: Array<String> = activity.resources.getStringArray(R.array.report_reasons)

        madb.setTitle(R.string.report_cheat_title)
            .setItems(reasons) { _, which ->
                reportCheat(cheat.cheatId, member.mid, reasons[which])
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        materialDialog = madb.create()
        materialDialog.show()
    }

    private fun reportCheat(cheatId: Int, memberId: Int, reason: String?) {
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
            materialDialog.dismiss()
        } else {
            tools.showSnackbar(view, activity.getString(R.string.err_occurred))
        }
    }
}