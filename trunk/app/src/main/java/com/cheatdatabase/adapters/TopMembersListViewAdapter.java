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

import com.cheatdatabase.MemberCheatListActivity_;
import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;

@EBean
public class TopMembersListViewAdapter extends RecyclerView.Adapter<TopMembersListViewAdapter.ViewHolder> {
    private static final String TAG = TopMembersListViewAdapter.class.getSimpleName();

    private ArrayList<Member> mMemberObjects;
    private Typeface latoFontBold;
    private Typeface latoFontLight;
    private Member memberObj;

    @RootContext
    Context mContext;

    public void init(ArrayList<Member> members) {
        mMemberObjects = members;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvMemberName;
        TextView tvCheatCount;
        TextView tvHiMessage;
        TextView tvWebsite;
        ImageView avatarImageView;

        public ViewHolder(View v) {
            super(v);

            tvMemberName = v.findViewById(R.id.membername);
            tvCheatCount = v.findViewById(R.id.cheatcount);
            tvHiMessage = v.findViewById(R.id.hi_message);
            tvWebsite = v.findViewById(R.id.website);
            avatarImageView = (ImageView) v.findViewById(R.id.avatar);
        }
    }

    private void openWebsite(String url) {
        // TODO member per email informieren, dass jemand seine homepage geoeffnet hat.
        if ((url != null) && (url.length() > 4)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            mContext.startActivity(intent);
        }
    }

    private void showMemberCheatList(Member member) {
//        //CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "show_member").setLabel(member.getUsername()).build());

        if (Reachability.reachability.isReachable) {
            Intent explicitIntent = new Intent(mContext, MemberCheatListActivity_.class);
            explicitIntent.putExtra("memberObj", member);
            mContext.startActivity(explicitIntent);
        } else {
            Toast.makeText(mContext, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }


    // Create new views (invoked by the layout manager)
    @Override
    public TopMembersListViewAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

        latoFontBold = Tools.getFont(parent.getContext().getAssets(), Konstanten.FONT_BOLD);
        latoFontLight = Tools.getFont(parent.getContext().getAssets(), Konstanten.FONT_LIGHT);

        // create a new view
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.topmembers_list_item, parent, false);
        v.setDrawingCacheEnabled(true);

        return new ViewHolder(v);
    }

    public void onBindViewHolder(ViewHolder holder, final int position) {
        memberObj = mMemberObjects.get(position);

        holder.tvMemberName.setTypeface(latoFontBold);
        holder.tvMemberName.setText(memberObj.getUsername().toUpperCase());
        holder.tvMemberName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMemberCheatList(mMemberObjects.get(position));
            }
        });

        holder.tvCheatCount.setTypeface(latoFontLight);
        holder.tvCheatCount.setText(mContext.getString(R.string.top_members_cheats_count) + ": " + String.valueOf(memberObj.getCheatSubmissionCount()));
        holder.tvCheatCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMemberCheatList(mMemberObjects.get(position));
            }
        });

        holder.tvWebsite.setTypeface(latoFontLight);
        if (memberObj.getWebsite().length() > 1) {
            holder.tvWebsite.setText(memberObj.getWebsite());
        } else {
            holder.tvWebsite.setVisibility(View.GONE);
        }
        holder.tvWebsite.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                //CheatDatabaseApplication.tracker().send(new HitBuilders.EventBuilder("ui", "show_member_website").setLabel(memberObj.getUsername()).build());
                openWebsite(memberObj.getWebsite());
            }
        });
        holder.tvWebsite.setDrawingCacheEnabled(false);


        holder.tvHiMessage.setTypeface(latoFontLight);
        if (memberObj.getGreeting().length() > 1) {
            holder.tvHiMessage.setText("\"" + memberObj.getGreeting().replaceAll("\\\\", "").trim() + "\"");
        } else {
            holder.tvHiMessage.setVisibility(View.GONE);
        }

        holder.avatarImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showMemberCheatList(mMemberObjects.get(position));
            }

        });
        Picasso.with(mContext.getApplicationContext()).load(Konstanten.WEBDIR_MEMBER_AVATAR + memberObj.getMid()).placeholder(R.drawable.avatar).into(holder.avatarImageView);
    }

    @Override
    public int getItemCount() {
        return mMemberObjects.size();
    }

}