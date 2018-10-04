package com.cheatdatabase.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cheatdatabase.CheatDatabaseApplication;
import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.events.GameListRecyclerViewClickEvent;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Reachability;
import com.cheatdatabase.helpers.Tools;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.greenrobot.eventbus.EventBus;

import java.util.List;

@EBean
public class GamesBySystemRecycleListViewAdapter extends RecyclerView.Adapter<GamesBySystemRecycleListViewAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter,
        FastScrollRecyclerView.MeasurableAdapter {

    private static final String TAG = GamesBySystemRecycleListViewAdapter.class.getSimpleName();

    private List<Game> gameArrayList;
    private Typeface latoFontBold;
    private Typeface latoFontLight;
    private Game gameObj;

    @RootContext
    Context mContext;

    @App
    CheatDatabaseApplication app;

    public void setGameList(List<Game> gameList) {
        gameArrayList = gameList;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView gameName;
        public TextView cheatCount;
        public OnGameItemClickListener onGameItemClickListener;

        public ViewHolder(View v, OnGameItemClickListener listener) {
            super(v);
            onGameItemClickListener = listener;

            gameName = v.findViewById(R.id.cheat_title);
            cheatCount = v.findViewById(R.id.cheats_count);
            gameName.setTypeface(Tools.getFont(v.getContext().getAssets(), Konstanten.FONT_BOLD));
            cheatCount.setTypeface(Tools.getFont(v.getContext().getAssets(), Konstanten.FONT_LIGHT));

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onGameItemClickListener.onGameClick(this);
        }
    }

    public interface OnGameItemClickListener {
        void onGameClick(GamesBySystemRecycleListViewAdapter.ViewHolder caller);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public GamesBySystemRecycleListViewAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        latoFontBold = Tools.getFont(parent.getContext().getAssets(), Konstanten.FONT_BOLD);
        latoFontLight = Tools.getFont(parent.getContext().getAssets(), Konstanten.FONT_LIGHT);

        // create a new view
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listrow_game_item, parent, false);
        v.setDrawingCacheEnabled(true);

        return new ViewHolder(v, new OnGameItemClickListener() {
            @Override
            public void onGameClick(ViewHolder caller) {

                if (Reachability.reachability.isReachable) {
                    EventBus.getDefault().post(new GameListRecyclerViewClickEvent(gameArrayList.get(caller.getAdapterPosition())));
                } else {
                    EventBus.getDefault().post(new GameListRecyclerViewClickEvent(new Exception()));
                }
            }
        });
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        gameObj = gameArrayList.get(position);

        holder.gameName.setText(gameObj.getGameName());
        holder.gameName.setTypeface(latoFontBold);

        try {
            if (gameObj.getCheatsCount() > 0) {
                holder.cheatCount.setVisibility(View.VISIBLE);
                holder.cheatCount.setText(gameObj.getCheatsCount() + " " + mContext.getResources().getQuantityString(R.plurals.entries, gameObj.getCheatsCount()));
                holder.cheatCount.setTypeface(latoFontLight);
            } else {
                holder.cheatCount.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return gameArrayList.size();
    }

    // Display the first letter of the game during fast scrolling
    @NonNull
    @Override
    public String getSectionName(int position) {
        return gameArrayList.get(position).getGameName().substring(0, 1).toUpperCase();
    }

    // Height of the scroll-bar at the right screen side
    @Override
    public int getViewTypeHeight(RecyclerView recyclerView, @Nullable RecyclerView.ViewHolder viewHolder, int viewType) {
        return 100;
    }

}
