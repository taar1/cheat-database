package com.cheatdatabase.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TopMembersListViewAdapter extends RecyclerView.Adapter<TopMembersListViewAdapter.TopMembersListViewItemHolder> {
    private final String TAG = this.getClass().getSimpleName();
    private List<Member> memberList;

    public void setMemberList(List<Member> members) {
        memberList = members;
    }

    public class TopMembersListViewItemHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.membername)
        TextView tvMemberName;
        @BindView(R.id.cheatcount)
        TextView tvCheatCount;
        @BindView(R.id.hi_message)
        TextView tvHiMessage;
        @BindView(R.id.website)
        TextView tvWebsite;
        @BindView(R.id.avatar)
        ImageView avatarImageView;

        private final Typeface latoFontBold;
        private final Typeface latoFontLight;
        private final Context context;

        public TopMembersListViewItemHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            context = CheatDatabaseApplication.getAppContext();

            latoFontBold = Tools.getFont(context.getAssets(), Konstanten.FONT_BOLD);
            latoFontLight = Tools.getFont(context.getAssets(), Konstanten.FONT_LIGHT);
        }

        public void updateUI(final Member member) {
            Picasso.get().load(Konstanten.WEBDIR_MEMBER_AVATAR + member.getMid()).placeholder(R.drawable.avatar).into(avatarImageView);

            tvMemberName.setTypeface(latoFontBold);
            tvMemberName.setText(member.getUsername().toUpperCase());
            tvMemberName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMemberCheatList(member);
                }
            });

            tvCheatCount.setTypeface(latoFontLight);
            tvCheatCount.setText(context.getString(R.string.top_members_cheats_count) + ": " + String.valueOf(member.getCheatSubmissionCount()));
            tvCheatCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMemberCheatList(member);
                }
            });

            tvWebsite.setTypeface(latoFontLight);
            if (member.getWebsite().length() > 1) {
                tvWebsite.setText(member.getWebsite());
            } else {
                tvWebsite.setVisibility(View.GONE);
            }
            tvWebsite.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    openWebsite(member.getWebsite());
                }
            });

            tvHiMessage.setTypeface(latoFontLight);
            if (member.getGreeting().length() > 1) {
                tvHiMessage.setText("\"" + member.getGreeting().replaceAll("\\\\", "").trim() + "\"");
            } else {
                tvHiMessage.setVisibility(View.GONE);
            }

            avatarImageView.setOnClickListener(new View.OnClickListener() {

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
            // TODO FIXME
            // TODO FIXME
            // TODO FIXME
            if (Reachability.reachability.isReachable) {
                Intent explicitIntent = new Intent(context, CheatsByMemberListActivity.class);
                explicitIntent.putExtra("member", member);
                context.startActivity(explicitIntent);
            } else {
                Toast.makeText(context, R.string.no_internet, Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    public TopMembersListViewItemHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.topmembers_list_item, parent, false);
        return new TopMembersListViewItemHolder(itemView);
    }

    public void onBindViewHolder(TopMembersListViewItemHolder holder, final int position) {
        TopMembersListViewItemHolder topMembersListViewItemHolder = holder;
        topMembersListViewItemHolder.updateUI(memberList.get(position));
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

}