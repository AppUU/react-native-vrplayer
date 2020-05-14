package com.zjiaxin.vrvideo;

import android.support.annotation.Nullable;

import com.asha.vrlib.MDVRLibrary;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

import javax.annotation.Nonnull;

@ReactModule(name = RNVRPlayerViewManager.REACT_CLASS)
public class RNVRPlayerViewManager extends ViewGroupManager<RNVRPlayerView> {
    protected static final String REACT_CLASS = "RNVRVideoPlayer";

    private static final String VR_URL = "url";
    private static final String VR_PAUSED = "paused";
    private static final String VR_PROGRESS_UPDATE_INTERVAL = "progressUpdateInterval";
    private static final String VR_SEEK = "seek";
    private static final String VR_INTERACTIVE_MODE = "switchInteractiveMode";
    private static final String VR_DISPLAY_MODE = "switchDisplayMode";
    private static final String VR_PROJECTION_MODE = "switchProjectionMode";
    private static final String VR_ANT_DISTORTION_ENABLED = "setAntiDistortionEnabled";

    @Nonnull
    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Nonnull
    @Override
    protected RNVRPlayerView createViewInstance(@Nonnull ThemedReactContext reactContext) {
        return new RNVRPlayerView(reactContext);
    }


    @Override
    @Nullable
    public Map getExportedCustomDirectEventTypeConstants() {
        MapBuilder.Builder builder = MapBuilder.builder();
        for (RNVRPlayerView.Events event : RNVRPlayerView.Events.values()) {
            builder.put(event.toString(), MapBuilder.of("registrationName", event.toString()));
        }
        return builder.build();
    }


    @ReactProp(name = VR_URL)
    public void setSrc(final RNVRPlayerView view, final String url) {
        view.setSrc(url);
    }

    @ReactProp(name = VR_PAUSED, defaultBoolean = false)
    public void paused(final RNVRPlayerView view, final boolean paused) {
        view.setPausedModifier(paused);
    }

    @ReactProp(name = VR_PROGRESS_UPDATE_INTERVAL, defaultFloat = 250.0f)
    public void setProgressUpdateInterval(final RNVRPlayerView videoView, final float progressUpdateInterval) {
        videoView.setProgressUpdateInterval(progressUpdateInterval);
    }


    @ReactProp(name = VR_SEEK)
    public void setSeek(final RNVRPlayerView videoView, final float seek) {
        videoView.seekTo(Math.round(seek * 1000.0f));
    }


    @ReactProp(name = VR_INTERACTIVE_MODE)
    public void switchInteractiveMode(final RNVRPlayerView view, final String mode) {
        if ("CARDBOARD_MOTION".equals(mode)) {
            view.switchInteractiveMode(MDVRLibrary.INTERACTIVE_MODE_CARDBORAD_MOTION);
        } else if ("CARDBOARD_MOTION_WITH_TOUCH".equals(mode)) {
            view.switchInteractiveMode(MDVRLibrary.INTERACTIVE_MODE_CARDBORAD_MOTION_WITH_TOUCH);
        } else if ("TOUCH".equals(mode)) {
            view.switchInteractiveMode(MDVRLibrary.INTERACTIVE_MODE_TOUCH);
        } else if ("MOTION".equals(mode)) {
            view.switchInteractiveMode(MDVRLibrary.INTERACTIVE_MODE_MOTION);
        } else if ("MOTION_WITH_TOUCH".equals(mode)) {
            view.switchInteractiveMode(MDVRLibrary.INTERACTIVE_MODE_MOTION_WITH_TOUCH);
        }
    }

    @ReactProp(name = VR_DISPLAY_MODE)
    public void switchDisplayMode(final RNVRPlayerView view, final String mode) {
        if ("GLASS".equals(mode)) {
            view.switchDisplayMode(MDVRLibrary.DISPLAY_MODE_GLASS);
        } else if ("NORMAL".equals(mode)) {
            view.switchDisplayMode(MDVRLibrary.DISPLAY_MODE_NORMAL);
        }
    }

    @ReactProp(name = VR_PROJECTION_MODE)
    public void switchProjectionMode(final RNVRPlayerView view, final String mode) {
        if ("SPHERE".equals(mode)) {
            view.switchProjectionMode(MDVRLibrary.PROJECTION_MODE_SPHERE);
        } else if ("CUBE".equals(mode)) {
            view.switchProjectionMode(MDVRLibrary.PROJECTION_MODE_CUBE);
        }
    }

    @ReactProp(name = VR_ANT_DISTORTION_ENABLED, defaultBoolean = false)
    public void setAntiDistortionEnabled(final RNVRPlayerView view, final boolean enable) {
        view.setAntiDistortionEnabled(enable);
    }
}
