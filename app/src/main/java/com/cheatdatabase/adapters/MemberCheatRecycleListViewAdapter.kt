package com.cheatdatabase.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cheatdatabase.data.model.Cheat
import com.cheatdatabase.data.model.Member
import com.cheatdatabase.databinding.ListrowMemberCheatItemBinding
import com.cheatdatabase.holders.MemberCheatsListViewItemHolder
import com.cheatdatabase.listeners.OnMyCheatListItemSelectedListener
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView.MeasurableAdapter
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView.SectionedAdapter
import java.util.*

class MemberCheatRecycleListViewAdapter(
    private val onMyCheatListItemSelectedListener: OnMyCheatListItemSelectedListener,
    val member: Member,
    val context: Context
) : RecyclerView.Adapter<MemberCheatsListViewItemHolder>(), SectionedAdapter,
    MeasurableAdapter<MemberCheatsListViewItemHolder?> {
    private var cheatList: List<Cheat>

    private var _binding: ListrowMemberCheatItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MemberCheatsListViewItemHolder {
        _binding = ListrowMemberCheatItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MemberCheatsListViewItemHolder(binding, context)
    }

    override fun onBindViewHolder(holder: MemberCheatsListViewItemHolder, position: Int) {
        val cheat = cheatList[position]
        holder.setLoggedInMember(member)
        holder.setCheat(cheat)
        holder.itemView.setOnClickListener {
            onMyCheatListItemSelectedListener.onCheatListItemSelected(
                cheat,
                position
            )
        }
    }

    fun setCheatList(cheatList: List<Cheat>) {
        this.cheatList = cheatList
    }

    override fun getItemCount(): Int {
        return cheatList.size
    }

    override fun getSectionName(position: Int): String {
        return cheatList[position].gameName.substring(0, 1).uppercase(Locale.getDefault())
    }

    init {
        cheatList = ArrayList()
    }

    override fun getViewTypeHeight(
        recyclerView: RecyclerView?,
        viewHolder: MemberCheatsListViewItemHolder?,
        viewType: Int
    ): Int {
        return 100
    }
}