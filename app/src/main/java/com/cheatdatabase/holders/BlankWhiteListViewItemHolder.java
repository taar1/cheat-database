package com.cheatdatabase.holders;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import butterknife.ButterKnife;

public class BlankWhiteListViewItemHolder extends RecyclerView.ViewHolder {
    public View view;

    public BlankWhiteListViewItemHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

}
