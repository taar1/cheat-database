package com.cheatdatabase.holders;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.helpers.Konstanten;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeAdView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FacebookNativeAdListViewItemHolder extends RecyclerView.ViewHolder {
    private static final String TAG = FacebookNativeAdListViewItemHolder.class.getSimpleName();

    private final NativeAd nativeAd;
    public View view;
    private Context context;

    @BindView(R.id.outer_layout)
    LinearLayout outerLayout;
    @BindView(R.id.native_ad_container)
    NativeAdLayout nativeAdContainer;

    public FacebookNativeAdListViewItemHolder(View view, Context context) {
        super(view);
        ButterKnife.bind(this, view);

        this.view = view;
        this.context = context;

        Log.d(TAG, "XXXXX FacebookNativeAdListViewItemHolder()");

        nativeAd = new NativeAd(context, Konstanten.FACEBOOK_AUDIENCE_NETWORK_NATIVE_AD_IN_RECYCLER_VIEW);

        nativeAd.setAdListener(new NativeAdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                Log.d(TAG, "XXXXX FacebookNativeAdListViewItemHolder onError(): " + adError.getErrorMessage());

                // TODO FIXME hier gibt es immer einen fehler beim laden der ads....
                // TODO FIXME hier gibt es immer einen fehler beim laden der ads....
                // TODO FIXME hier gibt es immer einen fehler beim laden der ads....
                // TODO FIXME hier gibt es immer einen fehler beim laden der ads....
                // TODO FIXME hier gibt es immer einen fehler beim laden der ads....

                //outerLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.d(TAG, "XXXXX FacebookNativeAdListViewItemHolder onAdLoaded()");

                // Render the Native Ad Template
                View adView = NativeAdView.render(context, nativeAd);
                // Add the Native Ad View to your ad container.
                // The recommended dimensions for the ad container are:
                // Width: 280dp - 500dp
                // Height: 250dp - 500dp
                // The template, however, will adapt to the supplied dimensions.
                nativeAdContainer.addView(adView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800));
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.d(TAG, "XXXXX FacebookNativeAdListViewItemHolder onAdClicked()");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.d(TAG, "XXXXX FacebookNativeAdListViewItemHolder onLoggingImpression()");
            }

            @Override
            public void onMediaDownloaded(Ad ad) {
                Log.d(TAG, "XXXXX FacebookNativeAdListViewItemHolder onMediaDownloaded()");
            }
        });

        // Initiate a request to load an ad.
        nativeAd.loadAd();
    }

}
