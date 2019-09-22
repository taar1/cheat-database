package com.cheatdatabase.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UkonAdListViewItemHolder extends RecyclerView.ViewHolder {
    public View view;

    @BindView(R.id.ukon_image)
    ImageView ukonImage;
    @BindView(R.id.outer_layout)
    RelativeLayout outerLayout;

    public UkonAdListViewItemHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        this.view = view;

        int random = (int) (Math.random() * 10 + 1);
        if (random % 2 == 1) {
            ukonImage.setImageResource(R.drawable.ukon_ad_2_450_225);
        }

    }
}
