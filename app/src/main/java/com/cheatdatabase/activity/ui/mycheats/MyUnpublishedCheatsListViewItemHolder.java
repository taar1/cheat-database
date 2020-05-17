package com.cheatdatabase.activity.ui.mycheats;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.data.model.UnpublishedCheat;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyUnpublishedCheatsListViewItemHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.card_layout)
    CardView cardLayout;
    @BindView(R.id.reject_reason_layout)
    RelativeLayout rejectReasonLayout;
    @BindView(R.id.submission_status)
    TextView submissionStatus;
    @BindView(R.id.submission_status_text)
    TextView submissionStatusText;
    @BindView(R.id.details_button)
    Button detailsButton;
    @BindView(R.id.game_and_system)
    TextView gameAndSystem;
    @BindView(R.id.cheat_title)
    TextView cheatTitle;
    @BindView(R.id.cheat_text)
    TextView cheatText;
    @BindView(R.id.submission_date)
    TextView submissionDate;
    @BindView(R.id.delete_button)
    Button deleteButton;
    @BindView(R.id.edit_button)
    Button editButton;

    public final View view;
    private Activity activity;

    public MyUnpublishedCheatsListViewItemHolder(View view, Activity activity) {
        super(view);
        ButterKnife.bind(this, view);

        this.view = view;
        this.activity = activity;
    }

    public void updateUI(final UnpublishedCheat unpublishedCheat) {

        deleteButton.setText("Delete XXX");

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