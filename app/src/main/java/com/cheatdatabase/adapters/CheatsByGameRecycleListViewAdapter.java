package com.cheatdatabase.adapters;

import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.events.CheatListRecyclerViewClickEvent;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CheatsByGameRecycleListViewAdapter extends RecyclerView.Adapter<CheatsByGameRecycleListViewAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter,
        FastScrollRecyclerView.MeasurableAdapter {

    private static final String TAG = CheatsByGameRecycleListViewAdapter.class.getSimpleName();

    private Typeface latoFontBold;
    private Typeface latoFontLight;
    private List<Cheat> cheatList;
    private Cheat cheatObj;

    public CheatsByGameRecycleListViewAdapter() {
        cheatList = new ArrayList<>();
    }

    public void setCheats(List<Cheat> cheatList) {
        this.cheatList = cheatList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.cheat_title)
        TextView mCheatTitle;
        @BindView(R.id.small_ratingbar)
        RatingBar mRatingBar;
        @BindView(R.id.newaddition)
        ImageView mFlagNewAddition;
        @BindView(R.id.screenshots)
        ImageView mFlagScreenshot;
        @BindView(R.id.flag)
        ImageView mFlagGerman;

        private OnCheatItemClickListener mListener;

        public ViewHolder(View view, OnCheatItemClickListener listener) {
            super(view);
            ButterKnife.bind(this, view);
            mListener = listener;

            mCheatTitle.setTypeface(Tools.getFont(view.getContext().getAssets(), Konstanten.FONT_REGULAR));

//            mCheatTitle = view.findViewById(R.id.cheat_title);
//            mRatingBar = view.findViewById(R.id.small_ratingbar);
//            mFlagNewAddition = view.findViewById(R.id.newaddition);
//            mFlagScreenshot = view.findViewById(R.id.screenshots);
//            mFlagGerman = view.findViewById(R.id.flag);

            mFlagNewAddition.setImageResource(R.drawable.flag_new);
            mFlagScreenshot.setImageResource(R.drawable.flag_img);
            mFlagGerman.setImageResource(R.drawable.flag_german);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onCheatClick(this);
        }

    }

    public interface OnCheatItemClickListener {
        void onCheatClick(CheatsByGameRecycleListViewAdapter.ViewHolder caller);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CheatsByGameRecycleListViewAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

        latoFontBold = Tools.getFont(parent.getContext().getAssets(), Konstanten.FONT_BOLD);
        latoFontLight = Tools.getFont(parent.getContext().getAssets(), Konstanten.FONT_LIGHT);

        // create a new view
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listrow_cheat_item, parent, false);
        v.setDrawingCacheEnabled(true);

        return new ViewHolder(v, new OnCheatItemClickListener() {
            @Override
            public void onCheatClick(ViewHolder caller) {
                if (Reachability.reachability.isReachable) {

                    Log.d(TAG, "caller.getAdapterPosition(): " + caller.getAdapterPosition());
                    Log.d(TAG, "Cheat Title: " + cheatList.get(caller.getAdapterPosition()).getCheatTitle());

                    EventBus.getDefault().post(new CheatListRecyclerViewClickEvent(cheatList.get(caller.getAdapterPosition()), caller.getAdapterPosition()));
                } else {
                    EventBus.getDefault().post(new CheatListRecyclerViewClickEvent(new Exception()));
                }
            }
        });
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cheatObj = cheatList.get(position);

        holder.mCheatTitle.setText(cheatObj.getCheatTitle());
        holder.mCheatTitle.setTypeface(latoFontBold);

        holder.mRatingBar.setRating(cheatObj.getRatingAverage() / 2);

        if (cheatObj.getDayAge() < Konstanten.CHEAT_DAY_AGE_SHOW_NEWADDITION_ICON) {
            holder.mFlagNewAddition.setVisibility(View.VISIBLE);
        } else {
            holder.mFlagNewAddition.setVisibility(View.GONE);
        }

        if (cheatObj.isScreenshots()) {
            holder.mFlagScreenshot.setVisibility(View.VISIBLE);
        } else {
            holder.mFlagScreenshot.setVisibility(View.GONE);
        }

        if (cheatObj.getLanguageId() == 2) { // 2 = German
            holder.mFlagGerman.setVisibility(View.VISIBLE);
        } else {
            holder.mFlagGerman.setVisibility(View.GONE);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return cheatList.size();
    }

    // Display the first letter of the cheat during fast scrolling
    @NonNull
    @Override
    public String getSectionName(int position) {
        return cheatList.get(position).getCheatTitle().substring(0, 1).toUpperCase();
    }

    // Height of the scroll-bar at the right screen side
    @Override
    public int getViewTypeHeight(RecyclerView recyclerView, @Nullable RecyclerView.ViewHolder viewHolder, int viewType) {
        return 100;
    }

}
