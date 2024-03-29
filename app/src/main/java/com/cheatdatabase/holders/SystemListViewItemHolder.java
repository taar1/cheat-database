package com.cheatdatabase.holders;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.data.model.SystemModel;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SystemListViewItemHolder extends RecyclerView.ViewHolder {
    public View view;

    @BindView(R.id.system_name)
    TextView systemName;
    @BindView(R.id.subtitle)
    TextView subtitle;

    public SystemListViewItemHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        this.view = view;
    }

    public void setSystemPlatform(SystemModel systemPlatform) {
        systemName.setText(systemPlatform.getName());
        subtitle.setText(systemPlatform.getGamesCount() + " Games");
    }

}

