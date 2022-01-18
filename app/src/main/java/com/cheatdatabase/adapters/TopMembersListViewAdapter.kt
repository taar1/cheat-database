package com.cheatdatabase.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.databinding.TopmembersListItemBinding
import com.cheatdatabase.holders.TopMembersListViewItemHolder
import com.cheatdatabase.listeners.OnTopMemberListItemSelectedListener

class TopMembersListViewAdapter(
    onTopMemberListItemSelectedListener: OnTopMemberListItemSelectedListener,
    val context: Context
) : RecyclerView.Adapter<TopMembersListViewItemHolder>() {
    private var memberList: List<Member>
    private val onTopMemberListItemSelectedListener: OnTopMemberListItemSelectedListener

    private var _binding: TopmembersListItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TopMembersListViewItemHolder {
        _binding = TopmembersListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return TopMembersListViewItemHolder(binding, context)
    }

    override fun onBindViewHolder(holder: TopMembersListViewItemHolder, position: Int) {
        holder.updateUI(memberList[position])
        holder.itemView.setOnClickListener {
            onTopMemberListItemSelectedListener.onMemberClicked(
                memberList[position]
            )
        }
        holder.website.setOnClickListener {
            onTopMemberListItemSelectedListener.onWebsiteClicked(
                memberList[position]
            )
        }
    }

    fun setMemberList(members: List<Member>) {
        memberList = members
    }

    override fun getItemCount(): Int {
        return memberList.size
    }

    init {
        memberList = ArrayList()
        this.onTopMemberListItemSelectedListener = onTopMemberListItemSelectedListener
    }
}