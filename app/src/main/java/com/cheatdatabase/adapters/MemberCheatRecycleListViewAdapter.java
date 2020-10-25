package com.cheatdatabase.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.data.model.Cheat;
import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.holders.MemberCheatsListViewItemHolder;
import com.cheatdatabase.listeners.OnMyCheatListItemSelectedListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MemberCheatRecycleListViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter,
        FastScrollRecyclerView.MeasurableAdapter {

    private static final String TAG = MemberCheatRecycleListViewAdapter.class.getSimpleName();

    private List<Cheat> cheatList;
    private final OnMyCheatListItemSelectedListener onMyCheatListItemSelectedListener;
    private Member loggedInMember;

    public MemberCheatRecycleListViewAdapter(OnMyCheatListItemSelectedListener onMyCheatListItemSelectedListener) {
        cheatList = new ArrayList<>();
        this.onMyCheatListItemSelectedListener = onMyCheatListItemSelectedListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        // TODO hier weiterfahren, member objekt wird jetzt mitgegeben (PHP)
        // TODO hier weiterfahren, member objekt wird jetzt mitgegeben (PHP)
        Log.d(TAG, "XXXXX onCreateViewHolder: loggedInMember" + loggedInMember.getMid());
        Log.d(TAG, "XXXXX onCreateViewHolder: loggedInMember" + cheatList.get(0).getSubmittingMember().getMid());

        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listrow_member_cheat_item, parent, false);
        itemView.setDrawingCacheEnabled(true);
        return new MemberCheatsListViewItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Cheat cheat = cheatList.get(position);
        MemberCheatsListViewItemHolder memberCheatsListViewItemHolder = (MemberCheatsListViewItemHolder) holder;
        memberCheatsListViewItemHolder.setLoggedInMember(loggedInMember);
        memberCheatsListViewItemHolder.setCheat(cheat);
        memberCheatsListViewItemHolder.view.setOnClickListener(v -> onMyCheatListItemSelectedListener.onCheatListItemSelected(cheat, position));
        memberCheatsListViewItemHolder.editButton.setOnClickListener(v -> onMyCheatListItemSelectedListener.onCheatListItemEditSelected(cheat, position));
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

    public void setLoggedInMember(Member member) {
        this.loggedInMember = member;
    }
}
