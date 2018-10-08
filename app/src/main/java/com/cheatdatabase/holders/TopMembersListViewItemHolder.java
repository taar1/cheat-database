package com.cheatdatabase.holders;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatdatabase.CheatDatabaseApplication;
import com.cheatdatabase.CheatsByMemberListActivity;
import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
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

    private final Typeface fontBold;
    private final Typeface fontLight;
    private Member member;

    public TopMembersListViewItemHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        fontBold = Tools.getFont(CheatDatabaseApplication.getAppContext().getAssets(), Konstanten.FONT_BOLD);
        fontLight = Tools.getFont(CheatDatabaseApplication.getAppContext().getAssets(), Konstanten.FONT_LIGHT);
    }

    public void updateUI(final Member member) {
        this.member = member;

        Picasso.get().load(Konstanten.WEBDIR_MEMBER_AVATAR + member.getMid()).placeholder(R.drawable.avatar).into(avatar);

        memberName.setTypeface(fontBold);
        cheatCount.setTypeface(fontLight);
        website.setTypeface(fontLight);
        memberMessage.setTypeface(fontLight);

        memberName.setText(member.getUsername().toUpperCase());
        cheatCount.setText(CheatDatabaseApplication.getAppContext().getString(R.string.top_members_cheats_count) + ": " + String.valueOf(member.getCheatSubmissionCount()));

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
        String url = member.getWebsite();
        if ((url != null) && (url.length() > 4)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            CheatDatabaseApplication.getAppContext().startActivity(intent);
        }
    }

    @OnClick({R.id.member_name, R.id.cheat_count, R.id.avatar})
    void showMemberCheatList() {
        if (Reachability.reachability.isReachable) {
            Intent explicitIntent = new Intent(CheatDatabaseApplication.getAppContext(), CheatsByMemberListActivity.class);
            explicitIntent.putExtra("member", member);
            CheatDatabaseApplication.getAppContext().startActivity(explicitIntent);
        } else {
            Toast.makeText(CheatDatabaseApplication.getAppContext(), R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }
}