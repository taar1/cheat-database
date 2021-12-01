package com.cheatdatabase.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.data.model.Cheat;
import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.holders.MemberCheatsListViewItemHolderK;
import com.cheatdatabase.listeners.OnMyCheatListItemSelectedListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MemberCheatRecycleListViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter,
        FastScrollRecyclerView.MeasurableAdapter {

    private static final String TAG = MemberCheatRecycleListViewAdapter.class.getSimpleName();

    private List<Cheat> cheatList;
    private final OnMyCheatListItemSelectedListener onMyCheatListItemSelectedListener;
    private Member member;

    public MemberCheatRecycleListViewAdapter(OnMyCheatListItemSelectedListener onMyCheatListItemSelectedListener, Member member) {
        cheatList = new ArrayList<>();
        this.onMyCheatListItemSelectedListener = onMyCheatListItemSelectedListener;
        this.member = member;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listrow_member_cheat_item, parent, false);
        itemView.setDrawingCacheEnabled(true);
        return new MemberCheatsListViewItemHolderK(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Cheat cheat = cheatList.get(position);
        MemberCheatsListViewItemHolderK memberCheatsListViewItemHolder = (MemberCheatsListViewItemHolderK) holder;
        memberCheatsListViewItemHolder.setLoggedInMember(member);
        memberCheatsListViewItemHolder.setCheat(cheat);
        memberCheatsListViewItemHolder.itemView.setOnClickListener(v -> onMyCheatListItemSelectedListener.onCheatListItemSelected(cheat, position));
        //memberCheatsListViewItemHolder.editButton.setOnClickListener(v -> onMyCheatListItemSelectedListener.onCheatListItemEditSelected(cheat, position));
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

    public void setMember(Member member) {
        this.member = member;
    }
}
