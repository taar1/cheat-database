package com.cheatdatabase.ads;

import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiNative;

/**
 * A listener for receiving notifications during the lifecycle of a Native ad.
 */
public abstract class NativeAdEventListener {
    /**
     * Called to indicate that an ad is available in response to a request for an ad (by calling
     * {@link InMobiNative#load()}. <p class="note"><strong>Note</strong> This does not
     * indicate that the ad can be shown yet. Your code should show an ad <strong>after</strong> the
     * {@link #onAdLoadSucceeded(InMobiNative)} method is called. Alternately, if you do not
     * want to handle this event, you must test if the ad is ready to be shown by checking the
     * result of calling the {@link InMobiNative#isReady()} method.</p>
     *
     * @param ad Represents the {@link InMobiNative} ad for which ad content was received
     */
    public void onAdReceived(InMobiNative ad) {
    }

    /**
     * Called to indicate that an ad was loaded and it can now be shown. This will always be called
     * <strong>after</strong> the {@link #onAdReceived(InMobiNative)} callback.
     *
     * @param ad Represents the {@link InMobiNative} ad which was loaded
     */
    public void onAdLoadSucceeded(InMobiNative ad) {
    }

    /**
     * Called to notify that a native ad failed to load.
     *
     * @param ad            Represents the {@link InMobiNative} ad which failed to load
     * @param requestStatus Represents the {@link InMobiAdRequestStatus} status containing error reason
     */
    public void onAdLoadFailed(InMobiNative ad, InMobiAdRequestStatus requestStatus) {
    }

    /**
     * @param ad Represents the {@link InMobiNative} ad whose fullscreen was dismissed
     */
    public void onAdFullScreenDismissed(InMobiNative ad) {
    }

    /**
     * Called to notify that the ad will open an overlay that covers the screen.
     *
     * @param ad Represents the {@link InMobiNative} ad which will go fullscreen
     */
    public void onAdFullScreenWillDisplay(InMobiNative ad) {
    }

    /**
     * Called to notify that the ad opened an overlay that covers the screen.
     *
     * @param ad Represents the {@link InMobiNative} ad whose fullscreen will be displayed
     */
    public void onAdFullScreenDisplayed(InMobiNative ad) {
    }

    /**
     * Called to notify that the user is about to leave the application as a result of interacting with it.
     *
     * @param ad Represents the {@link InMobiNative} ad
     */
    public void onUserWillLeaveApplication(InMobiNative ad) {
    }

    /**
     * Called to notify impression has been recorded for this ad. <b>Note:</b>Override this method to notify
     * viewable impression to the Mediation Adapter.
     *
     * @param ad Represents the {@link InMobiNative} ad for which impression is recorded.
     */
    public void onAdImpressed(InMobiNative ad) {
    }

    /**
     * Called to notify ad was clicked. <b>Note:</b>Override this method to notify click to the Mediation Adapter.
     *
     * @param ad Represents the {@link InMobiNative} ad which was clicked
     */
    public void onAdClicked(InMobiNative ad) {
    }

    /**
     * Called to notify that the ad status has changed.
     *
     * @param nativeAd Represents the {@link InMobiNative} ad
     */
    public void onAdStatusChanged(InMobiNative nativeAd) {
    }
}