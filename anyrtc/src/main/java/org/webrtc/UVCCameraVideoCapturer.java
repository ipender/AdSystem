package org.webrtc;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;

import org.webrtc.CameraEnumerationAndroid.CaptureFormat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by hadoop on 17-1-11.
 */
public class UVCCameraVideoCapturer implements VideoCapturer {

    private final static String TAG = "UVCCameraVideoCapturer";
    private static final int CAMERA_STOP_TIMEOUT_MS = 7000;


    /* |mCameraThreadHandler| must be synchronized on |mCameraThreadLock| when not on the camera thread,
     * or when modifying the reference. Use maybePostOnCameraThread() instead of posting directly to
     * the handler - this way all callbacks with a specifed token can be removed at once.*/
    private final Object mCameraThreadLock = new Object();
    private SurfaceTextureHelper mSurfaceHelper;
    private Handler mCameraThreadHandler;   // this is got from SurfaceTextureHelper
    private int openCameraAttempts;

    private final Set<byte[]> queuedBuffers = new HashSet<byte[]>();

    private int requestedWidth;
    private int requestedHeight;
    private int requestedFramerate;

    private CaptureFormat captureFormat;
    private CapturerObserver frameObserver = null;
    private Context applicationContext;

    private final boolean isCapturingToTexture;

    private int oriention = 0;

    private UVCCamera mUVCCamera;


    public UVCCameraVideoCapturer(UVCCamera uvcCamera, boolean isCapturingToTexture) {
        this.mUVCCamera = uvcCamera;
        this.isCapturingToTexture = isCapturingToTexture;
    }

    public void setUVCCameraProxy(UVCCamera proxy) {
        this.mUVCCamera = proxy;
    }

    // Returns true if this VideoCapturer is setup to capture video frames to a SurfaceTexture.
    public boolean isCapturingToTexture() {
        return isCapturingToTexture;
    }

    @Override
    public List<CameraEnumerationAndroid.CaptureFormat> getSupportedFormats() {
        return mUVCCamera.getSupportedFormats();
    }

    // Note that this actually opens the camera, and Camera callbacks run on the
    // thread that calls open(), so this is done on the CameraThread.
    @Override
    public void startCapture(final int width, final int height, final int framerate,
                             SurfaceTextureHelper surfaceTextureHelper, final Context applicationContext,
                             final CapturerObserver frameObserver) {

        if (surfaceTextureHelper == null) {
            frameObserver.onCapturerStarted(false);     // start capture failed
            return;
        }

        if (applicationContext == null) {
            throw new IllegalArgumentException("AnyRTC application context is not set!");
        }

        synchronized (mCameraThreadLock) {
            if (this.mCameraThreadHandler != null) {
                throw new RuntimeException("Camera has already been started!");
            }
            this.mCameraThreadHandler = surfaceTextureHelper.getHandler();
            this.mSurfaceHelper = surfaceTextureHelper;
            final boolean didPost = maybePostOnCameraThread(new Runnable() {
                @Override
                public void run() {
                    openCameraAttempts = 0;
                    startCaptureOnCameraThread(width, height, framerate, frameObserver,
                            applicationContext);
                }
            });

            if (!didPost) {
                frameObserver.onCapturerStarted(false);
            }
        }

    }

    @Override
    public void stopCapture() throws InterruptedException {

    }

    @Override
    public void onOutputFormatRequest(int width, int height, int framerate) {

    }

    @Override
    public void changeCaptureFormat(int width, int height, int framerate) {

    }

    @Override
    public void dispose() {

    }

    // (Re)start preview with the closest supported format to |width| x |height| @ |framerate|.
    private void startPreviewOnCameraThread(int width, int height, int framerate) {
        synchronized (mCameraThreadLock) {
            if (mCameraThreadHandler == null || mUVCCamera == null) {
                return;
            } else {
                checkIsOnCameraThread();
            }
        }

        requestedWidth = width;
        requestedHeight = height;
        requestedFramerate = framerate;

        // Find closest supported format for |width| x |height| @ |framerate|.
    }

    private void startCaptureOnCameraThread(final int width, final int height, final int framerate,
                                            final CapturerObserver frameObserver, final Context applicationContext) {
        synchronized (mCameraThreadLock) {
            if (mCameraThreadHandler == null) {
                return;
            } else {
                checkIsOnCameraThread();
            }
        }

        this.applicationContext = applicationContext;
        this.frameObserver = frameObserver;


    }

    private void onOutputFormatRequestOnCameraThread(int width, int height, int framerate) {
        synchronized (mCameraThreadLock) {
            if (mCameraThreadHandler == null) {
                return;
            } else {
                checkIsOnCameraThread();
            }
        }

        frameObserver.onOutputFormatRequest(width, height, framerate);
    }

    // this function should be called by hardware camera when a video frame is ready
    public void onFrame(byte[] frameData) {
        synchronized (mCameraThreadLock) {
            if (mCameraThreadHandler == null) {
                return;
            } else {
                checkIsOnCameraThread();
            }
        }

        final long captureTimeNs =
                TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());
        frameObserver.onByteBufferFrameCaptured(frameData, captureFormat.width, captureFormat.height,
                oriention, captureTimeNs);

    }

    private boolean maybePostOnCameraThread(Runnable runnable) {
        return maybePostDelayedOnCameraThread(0 /* delayMs */, runnable);
    }

    private boolean maybePostDelayedOnCameraThread(int delayMs, Runnable runnable) {
        synchronized (mCameraThreadLock) {
            return mCameraThreadHandler != null
                    && mCameraThreadHandler.postAtTime (
                    runnable, this /* token */, SystemClock.uptimeMillis() + delayMs);
        }
    }

    private void checkIsOnCameraThread() {
        synchronized (mCameraThreadLock) {
            if (mCameraThreadHandler == null) {
                Logging.e(TAG, "Camera is stopped - can't check thread.");
            } else if (Thread.currentThread() != mCameraThreadHandler.getLooper().getThread()) {
                throw new IllegalStateException("Wrong thread");
            }
        }
    }
}
