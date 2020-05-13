package com.cheatdatabase.activity.ui.mycheats;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.data.model.UnpublishedCheat;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MyUnpublishedCheatsListViewItemHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.member_name)
    TextView memberName;
    @BindView(R.id.cheat_count)
    TextView cheatCount;
    @BindView(R.id.member_message)
    TextView memberMessage;
    @BindView(R.id.website)
    public TextView website;
    @BindView(R.id.avatar)
    CircleImageView avatar;

    public final View view;
    private Activity activity;

    public MyUnpublishedCheatsListViewItemHolder(View view, Activity activity) {
        super(view);
        ButterKnife.bind(this, view);

        this.view = view;
        this.activity = activity;
    }

    public void updateUI(final UnpublishedCheat unpublishedCheat) {

        memberName.setText("TEST 1");

//        memberName.setText(unpublishedCheat.getUsername().toUpperCase());
//        cheatCount.setText(activity.getString(R.string.top_members_cheats_count) + ": " + unpublishedCheat.getCheatSubmissionCount());
//
//        if (unpublishedCheat.getWebsite().length() > 1) {
//            website.setText(unpublishedCheat.getWebsite());
//            website.setVisibility(View.VISIBLE);
//        } else {
//            website.setVisibility(View.GONE);
//        }
//
//        if (unpublishedCheat.getProfileText().length() > 1) {
//            memberMessage.setText("\"" + unpublishedCheat.getProfileText().replaceAll("\\\\", "").trim() + "\"");
//            memberMessage.setVisibility(View.VISIBLE);
//        } else {
//            memberMessage.setVisibility(View.GONE);
//        }
    }
}