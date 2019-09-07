package com.cheatdatabase.holders;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Game;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UkonAdListViewItemHolder extends RecyclerView.ViewHolder {
    public View view;

    @BindView(R.id.outer_layout)
    RelativeLayout outerLayout;

    public UkonAdListViewItemHolder(View view, Context context) {
        super(view);
        ButterKnife.bind(this, view);

        this.view = view;
    }
}
