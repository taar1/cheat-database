package com.cheatdatabase.holders;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.CheatDatabaseApplication;
import com.cheatdatabase.R;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.model.Member;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class TopMembersListViewItemHolder extends RecyclerView.ViewHolder {
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

    public TopMembersListViewItemHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        this.view = view;
    }

    public void updateUI(final Member member) {

        Picasso.get().load(Konstanten.WEBDIR_MEMBER_AVATAR + member.getMid()).placeholder(R.drawable.avatar).into(avatar);

        memberName.setText(member.getUsername().toUpperCase());
        cheatCount.setText(CheatDatabaseApplication.getAppContext().getString(R.string.top_members_cheats_count) + ": " + member.getCheatSubmissionCount());

        if (member.getWebsite().length() > 1) {
            website.setText(member.getWebsite());
            website.setVisibility(View.VISIBLE);
        } else {
            website.setVisibility(View.GONE);
        }

        if (member.getProfileText().length() > 1) {
            memberMessage.setText("\"" + member.getProfileText().replaceAll("\\\\", "").trim() + "\"");
            memberMessage.setVisibility(View.VISIBLE);
        } else {
            memberMessage.setVisibility(View.GONE);
        }
    }
}