package com.cheatdatabase.holders;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.CheatDatabaseApplication;
import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.listeners.OnTopMemberListItemSelectedListener;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class TopMembersListViewItemHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.member_name)
    TextView memberName;
    @BindView(R.id.cheat_count)
    TextView cheatCount;
    @BindView(R.id.member_message)
    TextView memberMessage;
    @BindView(R.id.website)
    TextView website;
    @BindView(R.id.avatar)
    CircleImageView avatar;

    private final View view;

    private Member member;
    private OnTopMemberListItemSelectedListener topMemberListItemClickListener;

    public TopMembersListViewItemHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        this.view = view;
    }

    public void updateUI(final Member member) {
        this.member = member;

        Picasso.get().load(Konstanten.WEBDIR_MEMBER_AVATAR + member.getMid()).placeholder(R.drawable.avatar).into(avatar);

        memberName.setText(member.getUsername().toUpperCase());
        cheatCount.setText(CheatDatabaseApplication.getAppContext().getString(R.string.top_members_cheats_count) + ": " + member.getCheatSubmissionCount());

        if (member.getWebsite().length() > 1) {
            website.setText(member.getWebsite());
            website.setVisibility(View.VISIBLE);
        } else {
            website.setVisibility(View.GONE);
        }

        if (member.getGreeting().length() > 1) {
            memberMessage.setText("\"" + member.getGreeting().replaceAll("\\\\", "").trim() + "\"");
            memberMessage.setVisibility(View.VISIBLE);
        } else {
            memberMessage.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.website)
    void openWebsite() {
        topMemberListItemClickListener.onWebsiteClicked(member);
    }

    @OnClick({R.id.member_name, R.id.cheat_count, R.id.avatar})
    void showMemberCheatList() {
        topMemberListItemClickListener.onMemberClicked(member);
    }

    public void setClickListener(OnTopMemberListItemSelectedListener onTopMemberListItemSelectedListener) {
        this.topMemberListItemClickListener = onTopMemberListItemSelectedListener;
    }
}