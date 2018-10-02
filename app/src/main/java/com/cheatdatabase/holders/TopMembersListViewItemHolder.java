package com.cheatdatabase.holders;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
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
    ImageView avatar;

    private final Typeface fontBold;
    private final Typeface fontLight;
    private final Context context;

    public TopMembersListViewItemHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        context = CheatDatabaseApplication.getAppContext();

        fontBold = Tools.getFont(context.getAssets(), Konstanten.FONT_BOLD);
        fontLight = Tools.getFont(context.getAssets(), Konstanten.FONT_LIGHT);
    }

    public void updateUI(final Member member) {
        Picasso.get().load(Konstanten.WEBDIR_MEMBER_AVATAR + member.getMid()).placeholder(R.drawable.avatar).into(avatar);

        memberName.setTypeface(fontBold);
        memberName.setText(member.getUsername().toUpperCase());
        memberName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMemberCheatList(member);
            }
        });

        cheatCount.setTypeface(fontLight);
        cheatCount.setText(context.getString(R.string.top_members_cheats_count) + ": " + String.valueOf(member.getCheatSubmissionCount()));
        cheatCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMemberCheatList(member);
            }
        });

        website.setTypeface(fontLight);
        if (member.getWebsite().length() > 1) {
            website.setText(member.getWebsite());
        } else {
            website.setVisibility(View.GONE);
        }
        website.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                openWebsite(member.getWebsite());
            }
        });

        memberMessage.setTypeface(fontLight);
        if (member.getGreeting().length() > 1) {
            memberMessage.setText("\"" + member.getGreeting().replaceAll("\\\\", "").trim() + "\"");
        } else {
            memberMessage.setVisibility(View.GONE);
        }

        avatar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showMemberCheatList(member);
            }
        });
    }

    private void openWebsite(String url) {
        if ((url != null) && (url.length() > 4)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        }
    }

    private void showMemberCheatList(Member member) {
        if (Reachability.reachability.isReachable) {
            Intent explicitIntent = new Intent(context, CheatsByMemberListActivity.class);
            explicitIntent.putExtra("member", member);
            context.startActivity(explicitIntent);
        } else {
            Toast.makeText(context, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }
}