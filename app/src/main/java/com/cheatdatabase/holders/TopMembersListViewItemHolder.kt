package com.cheatdatabase.holders

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cheatdatabase.R
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.databinding.TopmembersListItemBinding
import com.cheatdatabase.helpers.Konstanten
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class TopMembersListViewItemHolder(val binding: TopmembersListItemBinding, val context: Context) :
    RecyclerView.ViewHolder(binding.root) {

    val memberName: TextView = binding.memberName
    val cheatCount: TextView = binding.cheatCount
    val memberMessage: TextView = binding.memberMessage
    val website: TextView = binding.website
    val avatar: CircleImageView = binding.avatar

    fun updateUI(member: Member) {
        Picasso.get().load(Konstanten.WEBDIR_MEMBER_AVATAR + member.mid)
            .placeholder(R.drawable.avatar).into(avatar)

        memberName.text = member.username.uppercase()
        cheatCount.text =
            context.getString(R.string.top_members_cheats_count, member.cheatSubmissionCount)

        if (member.website.length > 1) {
            website.text = member.website
            website.visibility = View.VISIBLE
        } else {
            website.visibility = View.GONE
        }

        if (member.profileText.length > 1) {
            memberMessage.text = context.getString(
                R.string.quoted_text,
                member.profileText.replace("\\\\".toRegex(), "").trim { it <= ' ' })
            memberMessage.visibility = View.VISIBLE
        } else {
            memberMessage.visibility = View.GONE
        }
    }
}