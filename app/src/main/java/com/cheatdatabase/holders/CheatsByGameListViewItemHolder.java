package com.cheatdatabase.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.model.Cheat;
import com.cheatdatabase.helpers.Konstanten;

import butterknife.BindView;
import butterknife.ButterKnife;


public class CheatsByGameListViewItemHolder extends RecyclerView.ViewHolder {
    public View view;
    private Cheat cheat;

    @BindView(R.id.cheat_title)
    TextView mCheatTitle;
    @BindView(R.id.small_ratingbar)
    RatingBar mRatingBar;
    @BindView(R.id.newaddition)
    ImageView mFlagNewAddition;
    @BindView(R.id.screenshots)
    ImageView mFlagScreenshot;
    @BindView(R.id.flag)
    ImageView mFlagGerman;

    public CheatsByGameListViewItemHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        this.view = view;
    }

    public Cheat getCheat() {
        return cheat;
    }

    public void setCheat(Cheat cheat) {
        this.cheat = cheat;

        mCheatTitle.setText(cheat.getCheatTitle());
        mRatingBar.setRating(cheat.getRatingAverage() / 2);

        if (cheat.getDayAge() < Konstanten.CHEAT_DAY_AGE_SHOW_NEWADDITION_ICON) {
            mFlagNewAddition.setVisibility(View.VISIBLE);
        } else {
            mFlagNewAddition.setVisibility(View.GONE);
        }

        if (cheat.isScreenshots()) {
            mFlagScreenshot.setVisibility(View.VISIBLE);
        } else {
            mFlagScreenshot.setVisibility(View.GONE);
        }

        if (cheat.getLanguageId() == 2) { // 2 = German
            mFlagGerman.setVisibility(View.VISIBLE);
        } else {
            mFlagGerman.setVisibility(View.GONE);
        }
    }
}