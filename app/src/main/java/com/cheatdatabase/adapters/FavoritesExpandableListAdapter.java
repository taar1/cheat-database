package com.cheatdatabase.adapters;

import android.app.Activity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.cheatdatabase.R;
import com.cheatdatabase.helpers.Group;
import com.cheatdatabase.listeners.OnGameListItemSelectedListener;
import com.cheatdatabase.model.Game;

public class FavoritesExpandableListAdapter extends BaseExpandableListAdapter {

    private final SparseArray<Group> groups;
    public LayoutInflater inflater;
    public Activity activity;
    private OnGameListItemSelectedListener onGameListItemSelectedListener;
    private TextView textGameTitle;
    private TextView textCheatCounter;

    public FavoritesExpandableListAdapter(Activity activity, SparseArray<Group> groups, OnGameListItemSelectedListener onGameListItemSelectedListener) {
        this.activity = activity;
        this.groups = groups;
        this.inflater = activity.getLayoutInflater();
        this.onGameListItemSelectedListener = onGameListItemSelectedListener;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        final Game game = getChild(groupPosition, childPosition);

        // TODO FIXME hier den cheat count aus DB laden und einfügen.
        // TODO FIXME hier den cheat count aus DB laden und einfügen.
        // TODO FIXME hier den cheat count aus DB laden und einfügen.

        if (view == null) {
            view = inflater.inflate(R.layout.listrow_expandable_item, null);
        }
        textGameTitle = view.findViewById(R.id.text_game_name);
        textGameTitle.setText(game.getGameName());

        // TODO machen wie gamesbystem mit: "5 Cheats" (nicht: Anz. Cheats: 5
        textCheatCounter = view.findViewById(R.id.text_cheat_counter);
        textCheatCounter.setText(R.string.cheats_count);
        textCheatCounter.append(" " + game.getCheatsCount());

        view.setOnClickListener(v -> onGameListItemSelectedListener.onGameListItemSelected(game));

        return view;
    }

    @Override
    public Game getChild(int groupPosition, int childPosition) {
        return groups.get(groupPosition).gameChildren.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groups.get(groupPosition).gameChildren.get(childPosition).getGameId();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return groups.get(groupPosition).gameChildren.size();
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
        return groups.get(groupPosition).getGame().getSystemId();
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listrow_expandable_group, null);
        }
        Group group = (Group) getGroup(groupPosition);
        ((CheckedTextView) convertView).setText(group.systemName);
        ((CheckedTextView) convertView).setChecked(isExpanded);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}