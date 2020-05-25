package com.cheatdatabase.activity.ui.mycheats;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.data.model.UnpublishedCheat;
import com.cheatdatabase.listeners.MyUnpublishedCheatsListItemSelectedListener;

import java.util.ArrayList;
import java.util.List;

public class MyUnpublishedCheatsListViewAdapter extends RecyclerView.Adapter<MyUnpublishedCheatsListViewItemHolder> {
    private Activity activity;
    private List<UnpublishedCheat> unpublishedCheats;
    private MyUnpublishedCheatsListItemSelectedListener myUnpublishedCheatsListItemSelectedListener;

    public MyUnpublishedCheatsListViewAdapter(MyUnpublishedCheatsListItemSelectedListener myUnpublishedCheatsListItemSelectedListener, Activity activity) {
        unpublishedCheats = new ArrayList<>();
        this.activity = activity;

        this.myUnpublishedCheatsListItemSelectedListener = myUnpublishedCheatsListItemSelectedListener;
    }

    @Override
    public MyUnpublishedCheatsListViewItemHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.unpublished_cheat_list_item, parent, false);
        return new MyUnpublishedCheatsListViewItemHolder(itemView, activity);
    }

    public void onBindViewHolder(MyUnpublishedCheatsListViewItemHolder myUnpublishedCheatsListViewItemHolder, final int position) {
        myUnpublishedCheatsListViewItemHolder.updateUI(unpublishedCheats.get(position));
        myUnpublishedCheatsListViewItemHolder.detailsButton.setOnClickListener(v -> myUnpublishedCheatsListItemSelectedListener.onRejectReasonButtonClicked(unpublishedCheats.get(position)));
        myUnpublishedCheatsListViewItemHolder.editButton.setOnClickListener(v -> myUnpublishedCheatsListItemSelectedListener.onEditCheatButtonClicked(unpublishedCheats.get(position)));
        myUnpublishedCheatsListViewItemHolder.deleteButton.setOnClickListener(v -> myUnpublishedCheatsListItemSelectedListener.onDeleteButtonClicked(unpublishedCheats.get(position)));
    }

    public void setUnpublishedCheats(List<UnpublishedCheat> unpublishedCheats) {
        this.unpublishedCheats = unpublishedCheats;
    }

    @Override
    public int getItemCount() {
        return unpublishedCheats.size();
    }

}