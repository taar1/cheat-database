package com.cheatdatabase.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.cheatdatabase.data.model.Cheat
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.helpers.Konstanten
import kotlinx.android.synthetic.main.listrow_member_cheat_item.view.*

class MemberCheatsListViewItemHolderK(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var member: Member? = null

    fun setCheat(cheat: Cheat) {
        itemView.gameName.text = "${cheat.game.gameName} (${cheat.system.systemName})"
        itemView.cheatTitle.text = cheat.cheatTitle

        if (cheat.screenshotList.size > 0) {
            itemView.imagesIcon.visibility = View.VISIBLE
        } else {
            itemView.imagesIcon.visibility = View.GONE
        }

        if (cheat.languageId == Konstanten.GERMAN) {
            itemView.germanFlag.visibility = View.VISIBLE
        } else {
            itemView.germanFlag.visibility = View.GONE
        }

        // TODO FIXME add this again when continue work on the "edit my cheat" functionality
//        if (member.getMid() == cheat.getSubmittingMember().getMid()) {
//            itemView.edit.setVisibility(View.VISIBLE);
//        } else {
//            itemView.edit.setVisibility(View.GONE);
//        }
        itemView.editImageButton.visibility = View.GONE
    }

    fun setLoggedInMember(member: Member?) {
        this.member = member
    }
}