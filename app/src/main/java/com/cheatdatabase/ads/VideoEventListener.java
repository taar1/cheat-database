package com.cheatdatabase.ads;

import com.inmobi.ads.InMobiNative;

public abstract class VideoEventListener {
    /**
     * Called to notify that the video has finished playing.
     *
     * @param ad Represents the {@link InMobiNative} ad
     */
    public void onVideoCompleted(InMobiNative ad) {
    }

    /**
     * Called to notify that the user has skipped video play.
     *
     * @param ad Represents the {@link InMobiNative} ad
     */
    public void onVideoSkipped(InMobiNative ad) {
    }

    /**
     * Called to notify when media audio state changes.
     *
     * @param isMuted Represents whether media is muted or not.
     */
    public void onAudioStateChanged(InMobiNative inMobiNative, boolean isMuted) {
    }
}