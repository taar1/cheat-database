package com.cheatdatabase.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.model.SystemPlatform;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SystemListViewItemHolder extends RecyclerView.ViewHolder {
    public View view;

    @BindView(R.id.outer_layout)
    LinearLayout outerLayout;
    @BindView(R.id.system_icon)
    ImageView systemIcon;
    @BindView(R.id.system_name)
    TextView systemName;
    @BindView(R.id.subtitle)
    TextView subtitle;

    public SystemListViewItemHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        this.view = view;
    }

    public void setSystemPlatform(SystemPlatform systemPlatform) {
        systemName.setText(systemPlatform.getSystemName());
        subtitle.setText(systemPlatform.getGameCount() + " Games");
    }

}

