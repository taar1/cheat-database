package com.cheatdatabase.holders;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InMobiNativeAdListViewItemHolder extends RecyclerView.ViewHolder {
    public View view;

    @BindView(R.id.outer_layout)
    public RelativeLayout outerLayout;
    @BindView(R.id.ad_text_1)
    public TextView adText1;
    @BindView(R.id.ad_text_2)
    public TextView adText2;
    @BindView(R.id.ad_text_3)
    public TextView adText3;

    public InMobiNativeAdListViewItemHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        this.view = view;

    }
}
