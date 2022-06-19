package com.cheatdatabase.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.data.model.Cheat;
import com.cheatdatabase.databinding.IncludeApplovinMaxadviewNativeBinding;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.holders.ApplovinNativeAdListViewItemHolder;
import com.cheatdatabase.holders.CheatsByGameListViewItemHolder;
import com.cheatdatabase.listeners.OnCheatListItemSelectedListener;
import com.cheatdatabase.listitems.ApplovinNativeAdListItem;
import com.cheatdatabase.listitems.CheatListItem;
import com.cheatdatabase.listitems.ListItem;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

import needle.Needle;

public class CheatsByGameRecycleListViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter,
        FastScrollRecyclerView.MeasurableAdapter {

    private static final String TAG = CheatsByGameRecycleListViewAdapter.class.getSimpleName();

    private List<Cheat> cheatList;
    private final List<ListItem> listItems;
    private final Context context;
    private final OnCheatListItemSelectedListener listener;
    private final Tools tools;

    public CheatsByGameRecycleListViewAdapter(Activity activity, Tools tools, OnCheatListItemSelectedListener listener) {
        this.context = activity;
        this.tools = tools;
        this.listener = listener;
        cheatList = new ArrayList<>();
        listItems = new ArrayList<>();

        // TODO at some point implement a cheat filter function
        filterList("");
    }

    public void setCheatList(List<Cheat> cheatList) {
        this.cheatList = cheatList;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < 0) {
            return ListItem.TYPE_CHEAT;
        } else {
            return listItems.get(position).type();
        }
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        if (viewType == ListItem.TYPE_APPLOVIN_NATIVE) {
            IncludeApplovinMaxadviewNativeBinding binding = IncludeApplovinMaxadviewNativeBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new ApplovinNativeAdListViewItemHolder(binding);
        } else {
            final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listrow_cheat_item, parent, false);
            itemView.setDrawingCacheEnabled(true);
            return new CheatsByGameListViewItemHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        Log.d(TAG, "ADAPTER onBindViewHolder() TYPE: " + type);

        if (type == ListItem.TYPE_CHEAT) {
            final CheatListItem cheatListItem = (CheatListItem) listItems.get(position);
            CheatsByGameListViewItemHolder cheatsByGameListViewItemHolder = (CheatsByGameListViewItemHolder) holder;
            cheatsByGameListViewItemHolder.setCheat(cheatListItem.getCheat());
            cheatsByGameListViewItemHolder.view.setOnClickListener(v -> listener.onCheatListItemSelected(cheatListItem.getCheat(), getCorrectedPosition(cheatListItem)));
        } else if (type == ListItem.TYPE_APPLOVIN_NATIVE) {
            ApplovinNativeAdListViewItemHolder applovinNativeAdListViewItemHolder = (ApplovinNativeAdListViewItemHolder) holder;
            applovinNativeAdListViewItemHolder.showIt();
        }
    }

    private int getCorrectedPosition(CheatListItem selectedCheatListItem) {
        for (int i = 0; i < cheatList.size(); i++) {
            Cheat cheat = cheatList.get(i);
            if (selectedCheatListItem.getCheat().getCheatId() == cheat.getCheatId()) {
                return i;
            }
        }
        return 0;
    }

    // Display the first letter of the game during fast scrolling
    @NonNull
    @Override
    public String getSectionName(int position) {
        // What will be displayed at the right side when fast scroll is used (normally the first letter of the game)
        int type = getItemViewType(position);
        if (type == ListItem.TYPE_CHEAT) {
            return listItems.get(position).title().toUpperCase();
        } else {
            // When we show an ad or something else we show blank
            return "";
        }
    }

    @Override
    public int getViewTypeHeight(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int viewType) {
        return 100;
    }

    // Filter List by search qord (not implemented yet)
    public void filterList(String filter) {
        if ((filter != null) && (filter.trim().length() > 2)) {
            // TODO filter the list and update gameList with filtered List
        }

        updateCheatListAndInjectNativeAds();
    }

    private void updateCheatListAndInjectNativeAds() {
        int j = 0;
        final List<ListItem> newListItems = new ArrayList<>();

        for (Cheat cheat : cheatList) {
            CheatListItem cheatListItem = new CheatListItem();
            cheatListItem.setCheat(cheat);
            newListItems.add(cheatListItem);

            if (j % Konstanten.INJECT_AD_AFTER_EVERY_POSITION == Konstanten.INJECT_AD_AFTER_EVERY_POSITION - 1) {
                newListItems.add(new ApplovinNativeAdListItem());
            }
            j++;
        }

        Needle.onMainThread().execute(() -> {
            listItems.clear();
            listItems.addAll(newListItems);
            notifyDataSetChanged();
        });
    }

    public void updateCheatListWithoutAds() {
        final List<ListItem> newListItems = new ArrayList<>();

        for (Cheat cheat : cheatList) {
            CheatListItem cheatListItem = new CheatListItem();
            cheatListItem.setCheat(cheat);
            newListItems.add(cheatListItem);
        }

        Needle.onMainThread().execute(() -> {
            listItems.clear();
            listItems.addAll(newListItems);
            notifyDataSetChanged();
        });
    }
}
