package com.cheatdatabase.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.model.Cheat;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MemberCheatsListViewItemHolder extends RecyclerView.ViewHolder {
    public View view;
    private Cheat cheat;

    @BindView(R.id.game_name)
    TextView tvGameName;
    @BindView(R.id.cheat_title)
    TextView tvCheatTitle;
    @BindView(R.id.images_icon)
    ImageView imagesIcon;
    @BindView(R.id.german_flag_icon)
    ImageView germanFlagIcon;

    public MemberCheatsListViewItemHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        this.view = view;
    }

    public Cheat getCheat() {
        return cheat;
    }

    public void setCheat(Cheat cheat) {
        this.cheat = cheat;

        tvGameName.setText(cheat.getGame().getGameName() + " (" + cheat.getSystem().getSystemName() + ")");

        if (tvCheatTitle != null) {
            tvCheatTitle.setText(cheat.getCheatTitle());
        }

        if (cheat.getScreenshotList().size() > 0) {
            imagesIcon.setVisibility(View.VISIBLE);
        } else {
            imagesIcon.setVisibility(View.GONE);
        }

        if (cheat.getLanguageId() == Konstanten.GERMAN) {
            germanFlagIcon.setVisibility(View.VISIBLE);
        } else {
            germanFlagIcon.setVisibility(View.GONE);
        }


    }

}