package com.cheatdatabase.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.data.model.Game;
import com.cheatdatabase.databinding.IncludeApplovinMaxadviewNativeBinding;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.holders.ApplovinNativeAdListViewItemHolder;
import com.cheatdatabase.holders.GamesBySystemListViewItemHolder;
import com.cheatdatabase.holders.UkonAdListViewItemHolder;
import com.cheatdatabase.listeners.OnGameListItemSelectedListener;
import com.cheatdatabase.listitems.ApplovinNativeAdListItem;
import com.cheatdatabase.listitems.GameListItem;
import com.cheatdatabase.listitems.ListItem;
import com.cheatdatabase.listitems.UkonAdListItem;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import needle.Needle;

public class GamesBySystemRecycleListViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter,
        FastScrollRecyclerView.MeasurableAdapter {

    private static final String TAG = GamesBySystemRecycleListViewAdapter.class.getSimpleName();

    private List<Game> gameList;
    private final List<ListItem> listItems;
    private final Activity activity;
    private final OnGameListItemSelectedListener listener;

    private final Tools tools;

    public GamesBySystemRecycleListViewAdapter(Activity activity, Tools tools, OnGameListItemSelectedListener listener) {
        this.activity = activity;
        this.tools = tools;
        this.listener = listener;
        gameList = new ArrayList<>();
        listItems = new ArrayList<>();

        filterList(""); // Pass String for filtering
    }

    public void setGameList(List<Game> gameList) {
        this.gameList = gameList;
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= 0) {
            return listItems.get(position).type();
        } else {
            return ListItem.TYPE_GAME;
        }
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

        if (viewType == ListItem.TYPE_GAME) {
            final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listrow_gamebysystem_item, parent, false);
            itemView.setDrawingCacheEnabled(true);
            return new GamesBySystemListViewItemHolder(itemView, activity);
        } else if (viewType == ListItem.TYPE_APPLOVIN_NATIVE) {
            IncludeApplovinMaxadviewNativeBinding binding = IncludeApplovinMaxadviewNativeBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new ApplovinNativeAdListViewItemHolder(binding);
        } else if (viewType == ListItem.TYPE_UKON_NO_CHIKARA) {
            final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ukon_custom_ad, parent, false);
            return new UkonAdListViewItemHolder(itemView);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        if (type == ListItem.TYPE_GAME) {
            final GameListItem gameListItem = (GameListItem) listItems.get(position);
            GamesBySystemListViewItemHolder gamesBySystemListViewItemHolder = (GamesBySystemListViewItemHolder) holder;
            gamesBySystemListViewItemHolder.setGame(Objects.requireNonNull(gameListItem.getGame()));
            gamesBySystemListViewItemHolder.view.setOnClickListener(v -> listener.onGameListItemSelected(gameListItem.getGame()));
        } else if (type == ListItem.TYPE_APPLOVIN_NATIVE) {
            ApplovinNativeAdListViewItemHolder applovinNativeAdListViewItemHolder = (ApplovinNativeAdListViewItemHolder) holder;
            applovinNativeAdListViewItemHolder.showIt();
        } else if (type == ListItem.TYPE_UKON_NO_CHIKARA) {
            UkonAdListViewItemHolder ukonAdListViewItemHolder = (UkonAdListViewItemHolder) holder;
            ukonAdListViewItemHolder.view.setOnClickListener(v -> {
                Uri uri = Uri.parse(activity.getString(R.string.ukon_url));
                Intent intentMoreApps = new Intent(Intent.ACTION_VIEW, uri);
                activity.startActivity(intentMoreApps);
            });
        }
    }


    // Display the first letter of the game during fast scrolling
    @NonNull
    @Override
    public String getSectionName(int position) {
        // What will be displayed at the right side when fast scroll is used (normally the first letter of the game)
        int type = getItemViewType(position);
        if (type == ListItem.TYPE_GAME) {
            return listItems.get(position).title().toUpperCase();
        } else {
            // When we show an ad or something else we show blank
            return "";
        }
    }

    // Height of the scroll-bar at the right screen side
    @Override
    public int getViewTypeHeight(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int viewType) {
        return 100;
    }

    // Filter List by search word (not implemented yet)
    public void filterList(String filter) {
        if ((filter != null) && (filter.trim().length() > 2)) {
            // TODO filter the list and update gameList with filtered List
        }

        updateGameListAndInjectNativeListAds();
    }

    private void updateGameListAndInjectNativeListAds() {
        int j = 0;
        int adCounter = 0;
        final List<ListItem> newListItems = new ArrayList<>();
        boolean showUkonAds = !tools.getCountryCode(activity).equalsIgnoreCase("us");

        for (Game game : gameList) {
            GameListItem gameListItem = new GameListItem();
            gameListItem.setGame(game);
            newListItems.add(gameListItem);

            if (j % Konstanten.INJECT_AD_AFTER_EVERY_POSITION == Konstanten.INJECT_AD_AFTER_EVERY_POSITION - 1) {

                if (showUkonAds) {
                    if (adCounter == 1) {
                        newListItems.add(new UkonAdListItem());
                    } else {
                        newListItems.add(new ApplovinNativeAdListItem());
                    }

                    adCounter++;
                } else {
                    newListItems.add(new ApplovinNativeAdListItem());
                }
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
