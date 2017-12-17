package com.cheatdatabase.adapters;

import android.content.Context;
import android.graphics.Typeface;
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

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

@EBean
public class GamesBySystemRecycleListViewAdapter extends RecyclerView.Adapter<GamesBySystemRecycleListViewAdapter.ViewHolder> {

    private static final String TAG = GamesBySystemRecycleListViewAdapter.class.getSimpleName();

    private ArrayList<Game> mGames;
    private Typeface latoFontBold;
    private Typeface latoFontLight;
    private Game gameObj;

    @RootContext
    Context mContext;

    @App
    CheatDatabaseApplication app;

    public void init(ArrayList<Game> gameList) {
        mGames = gameList;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mGameName;
        public TextView mCheatCount;
        public IMyViewHolderClicks mListener;

        public ViewHolder(View v, IMyViewHolderClicks listener) {
            super(v);
            mListener = listener;

            mGameName = v.findViewById(R.id.cheat_title);
            mCheatCount = v.findViewById(R.id.cheats_count);
            mGameName.setTypeface(Tools.getFont(v.getContext().getAssets(), Konstanten.FONT_BOLD));
            mCheatCount.setTypeface(Tools.getFont(v.getContext().getAssets(), Konstanten.FONT_LIGHT));

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onGameClick(this);
        }

    }

    public interface IMyViewHolderClicks {
        void onGameClick(GamesBySystemRecycleListViewAdapter.ViewHolder caller);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public GamesBySystemRecycleListViewAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        latoFontBold = Tools.getFont(parent.getContext().getAssets(), Konstanten.FONT_BOLD);
        latoFontLight = Tools.getFont(parent.getContext().getAssets(), Konstanten.FONT_LIGHT);

        // create a new view
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listrow_gamelist, parent, false);
        v.setDrawingCacheEnabled(true);

        return new ViewHolder(v, new IMyViewHolderClicks() {
            @Override
            public void onGameClick(ViewHolder caller) {

                if (Reachability.reachability.isReachable) {
                    EventBus.getDefault().post(new GameListRecyclerViewClickEvent(mGames.get(caller.getAdapterPosition())));
                } else {
                    EventBus.getDefault().post(new GameListRecyclerViewClickEvent(new Exception()));
                }
            }
        });
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        gameObj = mGames.get(position);

        holder.mGameName.setText(gameObj.getGameName());
        holder.mGameName.setTypeface(latoFontBold);

        try {
            if (gameObj.getCheatsCount() > 0) {
                holder.mCheatCount.setVisibility(View.VISIBLE);
                holder.mCheatCount.setText(gameObj.getCheatsCount() + " " + mContext.getResources().getQuantityString(R.plurals.set_cheats, gameObj.getCheatsCount()));
                holder.mCheatCount.setTypeface(latoFontLight);
            } else {
                holder.mCheatCount.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mGames.size();
    }
}
