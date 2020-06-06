package com.cheatdatabase.activity.ui.mycheats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cheatdatabase.R
import com.cheatdatabase.data.model.UnpublishedCheat
import com.cheatdatabase.listeners.MyUnpublishedCheatsListItemSelectedListener
import java.util.*

class MyUnpublishedCheatsListViewAdapter(
    val myUnpublishedCheatsListItemSelectedListener: MyUnpublishedCheatsListItemSelectedListener,
    val activity: MyUnpublishedCheatsListActivity
) : RecyclerView.Adapter<MyUnpublishedCheatsListViewItemHolder>() {
    var unpublishedCheats: List<UnpublishedCheat> = ArrayList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyUnpublishedCheatsListViewItemHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.unpublished_cheat_list_item, parent, false)
        return MyUnpublishedCheatsListViewItemHolder(itemView, activity)
    }

    override fun onBindViewHolder(
        myUnpublishedCheatsListViewItemHolder: MyUnpublishedCheatsListViewItemHolder,
        position: Int
    ) {
        myUnpublishedCheatsListViewItemHolder.updateUI(unpublishedCheats[position])
        myUnpublishedCheatsListViewItemHolder.detailsButton.setOnClickListener {
            myUnpublishedCheatsListItemSelectedListener.onRejectReasonButtonClicked(
                unpublishedCheats[position]
            )
        }
        myUnpublishedCheatsListViewItemHolder.editButton.setOnClickListener {
            myUnpublishedCheatsListItemSelectedListener.onEditCheatButtonClicked(
                unpublishedCheats[position]
            )
        }
        myUnpublishedCheatsListViewItemHolder.deleteButton.setOnClickListener {
            myUnpublishedCheatsListItemSelectedListener.onDeleteButtonClicked(
                unpublishedCheats[position],
                position
            )
        }
    }

    override fun getItemCount(): Int {
        return unpublishedCheats.size
    }


}