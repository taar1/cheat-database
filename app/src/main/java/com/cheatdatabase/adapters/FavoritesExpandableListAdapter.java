package com.cheatdatabase.adapters;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.cheatdatabase.R;
import com.cheatdatabase.data.dao.FavoriteCheatDao;
import com.cheatdatabase.data.model.Game;
import com.cheatdatabase.helpers.Group;
import com.cheatdatabase.listeners.OnGameListItemSelectedListener;

import needle.Needle;

public class FavoritesExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;

    private final SparseArray<Group> groups;
    private LayoutInflater inflater;
    private FavoriteCheatDao dao;
    private OnGameListItemSelectedListener onGameListItemSelectedListener;

    private TextView textGameTitle;
    private TextView textCheatCounter;

    public FavoritesExpandableListAdapter(Context context, SparseArray<Group> groups, FavoriteCheatDao dao, OnGameListItemSelectedListener onGameListItemSelectedListener, LayoutInflater inflater) {
        this.context = context;
        this.groups = groups;
        this.dao = dao;
        this.onGameListItemSelectedListener = onGameListItemSelectedListener;
        this.inflater = inflater;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        Game game = getChild(groupPosition, childPosition);

        if (view == null) {
            view = inflater.inflate(R.layout.listrow_expandable_item, null);
        }

        textGameTitle = view.findViewById(R.id.text_game_name);
        textGameTitle.setText(game.getGameName());

        updateCheatsCounter(game, view);

        view.setOnClickListener(v -> onGameListItemSelectedListener.onGameListItemSelected(game));

        return view;
    }

    private void updateCheatsCounter(Game game, View view) {
        Needle.onBackgroundThread().execute(() -> {
            setCheatCounterText(dao.countCheats(game.getGameId()), view);
        });
    }

    private void setCheatCounterText(int count, View view) {
        Needle.onMainThread().execute(() -> {
            // findViewById must be here otherwise the first element doesn't get updated (for whatever reason...)
            textCheatCounter = view.findViewById(R.id.text_cheat_counter);
            textCheatCounter.setText(R.string.cheats_count);
            textCheatCounter.append(" " + count);
        });
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
        //return groups.get(groupPosition).getGame().getSystemId();
        return 0;
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