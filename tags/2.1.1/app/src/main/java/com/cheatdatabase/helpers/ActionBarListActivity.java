package com.cheatdatabase.helpers;

import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * ActionBarActivity ListActivity
 * Alternative without using an additional class: http://stackoverflow.com/questions/22439719/listview-without-extending-listactivity
 * Created by Dominik on 01.01.2015.
 */
public abstract class ActionBarListActivity extends ActionBarActivity {

    private ListView mListView;

    protected ListView getListView() {
        if (mListView == null) {
            mListView = (ListView) findViewById(android.R.id.list);
        }
        return mListView;
    }

    protected void setListAdapter(ListAdapter adapter) {
        getListView().setAdapter(adapter);
    }

    protected ListAdapter getListAdapter() {
        ListAdapter adapter = getListView().getAdapter();
        if (adapter instanceof HeaderViewListAdapter) {
            return ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        } else {
            return adapter;
        }
    }

    protected void onListItemClick(ListView lv, View v, int position, long id) {
        getListView().getOnItemClickListener().onItemClick(lv, v, position, id);
    }
}
