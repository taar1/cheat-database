package com.cheatdatabase.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.data.model.Game;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;
import com.cheatdatabase.holders.FacebookNativeAdHolder;
import com.cheatdatabase.holders.GamesBySystemListViewItemHolder;
import com.cheatdatabase.holders.UkonAdListViewItemHolder;
import com.cheatdatabase.listeners.OnGameListItemSelectedListener;
import com.cheatdatabase.listitems.FacebookNativeAdListItem;
import com.cheatdatabase.listitems.GameListItem;
import com.cheatdatabase.listitems.ListItem;
import com.cheatdatabase.listitems.UkonAdListItem;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdsManager;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

import needle.Needle;

public class GamesBySystemRecycleListViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter,
        FastScrollRecyclerView.MeasurableAdapter {

    private static final String TAG = GamesBySystemRecycleListViewAdapter.class.getSimpleName();

    private List<Game> gameList;
    private List<ListItem> listItems;
    private Activity activity;
    private final OnGameListItemSelectedListener listener;

    private NativeAdsManager mNativeAdsManager;

    private Tools tools;

    public GamesBySystemRecycleListViewAdapter(Activity activity, NativeAdsManager nativeAdsManager, Tools tools, OnGameListItemSelectedListener listener) {
        this.activity = activity;
        this.mNativeAdsManager = nativeAdsManager;
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
            return listItems.get(position).getType();
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
        } else if (viewType == ListItem.TYPE_FACEBOOK_NATIVE_AD) {
            NativeAdLayout inflatedView = (NativeAdLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.native_ad_unit, parent, false);
            return new FacebookNativeAdHolder(inflatedView);
        } else if (viewType == ListItem.TYPE_UKON_NO_CHIKARA) {
            final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ukon_custom_ad, parent, false);
            return new UkonAdListViewItemHolder(itemView);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        Log.d(TAG, "ADAPTER onBindViewHolder() TYPE: " + type);

        if (type == ListItem.TYPE_GAME) {
            final GameListItem gameListItem = (GameListItem) listItems.get(position);
            GamesBySystemListViewItemHolder gamesBySystemListViewItemHolder = (GamesBySystemListViewItemHolder) holder;
            gamesBySystemListViewItemHolder.setGame(gameListItem.getGame());
            gamesBySystemListViewItemHolder.view.setOnClickListener(v -> listener.onGameListItemSelected(gameListItem.getGame()));
        } else if (type == ListItem.TYPE_FACEBOOK_NATIVE_AD) {
            NativeAd ad = mNativeAdsManager.nextNativeAd();

            FacebookNativeAdHolder facebookNativeAdHolder = (FacebookNativeAdHolder) holder;
            facebookNativeAdHolder.adChoicesContainer.removeAllViews();

            if (ad != null) {
                facebookNativeAdHolder.tvAdTitle.setText(ad.getAdvertiserName());
                facebookNativeAdHolder.tvAdBody.setText(ad.getAdBodyText());
                facebookNativeAdHolder.tvAdSocialContext.setText(ad.getAdSocialContext());
                facebookNativeAdHolder.tvAdSponsoredLabel.setText("Sponsored");
                facebookNativeAdHolder.btnAdCallToAction.setText(ad.getAdCallToAction());
                facebookNativeAdHolder.btnAdCallToAction.setVisibility(ad.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
                AdOptionsView adOptionsView = new AdOptionsView(activity, ad, facebookNativeAdHolder.nativeAdLayout);
                facebookNativeAdHolder.adChoicesContainer.addView(adOptionsView, 0);

                List<View> clickableViews = new ArrayList<>();
                clickableViews.add(facebookNativeAdHolder.ivAdIcon);
                clickableViews.add(facebookNativeAdHolder.mvAdMedia);
                clickableViews.add(facebookNativeAdHolder.btnAdCallToAction);
                ad.registerViewForInteraction(
                        facebookNativeAdHolder.nativeAdLayout,
                        facebookNativeAdHolder.mvAdMedia,
                        facebookNativeAdHolder.ivAdIcon,
                        clickableViews);
            }
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
            return listItems.get(position).getTitle().toUpperCase();
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

        updateGameListAndInjectFacebookAds();
    }

    private void updateGameListAndInjectFacebookAds() {
        int j = 0;
        int adCounter = 0;
        final List<ListItem> newListItems = new ArrayList<>();
        boolean showUkonAds = true;

        if (tools.getCountryCode(activity).equalsIgnoreCase("us")) {
            showUkonAds = false;
        }

        for (Game game : gameList) {
            GameListItem gameListItem = new GameListItem();
            gameListItem.setGame(game);
            newListItems.add(gameListItem);

            if (j % Konstanten.INJECT_AD_AFTER_EVERY_POSITION == Konstanten.INJECT_AD_AFTER_EVERY_POSITION - 1) {

                if (showUkonAds) {
                    if (adCounter == 1) {
                        newListItems.add(new UkonAdListItem());
                    } else {
                        newListItems.add(new FacebookNativeAdListItem());
                    }

                    adCounter++;
                } else {
                    newListItems.add(new FacebookNativeAdListItem());
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
