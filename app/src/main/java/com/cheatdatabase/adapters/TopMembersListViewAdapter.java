package com.cheatdatabase.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.fragments.TopMembersFragment;
import com.cheatdatabase.holders.TopMembersListViewItemHolder;

import java.util.ArrayList;
import java.util.List;

public class TopMembersListViewAdapter extends RecyclerView.Adapter<TopMembersListViewItemHolder> {
    private List<Member> memberList;
    private TopMembersFragment.TopMemberListItemClickListener topMemberListItemClickListener;

    public TopMembersListViewAdapter(TopMembersFragment.TopMemberListItemClickListener topMemberListItemClickListener) {
        memberList = new ArrayList<>();

        this.topMemberListItemClickListener = topMemberListItemClickListener;
    }

    @Override
    public TopMembersListViewItemHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.topmembers_list_item, parent, false);
        return new TopMembersListViewItemHolder(itemView);
    }

    public void onBindViewHolder(TopMembersListViewItemHolder holder, final int position) {
        TopMembersListViewItemHolder topMembersListViewItemHolder = holder;
        topMembersListViewItemHolder.updateUI(memberList.get(position));
        topMembersListViewItemHolder.setClickListener(topMemberListItemClickListener);
    }

    public void setMemberList(List<Member> members) {
        memberList = members;
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

}