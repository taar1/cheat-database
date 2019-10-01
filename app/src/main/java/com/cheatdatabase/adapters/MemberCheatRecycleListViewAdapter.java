package com.cheatdatabase.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.holders.MemberCheatsListViewItemHolder;
import com.cheatdatabase.listeners.OnCheatListItemSelectedListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MemberCheatRecycleListViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter,
        FastScrollRecyclerView.MeasurableAdapter {

    private static final String TAG = MemberCheatRecycleListViewAdapter.class.getSimpleName();

    private List<Cheat> cheatList;
    private OnCheatListItemSelectedListener onCheatListItemSelectedListener;

    public MemberCheatRecycleListViewAdapter(OnCheatListItemSelectedListener onCheatListItemSelectedListener) {
        cheatList = new ArrayList<>();
        this.onCheatListItemSelectedListener = onCheatListItemSelectedListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listrow_member_cheat_item, parent, false);
        itemView.setDrawingCacheEnabled(true);
        return new MemberCheatsListViewItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Cheat cheat = cheatList.get(position);
        MemberCheatsListViewItemHolder memberCheatsListViewItemHolder = (MemberCheatsListViewItemHolder) holder;
        memberCheatsListViewItemHolder.setCheat(cheat);
        memberCheatsListViewItemHolder.view.setOnClickListener(v -> onCheatListItemSelectedListener.onCheatListItemSelected(cheat, position));
    }

    public void setCheatList(List<Cheat> cheatList) {
        this.cheatList = cheatList;
    }

    @Override
    public int getItemCount() {
        return cheatList.size();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return cheatList.get(position).getGameName().substring(0, 1).toUpperCase();
    }

    @Override
    public int getViewTypeHeight(RecyclerView recyclerView, @Nullable RecyclerView.ViewHolder viewHolder, int viewType) {
        return 100;
    }
}
