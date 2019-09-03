package com.cheatdatabase.holders;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cheatdatabase.R;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAdLayout;

public class FacebookNativeAdHolder extends RecyclerView.ViewHolder {

    public NativeAdLayout nativeAdLayout;
    public MediaView mvAdMedia;
    public MediaView ivAdIcon;
    public TextView tvAdTitle;
    public TextView tvAdBody;
    public TextView tvAdSocialContext;
    public TextView tvAdSponsoredLabel;
    public Button btnAdCallToAction;
    public LinearLayout adChoicesContainer;

    public FacebookNativeAdHolder(NativeAdLayout adLayout) {
        super(adLayout);

        nativeAdLayout = adLayout;
        mvAdMedia = adLayout.findViewById(R.id.native_ad_media);
        tvAdTitle = adLayout.findViewById(R.id.native_ad_title);
        tvAdBody = adLayout.findViewById(R.id.native_ad_body);
        tvAdSocialContext = adLayout.findViewById(R.id.native_ad_social_context);
        tvAdSponsoredLabel = adLayout.findViewById(R.id.native_ad_sponsored_label);
        btnAdCallToAction = adLayout.findViewById(R.id.native_ad_call_to_action);
        ivAdIcon = adLayout.findViewById(R.id.native_ad_icon);
        adChoicesContainer = adLayout.findViewById(R.id.ad_choices_container);
    }

}