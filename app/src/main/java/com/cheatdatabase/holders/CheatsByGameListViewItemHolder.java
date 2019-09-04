package com.cheatdatabase.holders;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;

import butterknife.BindView;
import butterknife.ButterKnife;


public class CheatsByGameListViewItemHolder extends RecyclerView.ViewHolder {
    public View view;
    private Context context;
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

    public CheatsByGameListViewItemHolder(View view, Context context) {
        super(view);
        ButterKnife.bind(this, view);

        this.view = view;
        this.context = context;

        mCheatTitle.setTypeface(Tools.getFont(view.getContext().getAssets(), Konstanten.FONT_REGULAR));

        mFlagNewAddition.setImageResource(R.drawable.flag_new);
        mFlagScreenshot.setImageResource(R.drawable.flag_img);
        mFlagGerman.setImageResource(R.drawable.flag_german);
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