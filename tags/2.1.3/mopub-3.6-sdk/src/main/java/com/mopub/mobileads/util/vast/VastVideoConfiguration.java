package com.mopub.mobileads.util.vast;

import com.mopub.mobileads.VastAbsoluteProgressTracker;
import com.mopub.mobileads.VastFractionalProgressTracker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VastVideoConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ArrayList<String> mImpressionTrackers;
    private final ArrayList<VastFractionalProgressTracker> mFractionalTrackers;
    private final ArrayList<VastAbsoluteProgressTracker> mAbsoluteTrackers;
    private final ArrayList<String> mCompleteTrackers;
    private final ArrayList<String> mCloseTrackers;
    private final ArrayList<String> mClickTrackers;
    private String mClickThroughUrl;
    private String mNetworkMediaFileUrl;
    private String mDiskMediaFileUrl;
    private VastCompanionAd mVastCompanionAd;

    public VastVideoConfiguration() {
        mImpressionTrackers = new ArrayList<String>();
        mFractionalTrackers = new ArrayList<VastFractionalProgressTracker>();
        mAbsoluteTrackers = new ArrayList<VastAbsoluteProgressTracker>();
        mCompleteTrackers = new ArrayList<String>();
        mCloseTrackers = new ArrayList<String>();
        mClickTrackers = new ArrayList<String>();
    }

    /**
     * Setters
     */

    public void addImpressionTrackers(final List<String> impressionTrackers) {
        mImpressionTrackers.addAll(impressionTrackers);
    }

    /**
     * Add trackers for percentage-based tracking. This includes all quartile trackers and any
     * "progress" events with other percentages.
     */
    public void addFractionalTrackers(final List<VastFractionalProgressTracker> fractionalTrackers) {
        mFractionalTrackers.addAll(fractionalTrackers);
    }

    /**
     * Add trackers for absolute tracking. This includes start trackers, which have an absolute threshold of 2 seconds.
     */
    public void addAbsoluteTrackers(final List<VastAbsoluteProgressTracker> absoluteTrackers) {
        mAbsoluteTrackers.addAll(absoluteTrackers);
    }

    public void addCompleteTrackers(final List<String> completeTrackers) {
        mCompleteTrackers.addAll(completeTrackers);
    }

    public void addCloseTrackers(final List<String> closeTrackers) {
        mCloseTrackers.addAll(closeTrackers);
    }

    public void addClickTrackers(final List<String> clickTrackers) {
        mClickTrackers.addAll(clickTrackers);
    }

    public void setClickThroughUrl(final String clickThroughUrl) {
        mClickThroughUrl = clickThroughUrl;
    }

    public void setNetworkMediaFileUrl(final String networkMediaFileUrl) {
        mNetworkMediaFileUrl = networkMediaFileUrl;
    }

    public void setDiskMediaFileUrl(final String diskMediaFileUrl) {
        mDiskMediaFileUrl = diskMediaFileUrl;
    }

    public void setVastCompanionAd(final VastCompanionAd vastCompanionAd) {
        mVastCompanionAd = vastCompanionAd;
    }

    /**
     * Getters
     */

    public List<String> getImpressionTrackers() {
        return mImpressionTrackers;
    }

    public ArrayList<VastAbsoluteProgressTracker> getAbsoluteTrackers() {
        return mAbsoluteTrackers;
    }

    public ArrayList<VastFractionalProgressTracker> getFractionalTrackers() {
        return mFractionalTrackers;
    }

    public List<String> getCompleteTrackers() {
        return mCompleteTrackers;
    }

    public List<String> getCloseTrackers() {
        return mCloseTrackers;
    }

    public List<String> getClickTrackers() {
        return mClickTrackers;
    }

    public String getClickThroughUrl() {
        return mClickThroughUrl;
    }

    public String getNetworkMediaFileUrl() {
        return mNetworkMediaFileUrl;
    }

    public String getDiskMediaFileUrl() {
        return mDiskMediaFileUrl;
    }

    public VastCompanionAd getVastCompanionAd() {
        return mVastCompanionAd;
    }
}
