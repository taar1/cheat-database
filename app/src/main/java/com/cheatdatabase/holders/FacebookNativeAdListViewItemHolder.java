package com.cheatdatabase.holders;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.cheatdatabase.helpers.Konstanten;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdBase;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeAdView;

import java.util.ArrayList;
import java.util.List;

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
//    @BindView(R.id.native_ad_media)
//    MediaView nativeAdMedia;

    public FacebookNativeAdListViewItemHolder(View view, Context context) {
        super(view);
        ButterKnife.bind(this, view);

        this.view = view;
        this.context = context;

        nativeAd = new NativeAd(context, Konstanten.FACEBOOK_AUDIENCE_NETWORK_NATIVE_AD_IN_RECYCLER_VIEW);

        nativeAd.setAdListener(new NativeAdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                Log.d(TAG, "YYYYY onError(): " + adError.getErrorMessage());
                Log.d(TAG, "YYYYY onError(): " + ad.getPlacementId());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.d(TAG, "YYYYY FacebookNativeAdListViewItemHolder onAdLoaded()");

                // Render the Native Ad Template
//                View adView = NativeAdView.render(context, nativeAd);
                // Add the Native Ad View to your ad container.
                // The recommended dimensions for the ad container are:
                // Width: 280dp - 500dp
                // Height: 250dp - 500dp
                // The template, however, will adapt to the supplied dimensions.
//                nativeAdContainer.addView(adView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800));

                nativeAd.downloadMedia();
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.d(TAG, "YYYYY FacebookNativeAdListViewItemHolder onAdClicked()");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.d(TAG, "YYYYY FacebookNativeAdListViewItemHolder onLoggingImpression()");
            }

            @Override
            public void onMediaDownloaded(Ad ad) {
                Log.d(TAG, "YYYYY FacebookNativeAdListViewItemHolder onMediaDownloaded()");

                inflateAd(nativeAd);
            }
        });

        // Initiate a request to load an ad.
//        nativeAd.loadAd();

        // Request an ad without auto cache
        nativeAd.loadAd(NativeAdBase.MediaCacheFlag.NONE);
    }


    private void inflateAd(NativeAd nativeAd) {
        Log.d(TAG, "YYYYY inflateAd()");

        nativeAd.unregisterView();

        // Add the Ad view into the ad container.
        LayoutInflater inflater = LayoutInflater.from(context);
        // Inflate the Ad view.  The layout referenced should be the one you created in the last step.
        LinearLayout adView = (LinearLayout) inflater.inflate(R.layout.native_ad_layout_1, nativeAdContainer, false);
        nativeAdContainer.addView(adView);

        // Add the AdOptionsView
        LinearLayout adChoicesContainer = adView.findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(context, nativeAd, nativeAdContainer);
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);

        // Create native UI using the ad metadata.
        AdIconView nativeAdIcon = adView.findViewById(R.id.native_ad_icon);
        TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
        MediaView nativeAdMedia = adView.findViewById(R.id.native_ad_media);
        TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
        TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
        TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
        Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);

        // Set the Text.
        nativeAdTitle.setText(nativeAd.getAdvertiserName());
        nativeAdBody.setText(nativeAd.getAdBodyText());
        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

        // Create a list of clickable views
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);

        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(
                adView,
                nativeAdMedia,
                nativeAdIcon,
                clickableViews);
    }

}
