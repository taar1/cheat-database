package com.mopub.mobileads.util.vast;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.VastAbsoluteProgressTracker;
import com.mopub.mobileads.VastFractionalProgressTracker;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

class VastXmlManager {
    private static final String ROOT_TAG = "MPMoVideoXMLDocRoot";
    private static final String ROOT_TAG_OPEN = "<" + ROOT_TAG + ">";
    private static final String ROOT_TAG_CLOSE = "</" + ROOT_TAG + ">";

    // Element names
    private static final String IMPRESSION_TRACKER = "Impression";
    private static final String VIDEO_TRACKER = "Tracking";
    private static final String CLICK_THROUGH = "ClickThrough";
    private static final String CLICK_TRACKER = "ClickTracking";
    private static final String MEDIA_FILE = "MediaFile";
    private static final String VAST_AD_TAG = "VASTAdTagURI";
    private static final String MP_IMPRESSION_TRACKER = "MP_TRACKING_URL";
    private static final String COMPANION = "Companion";

    // Attribute names
    private static final String EVENT = "event";
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String OFFSET = "offset";

    // Event Attibute values
    private static final String START = "start";
    private static final String FIRST_QUARTILE = "firstQuartile";
    private static final String MIDPOINT = "midpoint";
    private static final String THIRD_QUARTILE = "thirdQuartile";
    private static final String COMPLETE = "complete";
    private static final String CLOSE = "close";
    private static final String PROGRESS = "progress";

    private static final int START_TRACKER_THRESHOLD = 2000;
    private static final float FIRST_QUARTER_MARKER = 0.25f;
    private static final float MID_POINT_MARKER = 0.50f;
    private static final float THIRD_QUARTER_MARKER = 0.75f;

    private static Pattern percentagePattern = Pattern.compile("((\\d{1,2})|(100))%");
    private static Pattern absolutePattern = Pattern.compile("\\d{2}:\\d{2}:\\d{2}(.\\d{3})?");


    // This class currently assumes an image type companion ad since that is what we are supporting
    class ImageCompanionAdXmlManager {
        // Element name
        private static final String TRACKING_EVENTS = "TrackingEvents";
        private static final String COMPANION_STATIC_RESOURCE = "StaticResource";
        private static final String COMPANION_CLICK_THROUGH = "CompanionClickThrough";
        // Attribute value
        private static final String CREATIVE_VIEW = "creativeView";
        // Attribute name
        private static final String CREATIVE_TYPE = "creativeType";
        private final Node mCompanionNode;

        ImageCompanionAdXmlManager(final Node companionNode) throws IllegalArgumentException {
            if (companionNode == null) {
                throw new IllegalArgumentException("Companion node cannot be null");
            }
            mCompanionNode = companionNode;
        }

        Integer getWidth() {
            return XmlUtils.getAttributeValueAsInt(mCompanionNode, WIDTH);
        }

        Integer getHeight() {
            return XmlUtils.getAttributeValueAsInt(mCompanionNode, HEIGHT);
        }

        String getType() {
            final Node node = XmlUtils.getFirstMatchingChildNode(
                    mCompanionNode,
                    COMPANION_STATIC_RESOURCE
            );
            return XmlUtils.getAttributeValue(node, CREATIVE_TYPE);
        }

        String getImageUrl() {
            final Node node = XmlUtils.getFirstMatchingChildNode(
                    mCompanionNode,
                    COMPANION_STATIC_RESOURCE
            );
            return XmlUtils.getNodeValue(node);
        }

        String getClickThroughUrl() {
            final Node node = XmlUtils.getFirstMatchingChildNode(
                    mCompanionNode,
                    COMPANION_CLICK_THROUGH
            );
            return XmlUtils.getNodeValue(node);
        }

        List<String> getClickTrackers() {
            final List<String> companionAdClickTrackers = new ArrayList<String>();
            final Node node = XmlUtils.getFirstMatchingChildNode(
                    mCompanionNode,
                    TRACKING_EVENTS
            );

            if (node == null) {
                return companionAdClickTrackers;
            }

            final List<Node> trackerNodes = XmlUtils.getMatchingChildNodes(
                    node,
                    VIDEO_TRACKER,
                    EVENT,
                    Arrays.asList(CREATIVE_VIEW)
            );

            for (final Node trackerNode : trackerNodes) {
                if (trackerNode.getFirstChild() != null) {
                    companionAdClickTrackers.add(trackerNode.getFirstChild().getNodeValue().trim());
                }
            }

            return companionAdClickTrackers;
        }
    }

    class MediaXmlManager {
        // Attribute names
        private static final String DELIVERY = "delivery";
        private static final String VIDEO_TYPE  = "type";
        private final Node mMediaNode;

