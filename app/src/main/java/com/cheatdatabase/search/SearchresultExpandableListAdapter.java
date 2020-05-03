package com.cheatdatabase.search;

import android.app.Activity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.cheatdatabase.R;
import com.cheatdatabase.data.model.Game;
import com.cheatdatabase.helpers.Group;
import com.cheatdatabase.listeners.OnGameListItemSelectedListener;

public class SearchresultExpandableListAdapter extends BaseExpandableListAdapter {

    private final SparseArray<Group> groups;
    public LayoutInflater inflater;
    public Activity activity;
    private TextView textGameTitle;
    private TextView textCheatCounter;
    private OnGameListItemSelectedListener onGameListItemSelectedListener;

    public SearchresultExpandableListAdapter(Activity activity, SparseArray<Group> groups, OnGameListItemSelectedListener onGameListItemSelectedListener) {
        this.activity = activity;
        this.groups = groups;
        this.inflater = activity.getLayoutInflater();
        this.onGameListItemSelectedListener = onGameListItemSelectedListener;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        final Game gameObj = (Game) getChild(groupPosition, childPosition);

        if (view == null) {
            view = inflater.inflate(R.layout.listrow_expandable_item, null);
        }
        textGameTitle = view.findViewById(R.id.text_game_name);
        textGameTitle.setText(gameObj.getGameName());

        textCheatCounter = view.findViewById(R.id.text_cheat_counter);
        textCheatCounter.setText(R.string.cheats_count);
        textCheatCounter.append(" " + gameObj.getCheatsCount());

        view.setOnClickListener(v -> onGameListItemSelectedListener.onGameListItemSelected(gameObj));
        return view;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groups.get(groupPosition).gameChildren.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return groups.get(groupPosition).children.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.listrow_expandable_group, null);
        }
        Group group = (Group) getGroup(groupPosition);
        ((CheckedTextView) view).setText(group.systemName);
        ((CheckedTextView) view).setChecked(isExpanded);

        return view;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}