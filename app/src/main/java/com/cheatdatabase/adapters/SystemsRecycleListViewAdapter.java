package com.cheatdatabase.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.VisibleForTesting;
import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.data.model.SystemModel;
import com.cheatdatabase.holders.SystemListViewItemHolder;
import com.cheatdatabase.listeners.OnSystemListItemSelectedListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@VisibleForTesting
public class SystemsRecycleListViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = SystemsRecycleListViewAdapter.class.getSimpleName();

    private List<SystemModel> systemList;
    private final OnSystemListItemSelectedListener listener;


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
            SystemModel system = systemList.get(position);

            SystemListViewItemHolder systemListViewItemHolder = (SystemListViewItemHolder) holder;
            systemListViewItemHolder.setSystemPlatform(system);
            systemListViewItemHolder.view.setOnClickListener(v -> listener.onSystemListItemSelected(system));
        }
    }

    public void setSystemPlatforms(List<SystemModel> systemPlatforms) {
        systemList = systemPlatforms;

        // Filter out Android (33) and iOS (30)
//        List<SystemModel> filterList = new ArrayList<>();
//        for (SystemModel s : systemList) {
//            if (s.getSystemId() == 30 || s.getSystemId() == 33) {
//                filterList.add(s);
//            }
//        }
//        systemList.removeAll(filterList);

        if ((systemList != null) && (systemList.size() > 0)) {
            Collections.sort(systemList, (system1, system2) -> system1.getName().toLowerCase().compareTo(system2.getName().toLowerCase()));

            notifyDataSetChanged();
        }
    }

    // Filter List by search word (not implemented yet)
    public void filterList(String filter) {
        if ((filter != null) && (filter.trim().length() > 2)) {
            // TODO filter the list and update gameList with filtered List
        }
    }
}