package com.cheatdatabase.holders

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.cheatdatabase.R
import com.cheatdatabase.data.model.Cheat
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.databinding.ListrowMemberCheatItemBinding
import com.cheatdatabase.helpers.Konstanten

class MemberCheatsListViewItemHolderK(
    val binding: ListrowMemberCheatItemBinding,
    val context: Context
) :
    RecyclerView.ViewHolder(binding.root) {

    private var member: Member? = null

    fun setCheat(cheat: Cheat) {

        with(binding) {
//            gameName.text = "${cheat.game.gameName} (${cheat.system.systemName})"
            gameName.text = context.getString(
                R.string.game_name_system_name,
                cheat.game.gameName,
                cheat.system.systemName
            )
            cheatTitle.text = cheat.cheatTitle

            if (cheat.screenshotList.size > 0) {
                imagesIcon.visibility = View.VISIBLE
            } else {
                imagesIcon.visibility = View.GONE
            }

            if (cheat.languageId == Konstanten.GERMAN) {
                germanFlag.visibility = View.VISIBLE
            } else {
                germanFlag.visibility = View.GONE
            }

            // TODO FIXME add this again when continue work on the "edit my cheat" functionality
//        if (member.getMid() == cheat.getSubmittingMember().getMid()) {
//            edit.setVisibility(View.VISIBLE);
//        } else {
//            edit.setVisibility(View.GONE);
//        }
            editImageButton.visibility = View.GONE
        }
    }

    fun setLoggedInMember(member: Member?) {
        this.member = member
    }
}