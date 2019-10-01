package com.cheatdatabase.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.SystemPlatform;
import com.cheatdatabase.holders.SystemListViewItemHolder;
import com.cheatdatabase.listeners.OnSystemListItemSelectedListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import needle.Needle;

public class SystemsRecycleListViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = SystemsRecycleListViewAdapter.class.getSimpleName();

    private List<SystemPlatform> systemList;
    private OnSystemListItemSelectedListener listener;


    public SystemsRecycleListViewAdapter(OnSystemListItemSelectedListener listener) {
        this.listener = listener;
        systemList = new ArrayList<>();

        filterList("");
    }

    @Override
    public int getItemCount() {
        if (systemList != null) {
            return systemList.size();
        } else {
            return 0;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.systemlist_item, parent, false);
        return new SystemListViewItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (systemList != null && systemList.size() > 0) {
            SystemPlatform system = systemList.get(position);

            SystemListViewItemHolder systemListViewItemHolder = (SystemListViewItemHolder) holder;
            systemListViewItemHolder.setSystemPlatform(system);
            systemListViewItemHolder.view.setOnClickListener(v -> listener.onSystemListItemSelected(system));
        }
    }

    public void setSystemPlatforms(List<SystemPlatform> systemPlatforms) {
        systemList = systemPlatforms;

        if ((systemList != null) && (systemList.size() > 0)) {
            Collections.sort(systemList, (system1, system2) -> system1.getSystemName().toLowerCase().compareTo(system2.getSystemName().toLowerCase()));

            Needle.onMainThread().execute(() -> {
                notifyDataSetChanged();
            });
        }
    }

    // Filter List by search qord (not implemented yet)
    public void filterList(String filter) {
        if ((filter != null) && (filter.trim().length() > 2)) {
            // TODO filter the list and update gameList with filtered List
        }
    }
}