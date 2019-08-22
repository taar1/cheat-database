package com.cheatdatabase.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.holders.BlankWhiteListViewItemHolder;
import com.cheatdatabase.holders.GamesBySystemListViewItemHolder;
import com.cheatdatabase.listeners.OnGameListItemSelectedListener;
import com.cheatdatabase.listitems.BlankListItem;
import com.cheatdatabase.listitems.GameListItem;
import com.cheatdatabase.listitems.ListItem;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

import needle.Needle;

public class GamesBySystemRecycleListViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter,
        FastScrollRecyclerView.MeasurableAdapter {

    private static final String TAG = GamesBySystemRecycleListViewAdapter.class.getSimpleName();

    private List<Game> gameList;
    private List<ListItem> listItems;
    //    private Typeface latoFontBold;
//    private Typeface latoFontLight;
    private Game gameObj;
    private Context context;
    private final OnGameListItemSelectedListener listener;

    public GamesBySystemRecycleListViewAdapter(Context context, OnGameListItemSelectedListener listener) {
        Log.d(TAG, "XXXXX ADAPTER GamesBySystemRecycleListViewAdapter()");
        this.context = context;
        this.listener = listener;
        gameList = new ArrayList<>();
        listItems = new ArrayList<>();

//        latoFontBold = Tools.getFont(context.getAssets(), Konstanten.FONT_BOLD);
//        latoFontLight = Tools.getFont(context.getAssets(), Konstanten.FONT_LIGHT);

        // TODO FIXME try out if this is needed or not....
        filterList("");
    }

    public void setGameList(List<Game> gameList) {
        Log.d(TAG, "XXXXX ADAPTER setGameList: " + gameList.size());

        this.gameList = gameList;
    }

//    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
//        @BindView(R.id.cheat_title)
//        TextView gameName;
//        @BindView(R.id.cheats_count)
//        TextView cheatCount;
//
//        public OnGameItemClickListener onGameItemClickListener;
//
//        public ViewHolder(View view, OnGameItemClickListener listener) {
//            super(view);
//            ButterKnife.bind(this, view);
//            onGameItemClickListener = listener;
//
//            gameName.setTypeface(Tools.getFont(view.getContext().getAssets(), Konstanten.FONT_BOLD));
//            cheatCount.setTypeface(Tools.getFont(view.getContext().getAssets(), Konstanten.FONT_LIGHT));
//
//            view.setOnClickListener(this);
//        }
//
//        @Override
//        public void onClick(View v) {
//            onGameItemClickListener.onGameClick(this);
//        }
//    }

    @Override
    public int getItemViewType(int position) {
        Log.d(TAG, "XXXXX ADAPTER getItemViewType(): " + listItems.get(position).getType());
        return listItems.get(position).getType();
    }

//    public interface OnGameItemClickListener {
//        void onGameClick(GamesBySystemRecycleListViewAdapter.ViewHolder caller);
//    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        Log.d(TAG, "XXXXX ADAPTER onCreateViewHolder()");

        if (viewType == ListItem.TYPE_GAME) {
            Log.d(TAG, "XXXXX ADAPTER onCreateViewHolder() GAME");

            final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listrow_gamebysystem_item, parent, false);
            itemView.setDrawingCacheEnabled(true);

            Log.d(TAG, "XXXXX ADAPTER onCreateViewHolder() RETURNS GAME");
            return new GamesBySystemListViewItemHolder(itemView, context);

//            return new ViewHolder(v, caller -> {
//
//                if (Reachability.reachability.isReachable) {
//                    EventBus.getDefault().post(new GameListRecyclerViewClickEvent(gameList.get(caller.getAdapterPosition())));
//                } else {
//                    EventBus.getDefault().post(new GameListRecyclerViewClickEvent(new Exception()));
//                }
//            });
        } else if (viewType == ListItem.TYPE_BLANK) {
            return new BlankWhiteListViewItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.listrow_blankwhite_item, parent, false));
        }

        Log.d(TAG, "XXXXX ADAPTER onCreateViewHolder() RETURNS NULL");
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        Log.d(TAG, "XXXXX ADAPTER onBindViewHolder() TYPE: " + type);

        if (type == ListItem.TYPE_GAME) {
            final GameListItem gameListItem = (GameListItem) listItems.get(position);
            GamesBySystemListViewItemHolder gamesBySystemListViewItemHolder = (GamesBySystemListViewItemHolder) holder;
            gamesBySystemListViewItemHolder.setGame(gameListItem.getGame());
            gamesBySystemListViewItemHolder.view.setOnClickListener(v -> listener.onGameListItemSelected(gameListItem.getGame()));

        } else if (type == ListItem.TYPE_FACEBOOK_NATIVE_AD) {
            // TODO
        } else if (type == ListItem.TYPE_BLANK) {
            // TODO
            BlankWhiteListViewItemHolder blankWhiteListViewItemHolder = (BlankWhiteListViewItemHolder) holder;
        }

//        gameObj = gameList.get(position);
//
//        holder.gameName.setText(gameObj.getGameName());
//        holder.gameName.setTypeface(latoFontBold);
//
//        try {
//            if (gameObj.getCheatsCount() > 0) {
//                holder.cheatCount.setVisibility(View.VISIBLE);
//                holder.cheatCount.setText(gameObj.getCheatsCount() + " " + context.getResources().getQuantityString(R.plurals.entries, gameObj.getCheatsCount()));
//                holder.cheatCount.setTypeface(latoFontLight);
//            } else {
//                holder.cheatCount.setVisibility(View.GONE);
//            }
//        } catch (Exception e) {
//            Log.e(TAG, e.getLocalizedMessage());
//        }
    }

    @Override
    public int getItemCount() {
        //Log.d(TAG, "XXXXX ADAPTER getItemCount(): " + listItems.size());
        return listItems.size();
    }

    // Display the first letter of the game during fast scrolling
    @NonNull
    @Override
    public String getSectionName(int position) {
        Log.d(TAG, "XXXXX ADAPTER getSectionName()");
        int type = getItemViewType(position);
        if (type == ListItem.TYPE_GAME) {
            return gameList.get(position).getGameName().substring(0, 1).toUpperCase();
        } else {
            return "Ad";
        }
    }

    // Height of the scroll-bar at the right screen side
    @Override
    public int getViewTypeHeight(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int viewType) {
        return 100;
    }

    public void filterList(String filter) {
        Log.d(TAG, "XXXXX ADAPTER filterList()");

        if ((filter != null) && (filter.trim().length() > 2)) {
            // TODO filter the list and update gameList with filtered List
        }

        updateGameListAndInjectAds();
    }

    private void updateGameListAndInjectAds() {
        Log.d(TAG, "XXXXX ADAPTER updateGameListAndInjectAds()");

        int j = 0;
        final List<ListItem> newListItems = new ArrayList<>();

        Log.d(TAG, "XXXXX updateGameListAndInjectAds gameList: " + gameList.size());
        for (Game game : gameList) {


            Log.d(TAG, "XXXXX ADD GAME TO LIST: " + game.getGameName());
            GameListItem gameListItem = new GameListItem();
            gameListItem.setGame(game);
            newListItems.add(gameListItem);

            // TODO inject ad here to newListItems

            if (j % Konstanten.INJECT_AD_AFTER_EVERY_POSITION == Konstanten.INJECT_AD_AFTER_EVERY_POSITION - 1) {
                newListItems.add(new BlankListItem());
            }
            j++;
        }

        Needle.onMainThread().execute(() -> {
            listItems.clear();
            listItems.addAll(newListItems);
            notifyDataSetChanged();
        });
    }
}
