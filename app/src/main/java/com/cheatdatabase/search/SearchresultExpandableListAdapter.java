package com.cheatdatabase.search;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.CheatListActivity;
import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.helpers.Group;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;

public class SearchresultExpandableListAdapter extends BaseExpandableListAdapter {

    private final SparseArray<Group> groups;
    public LayoutInflater inflater;
    public Activity activity;
    private Typeface latoFontLight;
    private Typeface latoFontRegular;
    private Typeface latoFontBold;

    public SearchresultExpandableListAdapter(Activity act, SparseArray<Group> groups) {
        activity = act;
        this.groups = groups;
        inflater = act.getLayoutInflater();

        latoFontLight = Tools.getFont(act.getAssets(), Konstanten.FONT_LIGHT);
        latoFontRegular = Tools.getFont(act.getAssets(), Konstanten.FONT_REGULAR);
        latoFontBold = Tools.getFont(act.getAssets(), Konstanten.FONT_BOLD);
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
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final Game gameObj = (Game) getChild(groupPosition, childPosition);

        if (gameObj == null) {
            activity.finish();
        }

        TextView textGameTitle = null;
        TextView textCheatCounter = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.gamesearch_listrow_details, null);
        }
        textGameTitle = (TextView) convertView.findViewById(R.id.text_game_name);
        textGameTitle.setText(gameObj.getGameName());
        textGameTitle.setTypeface(latoFontRegular);

        // TODO gleich machen wie bei gamesbystem mit: "10 Cheats" (nicht:
        // Anzahl Cheats: 10)
        textCheatCounter = (TextView) convertView.findViewById(R.id.text_cheat_counter);
        textCheatCounter.setText(R.string.cheats_count);
        textCheatCounter.setTypeface(latoFontLight);
        textCheatCounter.append(" " + gameObj.getCheatsCount());
        convertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Reachability.reachability.isReachable) {
                    Intent explicitIntent = new Intent(activity, CheatListActivity.class);
                    explicitIntent.putExtra("gameObj", gameObj);

                    activity.startActivity(explicitIntent);
                } else {
                    Toast.makeText(activity, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });
        return convertView;
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
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.gamesearch_listrow_group, null);
        }
        Group group = (Group) getGroup(groupPosition);
        ((CheckedTextView) convertView).setText(group.string);
        ((CheckedTextView) convertView).setTypeface(latoFontBold);
        ((CheckedTextView) convertView).setChecked(isExpanded);

        return convertView;
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