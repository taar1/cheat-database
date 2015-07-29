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

import com.cheatdatabase.MemberCheatListActivity;
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

        //        public TextView mSystemName;
//        public TextView mSubtitle;
//        public LinearLayout mLinearLayout;
//        public IMyViewHolderClicks mListener;

        TextView tvNumeration;
        TextView tvMemberName;
        TextView tvCheatCount;
        TextView tvHiMessage;
        TextView tvWebsite;
        ImageView avatarImageView;

        public ViewHolder(View v) {
            super(v);
//            mListener = listener;

//            mLinearLayout = (LinearLayout) v.findViewById(R.id.ll);
//            mSystemName = (TextView) v.findViewById(R.id.system_name);
//            mSubtitle = (TextView) v.findViewById(R.id.subtitle);

            tvNumeration = (TextView) v.findViewById(R.id.numeration);

            tvMemberName = (TextView) v.findViewById(R.id.membername);
//            tvMemberName.setOnClickListener(this);

            tvCheatCount = (TextView) v.findViewById(R.id.cheatcount);
//            tvCheatCount.setOnClickListener(this);

            tvHiMessage = (TextView) v.findViewById(R.id.hi_message);

            tvWebsite = (TextView) v.findViewById(R.id.website);
//            tvWebsite.setOnClickListener(this);

            avatarImageView = (ImageView) v.findViewById(R.id.avatar);
//            avatarImageView.setOnClickListener(this);

        }

//        @Override
//        public void onClick(View v) {
//            if (v == tvMemberName) {
//                mListener.onShowCheatsClick(this);
//            } else if (v == tvCheatCount) {
//                mListener.onShowCheatsClick(this);
//            } else if (v == tvWebsite) {
//                mListener.onWebsiteClick(this, (String) tvWebsite.getText());
//            } else if (v == avatarImageView) {
////                mListener.onAvatarClick(this);
//                mListener.onWebsiteClick(this, (String) tvWebsite.getText());
//            }
//
//        }

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
        if (Reachability.reachability.isReachable) {
            Intent explicitIntent = new Intent(mContext, MemberCheatListActivity.class);
            explicitIntent.putExtra("memberObj", member);
            mContext.startActivity(explicitIntent);
        } else {
            Toast.makeText(mContext, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

//    public static interface IMyViewHolderClicks {
//
//        public void onShowCheatsClick(TopMembersListViewAdapter.ViewHolder caller, Member member);
//
////        public void onAvatarClick(TopMembersListViewAdapter.ViewHolder caller);
//
//        public void onWebsiteClick(TopMembersListViewAdapter.ViewHolder caller, String url);
//    }


    // Create new views (invoked by the layout manager)
    @Override
    public TopMembersListViewAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

        latoFontBold = Tools.getFont(parent.getContext().getAssets(), Konstanten.FONT_BOLD);
        latoFontLight = Tools.getFont(parent.getContext().getAssets(), Konstanten.FONT_LIGHT);

        // create a new view
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.topmembers_list_item, parent, false);
        v.setDrawingCacheEnabled(true);

//        ViewHolder vhx = new ViewHolder(v, new TopMembersListViewAdapter.IMyViewHolderClicks() {
//
//            @Override
//            public void onShowCheatsClick(ViewHolder caller, Member member) {
//                if (Reachability.reachability.isReachable) {
//                    Intent explicitIntent = new Intent(mContext, MemberCheatListActivity.class);
//                    explicitIntent.putExtra("memberObj", member);
//                    mContext.startActivity(explicitIntent);
//                } else {
//                    Toast.makeText(mContext, R.string.no_internet, Toast.LENGTH_SHORT).show();
//                }
//            }
//
////            @Override
////            public void onAvatarClick(TopMembersListViewAdapter.ViewHolder caller) {
////
////                // TODO make proper onclick action
////                if (Reachability.reachability.isReachable) {
//////                    CheatDatabaseApplication.getEventBus().post(new SystemListRecyclerViewClickEvent(mMemberObjects.get(caller.getAdapterPosition())));
////                } else {
//////                    CheatDatabaseApplication.getEventBus().post(new SystemListRecyclerViewClickEvent(new Exception()));
////                }
////            }
//
//
//            @Override
//            public void onWebsiteClick(ViewHolder caller, String url) {
//                openWebsite(url);
//            }
//        });

        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    public void onBindViewHolder(ViewHolder holder, final int position) {
        memberObj = mMemberObjects.get(position);

        holder.tvNumeration.setTypeface(latoFontBold);
        holder.tvNumeration.setText(String.valueOf(position + 1));

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

        holder.tvNumeration.setText(String.valueOf(position + 1));

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