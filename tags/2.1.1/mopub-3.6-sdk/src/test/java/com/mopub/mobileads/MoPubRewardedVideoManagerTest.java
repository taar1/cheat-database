package com.mopub.mobileads;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mopub.common.AdType;
import com.mopub.common.LifecycleListener;
import com.mopub.common.test.support.SdkTestRunner;
import com.mopub.network.AdRequest;
import com.mopub.network.AdResponse;
import com.mopub.network.MoPubRequestQueue;
import com.mopub.network.Networking;
import com.mopub.volley.RequestQueue;
import com.mopub.volley.VolleyError;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;

import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(SdkTestRunner.class)
public class MoPubRewardedVideoManagerTest {

    @Mock
    MoPubRequestQueue mockRequestQueue;
    @Mock
    MoPubRewardedVideoListener mockVideoListener;

    AdRequest.Listener requestListener;
    private AdRequest request;
    private boolean mLoaded;

    @Before
    public void setup() {
        MoPubRewardedVideoManager.init(Robolectric.buildActivity(Activity.class).create().get());
        MoPubRewardedVideoManager.setVideoListener(mockVideoListener);

        when(mockRequestQueue.add(any(AdRequest.class))).then(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
                request = ((AdRequest) invocationOnMock.getArguments()[0]);
                requestListener = request.getListener();
                return null;
            }
        });

        Networking.setRequestQueueForTesting(mockRequestQueue);
    }

    @Test
    public void onAdSuccess_noCEFound_shouldCallFailCallback() throws Exception {
        AdResponse testResponse = new AdResponse.Builder()
                .setAdType(AdType.CUSTOM)
                .setCustomEventClassName("doesn't_Exist")
                .build();

        MoPubRewardedVideoManager.loadVideo("testAdUnit");

        requestListener.onSuccess(testResponse);

        verify(mockVideoListener).onRewardedVideoLoadFailure(eq("testAdUnit"), eq(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR));
        verifyNoMoreInteractions(mockVideoListener);
    }

    @Test
    public void onAdSuccess_noCEFound_shouldLoadFailUrl() {
        AdResponse testResponse = new AdResponse.Builder()
                .setAdType(AdType.CUSTOM)
                .setCustomEventClassName("doesn't_Exist")
                .setFailoverUrl("fail.url")
                .build();



        MoPubRewardedVideoManager.loadVideo("testAdUnit");

        assertThat(request.getUrl()).contains("testAdUnit");
        requestListener.onSuccess(testResponse);
        assertThat(request.getUrl()).isEqualTo("fail.url");
        // Clear up the static state :(
        requestListener.onErrorResponse(new VolleyError("reset"));
    }

    @Test
    public void onAdSuccess_shouldInstantiateCustomEvent_andLoad() {
        AdResponse testResponse = new AdResponse.Builder()
                .setCustomEventClassName("com.mopub.mobileads.MoPubRewardedVideoManagerTest$TestCustomEvent")
                .setAdType(AdType.CUSTOM)
                .build();

        MoPubRewardedVideoManager.loadVideo("testAdUnit");

        requestListener.onSuccess(testResponse);
        assertThat(MoPubRewardedVideoManager.hasVideo("testAdUnit")).isTrue();
    }

    @Test
    public void playVideo_shouldSetHasVideoFalse() {
        AdResponse testResponse = new AdResponse.Builder()
                .setCustomEventClassName("com.mopub.mobileads.MoPubRewardedVideoManagerTest$TestCustomEvent")
                .setAdType(AdType.CUSTOM)
                .build();

        MoPubRewardedVideoManager.loadVideo("testAdUnit");

        requestListener.onSuccess(testResponse);
        assertThat(MoPubRewardedVideoManager.hasVideo("testAdUnit")).isTrue();
        MoPubRewardedVideoManager.showVideo("testAdUnit");
        assertThat(MoPubRewardedVideoManager.hasVideo("testAdUnit")).isFalse();
    }
    
    @Test
    public void playVideo_whenNotHasVideo_shouldFail() {
        AdResponse testResponse = new AdResponse.Builder()
                .setCustomEventClassName("com.mopub.mobileads.MoPubRewardedVideoManagerTest$NoVideoCustomEvent")
                .setAdType(AdType.CUSTOM)
                .build();

        MoPubRewardedVideoManager.loadVideo("testAdUnit");

        requestListener.onSuccess(testResponse);
        assertThat(MoPubRewardedVideoManager.hasVideo("testAdUnit")).isFalse();
        MoPubRewardedVideoManager.showVideo("testAdUnit");
        verify(mockVideoListener).onRewardedVideoLoadFailure(eq("testAdUnit"), eq(MoPubErrorCode.VIDEO_NOT_AVAILABLE));
    }

    @Test
    public void onAdFailure_shouldCallFailCallback() {
        VolleyError e = new VolleyError("testError!");

        MoPubRewardedVideoManager.loadVideo("testAdUnit");

        assertThat(request.getUrl()).contains("testAdUnit");
        requestListener.onErrorResponse(e);
        verify(mockVideoListener).onRewardedVideoLoadFailure(anyString(), any(MoPubErrorCode.class));
        verifyNoMoreInteractions(mockVideoListener);
    }

    public static class TestCustomEvent extends CustomEventRewardedVideo {
        protected boolean mPlayable = false;

        @Nullable
        @Override
        protected CustomEventRewardedVideoListener getVideoListenerForSdk() {
            return null;
        }

        @Nullable
        @Override
        protected LifecycleListener getLifecycleListener() {
            return null;
        }

        @NonNull
        @Override
        protected String getAdNetworkId() {
            return "id!";
        }

        @Override
        protected void onInvalidate() {
            mPlayable = false;
        }

        @Override
        protected boolean checkAndInitializeSdk(@NonNull final Activity launcherActivity,
                @NonNull final Map<String, Object> localExtras,
                @NonNull final Map<String, String> serverExtras) throws Exception {
            return false;
        }

        @Override
        protected void loadWithSdkInitialized(@NonNull final Activity activity,
                @NonNull final Map<String, Object> localExtras,
                @NonNull final Map<String, String> serverExtras) throws Exception {
            // Do nothing because robolectric handlers execute immediately.
            mPlayable = true;
        }

        @Override
        protected boolean hasVideoAvailable() {
            return mPlayable;
        }

        @Override
        protected void showVideo() {
            MoPubRewardedVideoManager.onRewardedVideoStarted(TestCustomEvent.class, "id!");
        }
    }

    public static class NoVideoCustomEvent extends TestCustomEvent {
        @Override
        protected void loadWithSdkInitialized(@NonNull final Activity activity,
                @NonNull final Map<String, Object> localExtras,
                @NonNull final Map<String, String> serverExtras) throws Exception {
            mPlayable = false;
        }
    }
}
