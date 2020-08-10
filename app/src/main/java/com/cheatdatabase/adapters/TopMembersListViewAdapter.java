package com.cheatdatabase.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.holders.TopMembersListViewItemHolder;
import com.cheatdatabase.listeners.OnTopMemberListItemSelectedListener;

import java.util.ArrayList;
import java.util.List;

public class TopMembersListViewAdapter extends RecyclerView.Adapter<TopMembersListViewItemHolder> {
    private Context context;
    private List<Member> memberList;
    private OnTopMemberListItemSelectedListener onTopMemberListItemSelectedListener;

    public TopMembersListViewAdapter(OnTopMemberListItemSelectedListener onTopMemberListItemSelectedListener, Context context) {
        memberList = new ArrayList<>();
        this.context = context;

        this.onTopMemberListItemSelectedListener = onTopMemberListItemSelectedListener;
    }

    @Override
    public TopMembersListViewItemHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.topmembers_list_item, parent, false);
        return new TopMembersListViewItemHolder(itemView, context);
    }

    public void onBindViewHolder(TopMembersListViewItemHolder holder, final int position) {
        TopMembersListViewItemHolder topMembersListViewItemHolder = holder;
        topMembersListViewItemHolder.updateUI(memberList.get(position));
        topMembersListViewItemHolder.view.setOnClickListener(v -> onTopMemberListItemSelectedListener.onMemberClicked(memberList.get(position)));
        topMembersListViewItemHolder.website.setOnClickListener(v -> onTopMemberListItemSelectedListener.onWebsiteClicked(memberList.get(position)));
    }

    public void setMemberList(List<Member> members) {
        memberList = members;
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

}