        MediaXmlManager(final Node mediaNode) throws IllegalArgumentException {
            if (mediaNode == null) {
                throw new IllegalArgumentException("Media node cannot be null");
            }
            mMediaNode = mediaNode;
        }

        String getDelivery() {
            return XmlUtils.getAttributeValue(mMediaNode, DELIVERY);
        }

        Integer getWidth() {
            return XmlUtils.getAttributeValueAsInt(mMediaNode, WIDTH);
        }

        Integer getHeight() {
            return XmlUtils.getAttributeValueAsInt(mMediaNode, HEIGHT);
        }

        String getType() {
            return XmlUtils.getAttributeValue(mMediaNode, VIDEO_TYPE);
        }

        String getMediaUrl() {
            return XmlUtils.getNodeValue(mMediaNode);
        }
    }

    private Document mVastDoc;

    void parseVastXml(String xmlString) throws ParserConfigurationException, IOException, SAXException {
        // if the xml string starts with <?xml?>, this tag can break parsing if it isn't formatted exactly right
        // or if it's not the first line of the document...we're just going to strip it
        xmlString = xmlString.replaceFirst("<\\?.*\\?>", "");

        // adserver may embed additional impression trackers as a sibling node of <VAST>
        // wrap entire document in root node for this case.
        String documentString = ROOT_TAG_OPEN + xmlString + ROOT_TAG_CLOSE;

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setCoalescing(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        mVastDoc = documentBuilder.parse(new InputSource(new StringReader(documentString)));
    }

    String getVastAdTagURI() {
        List<String> uriWrapper = XmlUtils.getStringDataAsList(mVastDoc, VAST_AD_TAG);
        return (uriWrapper.size() > 0) ? uriWrapper.get(0) : null;
    }

    List<String> getImpressionTrackers() {
        List<String> impressionTrackers = XmlUtils.getStringDataAsList(mVastDoc, IMPRESSION_TRACKER);
        impressionTrackers.addAll(XmlUtils.getStringDataAsList(mVastDoc, MP_IMPRESSION_TRACKER));

        return impressionTrackers;
    }

    /**
     * Return a sorted list of the video's percent-based progress-trackers. These are the
     * quartile trackers and any "progress" nodes with percent-based offsets.
     *
     * Quartile trackers look like:
     * {@code
     * <Tracking event="firstQuartile">
     *     <![CDATA[trackingURL]]>
     * </Tracking>
     * }
     *
     * Percent-based progress trackers look like:
     * {@code
     * <Tracking event="progress" offset="11%">
     *     <![CDATA[trackingURL]]>
     * </Tracking>
     * }
     */
    @NonNull
    List<VastFractionalProgressTracker> getFractionalProgressTrackers() {
        // Add all the quartile trackers from VAST 2.0:
        List<VastFractionalProgressTracker> percentTrackers = new ArrayList<VastFractionalProgressTracker>();
        addQuartileTrackerWithFraction(percentTrackers, getVideoTrackerByAttribute(FIRST_QUARTILE), FIRST_QUARTER_MARKER);
        addQuartileTrackerWithFraction(percentTrackers, getVideoTrackerByAttribute(MIDPOINT), MID_POINT_MARKER);
        addQuartileTrackerWithFraction(percentTrackers, getVideoTrackerByAttribute(THIRD_QUARTILE), THIRD_QUARTER_MARKER);

        // Get any other trackers with event="progress" offset="n%"
        final List<Node> progressNodes = XmlUtils.getNodesWithElementAndAttribute(mVastDoc, VIDEO_TRACKER, EVENT, PROGRESS);
        for (Node progressNode : progressNodes) {
            final String offsetString = XmlUtils.getAttributeValue(progressNode, OFFSET).trim();
            if (isPercentageTracker(offsetString)) {
                String trackingUrl = XmlUtils.getNodeValue(progressNode).trim();
                try {
                    float trackingFraction = Float.parseFloat(offsetString.replace("%", "")) / 100f;
                    percentTrackers.add(new VastFractionalProgressTracker(trackingUrl, trackingFraction));
                } catch (NumberFormatException e) {
                    MoPubLog.d(String.format("Failed to parse VAST progress tracker %s", offsetString));
                }
            }
        }

        // Sort the list so we can quickly index it in the video progress runnable.
        Collections.sort(percentTrackers);
        return percentTrackers;
    }

    /**
     * Return a sorted list of the video's absolute progress trackers. This includes start trackers
     * and any "progress" nodes with absolute offsets.
     *
     * Start trackers live in nodes like:
     * {@code
     * <Tracking event="start">
     *     <![CDATA[trackingURL]]>
     * </Tracking>
     * }
     * Absolute progress trackers look like:
     * {@code
     * <Tracking event="progress" offset="00:00:10.000">
     *     <![CDATA[trackingURL]]>
     * </Tracking>
     * }
     */
    @NonNull
    List<VastAbsoluteProgressTracker> getAbsoluteProgressTrackers() {
        List<VastAbsoluteProgressTracker> trackers = new ArrayList<VastAbsoluteProgressTracker>();
        // Start trackers are treated as absolute trackers with a 2s offset.
        final List<String> startTrackers = getVideoTrackerByAttribute(START);
        for (String url : startTrackers) {
            trackers.add(new VastAbsoluteProgressTracker(url, START_TRACKER_THRESHOLD));
        }

        // Parse progress trackers and extract the absolute offsets of the form "HH:MM:SS[.mmm]"
        final List<Node> progressNodes = XmlUtils.getNodesWithElementAndAttribute(mVastDoc, VIDEO_TRACKER, EVENT, PROGRESS);
        for (Node progressNode : progressNodes) {
            final String offSetString = XmlUtils.getAttributeValue(progressNode, OFFSET).trim();
            if (isAbsoluteTracker(offSetString)) {
                String trackingUrl = XmlUtils.getNodeValue(progressNode).trim();
                try {
                    Integer trackingMilliseconds = parseAbsoluteOffset(offSetString);
                    if (trackingMilliseconds != null) {
                        trackers.add(new VastAbsoluteProgressTracker(trackingUrl, trackingMilliseconds));
                    }
                } catch (NumberFormatException e) {
                    MoPubLog.d(String.format("Failed to parse VAST progress tracker %s", offSetString));
                }
            }
        }

        // Sort the list so we can quickly index it in the video progress runnable.
        Collections.sort(trackers);
        return trackers;
    }


    List<String> getVideoCompleteTrackers() {
        return getVideoTrackerByAttribute(COMPLETE);
    }

    List<String> getVideoCloseTrackers() {
        return getVideoTrackerByAttribute(CLOSE);
    }

    String getClickThroughUrl() {
        List<String> clickUrlWrapper = XmlUtils.getStringDataAsList(mVastDoc, CLICK_THROUGH);
        return (clickUrlWrapper.size() > 0) ? clickUrlWrapper.get(0) : null;
    }

    List<String> getClickTrackers() {
        return XmlUtils.getStringDataAsList(mVastDoc, CLICK_TRACKER);
    }

    String getMediaFileUrl() {
        List<String> urlWrapper = XmlUtils.getStringDataAsList(mVastDoc, MEDIA_FILE);
        return (urlWrapper.size() > 0) ? urlWrapper.get(0) : null;
    }

    List<MediaXmlManager> getMediaXmlManagers() {
        final NodeList nodes = mVastDoc.getElementsByTagName(MEDIA_FILE);
        final List<MediaXmlManager> mediaXmlManagers =
                new ArrayList<MediaXmlManager>(nodes.getLength());
        for (int i = 0; i < nodes.getLength(); ++i) {
            mediaXmlManagers.add(new MediaXmlManager(nodes.item(i)));
        }
        return mediaXmlManagers;
    }

    List<ImageCompanionAdXmlManager> getCompanionAdXmlManagers() {
        final NodeList nodes = mVastDoc.getElementsByTagName(COMPANION);
        final List<ImageCompanionAdXmlManager> imageCompanionAdXmlManagers =
                new ArrayList<ImageCompanionAdXmlManager>(nodes.getLength());
        for (int i = 0; i < nodes.getLength(); ++i) {
            imageCompanionAdXmlManagers.add(new ImageCompanionAdXmlManager(nodes.item(i)));
        }
        return imageCompanionAdXmlManagers;
    }

    private List<String> getVideoTrackerByAttribute(final String attributeValue) {
        return XmlUtils.getStringDataAsList(mVastDoc, VIDEO_TRACKER, EVENT, attributeValue);
    }

    private boolean isPercentageTracker(String progressValue) {
        return !TextUtils.isEmpty(progressValue)
                && percentagePattern.matcher(progressValue).matches();
    }

    private boolean isAbsoluteTracker(String progressValue) {
        return !TextUtils.isEmpty(progressValue)
                && absolutePattern.matcher(progressValue).matches();
    }

    private Integer parseAbsoluteOffset(String progressValue) {
        final String[] split = progressValue.split(":");
        if (split.length != 3) {
            return null;
        }

        return Integer.parseInt(split[0]) * 60 * 60 * 1000 // Hours
                + Integer.parseInt(split[1]) * 60 * 1000 // Minutes
                + (int)(Float.parseFloat(split[2]) * 1000);
    }

    private void addQuartileTrackerWithFraction(List<VastFractionalProgressTracker> trackers, List<String> urls, float fraction) {
        for (String url : urls) {
            trackers.add(new VastFractionalProgressTracker(url, fraction));
        }
    }
}
