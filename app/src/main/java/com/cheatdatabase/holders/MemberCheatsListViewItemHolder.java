package com.cheatdatabase.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.data.model.Cheat;
import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.helpers.Konstanten;

import butterknife.BindView;
import butterknife.ButterKnife;

@Deprecated
public class MemberCheatsListViewItemHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "MemberCheatsListViewIte";

    public View view;
    private Cheat cheat;
    private Member member;

    @BindView(R.id.gameName)
    TextView tvGameName;
    @BindView(R.id.cheatTitle)
    TextView tvCheatTitle;
    @BindView(R.id.imagesIcon)
    ImageView imagesIcon;
    @BindView(R.id.germanFlag)
    ImageView germanFlagIcon;
    @BindView(R.id.editImageButton)
    public ImageView editButton;

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

        // TODO FIXME add this again when continue work on the "edit my cheat" functionality
//        if (member.getMid() == cheat.getSubmittingMember().getMid()) {
//            editButton.setVisibility(View.VISIBLE);
//        } else {
//            editButton.setVisibility(View.GONE);
//        }
        editButton.setVisibility(View.GONE);
    }

    public void setLoggedInMember(Member member) {
        this.member = member;
    }
}