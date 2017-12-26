package com.cheatdatabase.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.events.CheatListRecyclerViewClickEvent;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

@EBean
public class MemberCheatRecycleListViewAdapter extends RecyclerView.Adapter<MemberCheatRecycleListViewAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter,
        FastScrollRecyclerView.MeasurableAdapter {

    private static final String TAG = MemberCheatRecycleListViewAdapter.class.getSimpleName();

    private ArrayList<Cheat> mCheats;
    private Typeface latoFontRegular;
    private Cheat cheatObj;

    @RootContext
    Context mContext;

    @Bean
    Tools tools;

    public void init(ArrayList<Cheat> cheatList) {
        mCheats = cheatList;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a vie1w holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView tvGameName;
        public TextView tvCheatTitle;
        public LinearLayout flagLayout;
        public ImageView screenshotFlag;
        public ImageView germanFlag;
        public IMyViewHolderClicks mListener;

        public ViewHolder(View v, IMyViewHolderClicks listener) {
            super(v);
            mListener = listener;

            tvGameName = v.findViewById(R.id.gamename);
            tvCheatTitle = v.findViewById(R.id.cheattitle);
            screenshotFlag = v.findViewById(R.id.ivMap);
            germanFlag = v.findViewById(R.id.ivFlag);
            flagLayout = v.findViewById(R.id.flag_layout);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onCheatClick(this);
        }
    }

    public interface IMyViewHolderClicks {
        void onCheatClick(MemberCheatRecycleListViewAdapter.ViewHolder caller);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MemberCheatRecycleListViewAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

        latoFontRegular = tools.getFont(parent.getContext().getAssets(), Konstanten.FONT_REGULAR);

        // create a new view
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listrow_member_cheat_item, parent, false);
        v.setDrawingCacheEnabled(true);

        return new ViewHolder(v, new IMyViewHolderClicks() {
            @Override
            public void onCheatClick(ViewHolder caller) {
                if (Reachability.reachability.isReachable) {

                    Log.d(TAG, "caller.getAdapterPosition(): " + caller.getAdapterPosition());
                    Log.d(TAG, "Cheat Title: " + mCheats.get(caller.getAdapterPosition()).getCheatTitle());

                    EventBus.getDefault().post(new CheatListRecyclerViewClickEvent(mCheats.get(caller.getAdapterPosition()), caller.getAdapterPosition()));
                } else {
                    EventBus.getDefault().post(new CheatListRecyclerViewClickEvent(new Exception()));
                }
            }
        });
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cheatObj = mCheats.get(position);

        holder.tvCheatTitle.setTypeface(latoFontRegular);
        holder.tvGameName.setText(cheatObj.getGameName() + " (" + cheatObj.getSystemName() + ")");

        if (holder.tvCheatTitle != null) {
            holder.tvCheatTitle.setText(cheatObj.getCheatTitle());
        }

        if ((!cheatObj.isScreenshots()) && (cheatObj.getLanguageId() != Konstanten.GERMAN)) {
            holder.flagLayout.setVisibility(View.GONE);
        } else {
            if (cheatObj.isScreenshots()) {
                holder.screenshotFlag.setImageResource(R.drawable.flag_img);
            } else {
                holder.screenshotFlag.setVisibility(View.GONE);
            }

            if (cheatObj.getLanguageId() == Konstanten.GERMAN) {
                holder.germanFlag.setImageResource(R.drawable.flag_german);
            } else {
                holder.germanFlag.setVisibility(View.GONE);
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mCheats.size();
    }

    // Display the first letter of the game during fast scrolling
    @NonNull
    @Override
    public String getSectionName(int position) {
        return mCheats.get(position).getGameName().substring(0, 1).toUpperCase();
    }

    // Height of the scroll-bar at the right screen side
    @Override
    public int getViewTypeHeight(RecyclerView recyclerView, int viewType) {
        return 100;
    }
}
