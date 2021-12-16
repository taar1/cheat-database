package com.cheatdatabase.activity.ui.mycheats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cheatdatabase.data.model.UnpublishedCheat
import com.cheatdatabase.databinding.UnpublishedCheatListItemBinding
import com.cheatdatabase.listeners.MyUnpublishedCheatsListItemSelectedListener

class MyUnpublishedCheatsListViewAdapter(
    val myUnpublishedCheatsListItemSelectedListener: MyUnpublishedCheatsListItemSelectedListener,
    val activity: MyUnpublishedCheatsListActivity
) : RecyclerView.Adapter<MyUnpublishedCheatsListViewItemHolder>() {
    var unpublishedCheats: List<UnpublishedCheat> = ArrayList()

    private var _binding: UnpublishedCheatListItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyUnpublishedCheatsListViewItemHolder {
        _binding = UnpublishedCheatListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return MyUnpublishedCheatsListViewItemHolder(binding, activity)
    }

    override fun onBindViewHolder(
        myUnpublishedCheatsListViewItemHolder: MyUnpublishedCheatsListViewItemHolder,
        position: Int
    ) {
        myUnpublishedCheatsListViewItemHolder.setCheat(unpublishedCheats[position])
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