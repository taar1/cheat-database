package com.cheatdatabase.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.holders.CheatsByGameListViewItemHolder;
import com.cheatdatabase.holders.FacebookNativeAdHolder;
import com.cheatdatabase.listeners.OnCheatListItemSelectedListener;
import com.cheatdatabase.listitems.CheatListItem;
import com.cheatdatabase.listitems.FacebookNativeAdListItem;
import com.cheatdatabase.listitems.ListItem;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdsManager;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

import needle.Needle;

public class CheatsByGameRecycleListViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter,
        FastScrollRecyclerView.MeasurableAdapter {

    private static final String TAG = CheatsByGameRecycleListViewAdapter.class.getSimpleName();

    private List<Cheat> cheatList;
    private List<ListItem> listItems;
    private Context context;

    private OnCheatListItemSelectedListener listener;

    private NativeAdsManager mNativeAdsManager;

    public CheatsByGameRecycleListViewAdapter(Activity activity, OnCheatListItemSelectedListener listener) {
        this.context = activity;
        this.listener = listener;
        cheatList = new ArrayList<>();
        listItems = new ArrayList<>();

        // TODO at some point implement a cheat filter function
        filterList("");
    }

    public CheatsByGameRecycleListViewAdapter(Activity activity, NativeAdsManager nativeAdsManager, OnCheatListItemSelectedListener listener) {
        this.context = activity;
        this.mNativeAdsManager = nativeAdsManager;
        this.listener = listener;
        cheatList = new ArrayList<>();
        listItems = new ArrayList<>();

        // TODO at some point implement a cheat filter function
        filterList("");
    }

    public void setCheatList(List<Cheat> cheatList) {
        this.cheatList = cheatList;
    }

    @Override
    public int getItemViewType(int position) {
        return listItems.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

        if (viewType == ListItem.TYPE_CHEAT) {
            final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listrow_cheat_item, parent, false);
            itemView.setDrawingCacheEnabled(true);
            return new CheatsByGameListViewItemHolder(itemView, context);
        } else if (viewType == ListItem.TYPE_FACEBOOK_NATIVE_AD) {
            NativeAdLayout inflatedView = (NativeAdLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.native_ad_unit, parent, false);
            return new FacebookNativeAdHolder(inflatedView);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        Log.d(TAG, "XXXXX ADAPTER onBindViewHolder() TYPE: " + type);

        if (type == ListItem.TYPE_CHEAT) {
            final CheatListItem cheatListItem = (CheatListItem) listItems.get(position);
            CheatsByGameListViewItemHolder cheatsByGameListViewItemHolder = (CheatsByGameListViewItemHolder) holder;
            cheatsByGameListViewItemHolder.setCheat(cheatListItem.getCheat());
            cheatsByGameListViewItemHolder.view.setOnClickListener(v -> listener.onCheatListItemSelected(cheatListItem.getCheat(), position));
        } else if (type == ListItem.TYPE_FACEBOOK_NATIVE_AD) {
            if (mNativeAdsManager != null) {
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
                    AdOptionsView adOptionsView = new AdOptionsView(context, ad, facebookNativeAdHolder.nativeAdLayout);
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
            }
        }
    }

    // Display the first letter of the game during fast scrolling
    @NonNull
    @Override
    public String getSectionName(int position) {
        // What will be displayed at the right side when fast scroll is used (normally the first letter of the game)
        int type = getItemViewType(position);
        if (type == ListItem.TYPE_CHEAT) {
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

    // Filter List by search qord (not implemented yet)
    public void filterList(String filter) {
        if ((filter != null) && (filter.trim().length() > 2)) {
            // TODO filter the list and update gameList with filtered List
        }

        updateCheatListAndInjectFacebookAds();
    }

    private void updateCheatListAndInjectFacebookAds() {
        int j = 0;
        final List<ListItem> newListItems = new ArrayList<>();

        for (Cheat cheat : cheatList) {
            CheatListItem cheatListItem = new CheatListItem();
            cheatListItem.setCheat(cheat);
            newListItems.add(cheatListItem);

            if (mNativeAdsManager != null) {
                if (j % Konstanten.INJECT_AD_AFTER_EVERY_POSITION == Konstanten.INJECT_AD_AFTER_EVERY_POSITION - 1) {
                    newListItems.add(new FacebookNativeAdListItem());
                }
                j++;
            }
        }

        Needle.onMainThread().execute(() -> {
            listItems.clear();
            listItems.addAll(newListItems);
            notifyDataSetChanged();
        });
    }

    public void updateCheatListWithoutAds() {
        final List<ListItem> newListItems = new ArrayList<>();

        for (Cheat cheat : cheatList) {
            CheatListItem cheatListItem = new CheatListItem();
            cheatListItem.setCheat(cheat);
            newListItems.add(cheatListItem);
        }

        Needle.onMainThread().execute(() -> {
            listItems.clear();
            listItems.addAll(newListItems);
            notifyDataSetChanged();
        });
    }

}
