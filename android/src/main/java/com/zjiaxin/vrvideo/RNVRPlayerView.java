package com.zjiaxin.vrvideo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;
import android.widget.FrameLayout;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360DirectorFactory;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.BarrelDistortionConfig;
import com.asha.vrlib.model.MDPinchConfig;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import tv.danmaku.ijk.media.player.IMediaPlayer;

@SuppressLint("ViewConstructor")
class RNVRPlayerView extends FrameLayout implements
        IMediaPlayer.OnPreparedListener,
        IMediaPlayer.OnErrorListener,
        IMediaPlayer.OnBufferingUpdateListener,
        IMediaPlayer.OnSeekCompleteListener,
        IMediaPlayer.OnCompletionListener,
        IMediaPlayer.OnInfoListener,
        IMediaPlayer.OnVideoSizeChangedListener,
        LifecycleEventListener, VRModeListener {

    public enum Events {
        EVENT_LOAD_START("onVideoLoadStart"),//刚准备好
        EVENT_LOAD("onVideoLoad"),//已经播放
        EVENT_ERROR("onVideoError"),//播放失败
        EVENT_END("onVideoEnd"),
        EVENT_SEEK("onVideoSeek"),
        EVENT_STALLED("onPlaybackStalled"),
        EVENT_ROTATION("onPlaybackRotation"),
        EVENT_RESUME("onPlaybackResume"),
        EVENT_READY_FOR_DISPLAY("onReadyForDisplay"),
        EVENT_PROGRESS("onVideoProgress");//播放进度

        private final String mName;

        Events(final String name) {
            mName = name;
        }

        @Override
        public String toString() {
            return mName;
        }
    }

    /**
     * event start
     */
    public static final String EVENT_PROP_FAST_FORWARD = "canPlayFastForward";
    public static final String EVENT_PROP_SLOW_FORWARD = "canPlaySlowForward";
    public static final String EVENT_PROP_SLOW_REVERSE = "canPlaySlowReverse";
    public static final String EVENT_PROP_REVERSE = "canPlayReverse";
    public static final String EVENT_PROP_STEP_FORWARD = "canStepForward";
    public static final String EVENT_PROP_STEP_BACKWARD = "canStepBackward";

    public static final String EVENT_PROP_DURATION = "duration";
    public static final String EVENT_PROP_PLAYABLE_DURATION = "playableDuration";
    public static final String EVENT_PROP_SEEKABLE_DURATION = "seekableDuration";
    public static final String EVENT_PROP_CURRENT_TIME = "currentTime";
    public static final String EVENT_PROP_SEEK_TIME = "seekTime";
    public static final String EVENT_PROP_NATURALSIZE = "naturalSize";
    public static final String EVENT_PROP_WIDTH = "width";
    public static final String EVENT_PROP_HEIGHT = "height";
    public static final String EVENT_PROP_ORIENTATION = "orientation";
    public static final String EVENT_PROP_METADATA = "metadata";
    public static final String EVENT_PROP_TARGET = "target";
    public static final String EVENT_PROP_METADATA_IDENTIFIER = "identifier";
    public static final String EVENT_PROP_METADATA_VALUE = "value";

    public static final String EVENT_PROP_ERROR = "error";
    public static final String EVENT_PROP_WHAT = "what";
    public static final String EVENT_PROP_EXTRA = "extra";
    /**
     * event end
     */


    private static final String TAG = "ReactVRPlayerView";
    private boolean mPaused = false;
    private boolean mMediaPlayerValid = false; // True if mMediaPlayer is in prepared, started, paused or completed state.
    private long mVideoDuration = 0;
    private long mVideoBufferedDuration = 0;
    private boolean isCompleted = false;
    private boolean mBackgroundPaused = false;
    private long mSeekTime = 0;

    private final Context mContext;
    private RCTEventEmitter mEventEmitter;

    private Handler mProgressUpdateHandler = new Handler();

    private CustomGlSurfaceView mGLSurfaceView;

//    private GLSurfaceView mGLSurfaceView;//openGL
    private Runnable mProgressUpdateRunnable = null;

    private MDVRLibrary mVRLibrary;
    private IjkMediaPlayerWrapper mPlayerWrapper;

    private FrameLayout flContainer;

    private float mProgressUpdateInterval = 250.0f;

    BroadcastReceiver receiver;

    public RNVRPlayerView(@NonNull ThemedReactContext context) {
        super(context);
        mContext = context;
        mEventEmitter = context.getJSModule(RCTEventEmitter.class);
        context.addLifecycleEventListener(this);
        init();

        mProgressUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (mMediaPlayerValid && !isCompleted && !mPaused && !mBackgroundPaused) {
                    WritableMap event = Arguments.createMap();
                    event.putDouble(EVENT_PROP_CURRENT_TIME, mPlayerWrapper.getPlayer().getCurrentPosition() / 1000.0);
                    event.putDouble(EVENT_PROP_PLAYABLE_DURATION, mVideoBufferedDuration / 1000.0); //TODO:mBufferUpdateRunnable
                    event.putDouble(EVENT_PROP_SEEKABLE_DURATION, mVideoDuration / 1000.0);
                    mEventEmitter.receiveEvent(getId(), Events.EVENT_PROGRESS.toString(), event);
                    // Check for update after an interval
                    mProgressUpdateHandler.postDelayed(mProgressUpdateRunnable, Math.round(mProgressUpdateInterval));
                }
            }
        };
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Configuration newConfig = intent.getParcelableExtra("newConfig");
                Log.d(TAG, "onReceive: " + newConfig);
            }
        };

    }

    private void init() {
        if (mPlayerWrapper == null) {
            mMediaPlayerValid = false;
            mPlayerWrapper = new IjkMediaPlayerWrapper();
            flContainer = new FrameLayout(mContext);
//            flContainer = (FrameLayout) LayoutInflater.from(mContext)
//                    .inflate(R.layout.player_view, null);
//            mGLSurfaceView = flContainer.findViewById(R.id.glSurfaceView);
            mGLSurfaceView = new CustomGlSurfaceView(mContext);
            flContainer.addView(mGLSurfaceView);

            addView(flContainer);
            initVRLibrary();
        }
    }

    private void initVRLibrary() {

        mPlayerWrapper.init();

        mVRLibrary = MDVRLibrary.with(mContext)
                .displayMode(MDVRLibrary.DISPLAY_MODE_NORMAL)
                .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_TOUCH)
                .projectionMode(MDVRLibrary.PROJECTION_MODE_SPHERE)
                .asVideo(new MDVRLibrary.IOnSurfaceReadyCallback() {
                    @Override
                    public void onSurfaceReady(Surface surface) {
                        mPlayerWrapper.setSurface(surface);
                    }
                })
                .ifNotSupport(new MDVRLibrary.INotSupportCallback() {
                    @Override
                    public void onNotSupport(int mode) {
                        Log.d(TAG, "onNotSupport: " + mode);
                    }
                })
                .pinchConfig(new MDPinchConfig().setMin(1.0f).setMax(8.0f).setDefaultValue(0.7f))
                .pinchEnabled(true)
                .directorFactory(new MD360DirectorFactory() {
                    @Override
                    public MD360Director createDirector(int i) {
                        return MD360Director.builder().setPitch(90).build();
                    }
                })
                .projectionFactory(new CustomProjectionFactory())
                .barrelDistortionConfig(new BarrelDistortionConfig().setDefaultEnabled(false).setScale(0.95f))
                .build(mGLSurfaceView);
        mVRLibrary.setAntiDistortionEnabled(false);

        mPlayerWrapper.setPreparedListener(this);
        mPlayerWrapper.getPlayer().setScreenOnWhilePlaying(true);
        mPlayerWrapper.getPlayer().setOnVideoSizeChangedListener(this);
        mPlayerWrapper.getPlayer().setOnErrorListener(this);
        mPlayerWrapper.getPlayer().setOnBufferingUpdateListener(this);
        mPlayerWrapper.getPlayer().setOnSeekCompleteListener(this);
        mPlayerWrapper.getPlayer().setOnCompletionListener(this);
        mPlayerWrapper.getPlayer().setOnInfoListener(this);
        mPlayerWrapper.getPlayer().setOnVideoSizeChangedListener(this);

    }


    public void applyModifiers() {
//        setResizeModeModifier(mResizeMode);
//        setRepeatModifier(mRepeat);
        setPausedModifier(mPaused);
//        setMutedModifier(mMuted);
        setProgressUpdateInterval(mProgressUpdateInterval);
//        setRateModifier(mRate);
    }


    public void setProgressUpdateInterval(final float progressUpdateInterval) {
        mProgressUpdateInterval = progressUpdateInterval;
    }

    public void setSrc(String url) {
        Log.d("video_url", url);
        mMediaPlayerValid = false;
        mVideoDuration = 0;
        mVideoBufferedDuration = 0;
        mPlayerWrapper.openFromUrl(url);
        mPlayerWrapper.prepare();
        isCompleted = false;
    }

    public void setPausedModifier(boolean pause) {
        Log.d(TAG, "pause: " + pause);
        mPaused = pause;
        if (!mMediaPlayerValid) {
            return;
        }
        if (mPaused) {
            if (mPlayerWrapper.getPlayer().isPlaying())
                mPlayerWrapper.getPlayer().pause();
        } else {
            if (!mPlayerWrapper.getPlayer().isPlaying()) {
                mPlayerWrapper.getPlayer().start();
                mProgressUpdateHandler.post(mProgressUpdateRunnable);
            }
        }
        setKeepScreenOn(!mPaused);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mVRLibrary.onOrientationChanged(mContext);
    }

    @Override
    public void onHostResume() {
        mBackgroundPaused = false;
        mVRLibrary.onResume(mContext);
        if (mMediaPlayerValid && !mPaused) {
            mPlayerWrapper.resume();
        }
    }

    @Override
    public void onHostPause() {
        mVRLibrary.onPause(mContext);
        if (mMediaPlayerValid && !mPaused) {
            mBackgroundPaused = true;
            mPlayerWrapper.pause();
        }
    }

    @Override
    public void onHostDestroy() {
        mVRLibrary.onDestroy();
    }

    public void destroy() {
        mVRLibrary.onDestroy();
        mPlayerWrapper.destroy();
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        Log.d(TAG, "onPrepared: ");
        if (mVRLibrary != null) {
            mVRLibrary.notifyPlayerChanged();
            mMediaPlayerValid = true;
            mVideoDuration = iMediaPlayer.getDuration();

            WritableMap naturalSize = Arguments.createMap();
            naturalSize.putInt(EVENT_PROP_WIDTH, iMediaPlayer.getVideoWidth());
            naturalSize.putInt(EVENT_PROP_HEIGHT, iMediaPlayer.getVideoHeight());
            if (iMediaPlayer.getVideoWidth() > iMediaPlayer.getVideoHeight())
                naturalSize.putString(EVENT_PROP_ORIENTATION, "landscape");
            else
                naturalSize.putString(EVENT_PROP_ORIENTATION, "portrait");


            WritableMap event = Arguments.createMap();
            event.putDouble(EVENT_PROP_DURATION, mVideoDuration / 1000.0);
            event.putDouble(EVENT_PROP_CURRENT_TIME, iMediaPlayer.getCurrentPosition() / 1000.0);
            event.putMap(EVENT_PROP_NATURALSIZE, naturalSize);
            // TODO: Actually check if you can.
            event.putBoolean(EVENT_PROP_FAST_FORWARD, true);
            event.putBoolean(EVENT_PROP_SLOW_FORWARD, true);
            event.putBoolean(EVENT_PROP_SLOW_REVERSE, true);
            event.putBoolean(EVENT_PROP_REVERSE, true);
            event.putBoolean(EVENT_PROP_FAST_FORWARD, true);
            event.putBoolean(EVENT_PROP_STEP_BACKWARD, true);
            event.putBoolean(EVENT_PROP_STEP_FORWARD, true);
            Log.d(TAG, "onPrepared: " + event);
            mEventEmitter.receiveEvent(getId(), Events.EVENT_LOAD.toString(), event);

            applyModifiers();


        }
    }


    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int percent) {
        mVideoBufferedDuration = (int) Math.round((double) (mVideoDuration * percent) / 100.0);
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int what, int extra) {
        WritableMap error = Arguments.createMap();
        error.putInt(EVENT_PROP_WHAT, what);
        error.putInt(EVENT_PROP_EXTRA, extra);
        WritableMap event = Arguments.createMap();
        event.putMap(EVENT_PROP_ERROR, error);
        mEventEmitter.receiveEvent(getId(), Events.EVENT_ERROR.toString(), event);
        return true;
    }

    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {
        WritableMap event = Arguments.createMap();
        event.putDouble(EVENT_PROP_CURRENT_TIME, iMediaPlayer.getCurrentPosition() / 1000.0);
        event.putDouble(EVENT_PROP_SEEK_TIME, mSeekTime / 1000.0);
        mEventEmitter.receiveEvent(getId(), Events.EVENT_SEEK.toString(), event);
        mSeekTime = 0;
    }


    public void seekTo(int seek) {
        if (mMediaPlayerValid) {
            mSeekTime = seek;
            mPlayerWrapper.getPlayer().seekTo(seek);
            if (isCompleted && mVideoDuration != 0 && seek < mVideoDuration) {
                isCompleted = false;
            }
        }
    }


    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        isCompleted = true;
        mEventEmitter.receiveEvent(getId(), Events.EVENT_END.toString(), null);
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {
        if (mVRLibrary != null) {
            mVRLibrary.onTextureResize(i, i1);
        }
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
        switch (what) {
            case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                if (mGLSurfaceView != null) {
                    mGLSurfaceView.setRotation(extra);
                }
                mEventEmitter.receiveEvent(getId(), Events.EVENT_ROTATION.toString(), Arguments.createMap());
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                mEventEmitter.receiveEvent(getId(), Events.EVENT_STALLED.toString(), Arguments.createMap());
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                mEventEmitter.receiveEvent(getId(), Events.EVENT_RESUME.toString(), Arguments.createMap());
                break;
            case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                mEventEmitter.receiveEvent(getId(), Events.EVENT_READY_FOR_DISPLAY.toString(), Arguments.createMap());
                break;

            default:
        }
        return false;
    }


    //vr mode switch
    @Override
    public void switchInteractiveMode(int mode) {
        if (mVRLibrary != null) mVRLibrary.switchInteractiveMode(mContext, mode);
    }

    @Override
    public void switchDisplayMode(int mode) {
        if (mVRLibrary != null) mVRLibrary.switchDisplayMode(mContext, mode);
    }

    @Override
    public void switchProjectionMode(int mode) {
        if (mVRLibrary != null) mVRLibrary.switchProjectionMode(mContext, mode);
    }

    @Override
    public void setAntiDistortionEnabled(boolean enabled) {
        if (mVRLibrary != null) mVRLibrary.setAntiDistortionEnabled(enabled);
    }
}

