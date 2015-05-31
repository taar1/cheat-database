package com.cheatdatabase.adapters;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.SystemPlatform;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;

import java.util.ArrayList;

public class SystemsRecycleListViewAdapter extends RecyclerView.Adapter<SystemsRecycleListViewAdapter.ViewHolder> {
    private ArrayList<SystemPlatform> mSystemObjects;
    private Typeface latoFontBold;
    private Typeface latoFontLight;
    private SystemPlatform systemObj;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {


        public TextView mSystemName;
        public TextView mSubtitle;

        public ViewHolder(View v) {
            super(v);

            mSystemName = (TextView) v.findViewById(R.id.system_name);
            mSubtitle = (TextView) v.findViewById(R.id.subtitle);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public SystemsRecycleListViewAdapter(ArrayList<SystemPlatform> systemObjects) {
        mSystemObjects = systemObjects;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SystemsRecycleListViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        latoFontBold = Tools.getFont(parent.getContext().getAssets(), Konstanten.FONT_BOLD);
        latoFontLight = Tools.getFont(parent.getContext().getAssets(), Konstanten.FONT_LIGHT);

        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.systemlist_item, parent, false);
        v.setDrawingCacheEnabled(true);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        systemObj = mSystemObjects.get(position);

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mSystemName.setText(systemObj.getSystemName());
        holder.mSystemName.setTypeface(latoFontBold);

        try {
            holder.mSubtitle.setText(systemObj.getGameCount() + " Games");
            holder.mSubtitle.setTypeface(latoFontLight);
        } catch (Exception e) {
            // Do nothing
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mSystemObjects.size();
    }
}