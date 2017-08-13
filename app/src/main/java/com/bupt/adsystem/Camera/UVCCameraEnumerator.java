package com.bupt.adsystem.Camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.Surface;

import com.bupt.adsystem.Utils.AdSystemConfig;
import com.bupt.adsystem.view.LifeCycle;
import com.bupt.adsystem.view.LifeCycleMgr;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.MediaCodecVideoDecoder;
import org.webrtc.MediaCodecVideoEncoder;
import org.webrtc.UVCCameraInterface;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by hadoop on 17-1-27.
 */
public class UVCCameraEnumerator implements UVCCameraInterface, LifeCycle {

    private static final String TAG = "UVCCameraEnumerator";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;

    private volatile static UVCCameraEnumerator sUVCCamera = null;
    private volatile static UVCVideoRecorder sVideoRecorder = null;

    private int videoWidth = 640;
    private int videoHeight = 480;
    private int preferFPS = 30;

    // for thread pool
//    private static final int CORE_POOL_SIZE = 1;        // initial/minimum threads
//    private static final int MAX_POOL_SIZE = 4;            // maximum threads
//    private static final int KEEP_ALIVE_TIME = 10;        // time periods while keep the idle thread
//    protected static final ThreadPoolExecutor EXECUTER
//            = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME,
//            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    private Context mAppContext;
    private SurfaceTexture mPreviewTexture;

    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;
    private UVCCamera mUVCCamera;
    private boolean isUSBRegistered = false;
    private Surface mSurface;
    private USBMonitor.UsbControlBlock mUsbControlBlock;

    private UVCFrameCallback mUVCFrameCallback;

    public static UVCCameraEnumerator instance(Context appContext, SurfaceTexture previewTexture) {
        if (sUVCCamera == null) {
            synchronized (UVCCameraEnumerator.class) {
                if (sUVCCamera == null) {
                    sUVCCamera = new UVCCameraEnumerator(appContext, previewTexture);
                }
            }
        }
        return sUVCCamera;
    }

    public UVCCameraEnumerator(Context appContext, SurfaceTexture previewTexture) {
        mAppContext = appContext;
        mPreviewTexture = previewTexture;
        mUSBMonitor = new USBMonitor(mAppContext, mOnDeviceConnectListener);
        if (!isUSBRegistered) {
            mUSBMonitor.register();
            isUSBRegistered = true;
        }

        LifeCycleMgr.registerLifeCycle(this);
    }

    @Override
    public void toResume() {
        if (!isUSBRegistered) {
            mUSBMonitor.register();
            isUSBRegistered = true;
        }
    }

    @Override
    public void toStop() {
        if (isUSBRegistered){
            mUSBMonitor.unregister();
            isUSBRegistered = false;
        }

    }

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(UsbDevice device) {
            if (DEBUG) Log.d(TAG, " Got a device: " + device.getDeviceName());
            mUSBMonitor.requestPermission(device);
        }

        @Override
        public void onDetach(UsbDevice device) {

        }

        @Override
        public void onConnect(UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
            if (DEBUG) Log.d(TAG, "One Usb connect: " + device
                    + "\n    Is New: " + createNew);
            if (mUVCCamera != null) {
                mUVCCamera.destroy();
            }

//            if (createNew)
            {
//                try {
//                    mUsbControlBlock = ctrlBlock.clone();
//                } catch (CloneNotSupportedException e) {
//                    e.printStackTrace();
//                }
                mUVCCamera = UVCCamera.instance();
                mUVCCamera.open(ctrlBlock);

                try {
                    mUVCCamera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.FRAME_FORMAT_YUYV);
                } catch (final IllegalArgumentException e) {
                    if (DEBUG) Log.e(TAG, "UVCCamera failed to set preview size");
                    // fallback to YUV mode
                    try {
                        mUVCCamera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.DEFAULT_PREVIEW_MODE);
                    } catch (final IllegalArgumentException e1) {
                        mUVCCamera.destroy();
                        mUVCCamera = null;
                    }
                }
            }



    /*        EXECUTER.execute(new Runnable() {
                @Override
                public void run() {
                    mUVCCamera.open(ctrlBlock);

                    if (mSurface != null) {
                        mSurface.release();
                        mSurface = null;
                    }
                }
            });*/

        }

        @Override
        public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
            if (DEBUG) Log.d(TAG, "Usb disconnected!!!");
            if (mUVCCamera != null) {
                mUVCCamera.close();
                if (mSurface != null) {
                    mSurface.release();
                    mSurface = null;
                }
            }
        }

        @Override
        public void onCancel(UsbDevice device) {

        }
    };

    private boolean isFirstFrame = true;
    private byte[] frameData;
    private int frameSize;
    private final IFrameCallback mIFrameCallback = new IFrameCallback() {
        @Override
        public void onFrame(ByteBuffer frame) {
            if (mUVCFrameCallback == null) return;

            if (isFirstFrame) {
                frameSize = frame.remaining();
                frameData = new byte[frameSize];
                isFirstFrame = false;
            }

            frame.get(frameData);
            mUVCFrameCallback.onUVCFrame(frameData);
        }
    };

    @Override
    public void setUVCCameraFrameCallback(UVCFrameCallback frameCallback) {
        if (frameCallback == null) {
            this.mUVCFrameCallback = null;
            mUVCCamera.setFrameCallback(null, UVCCamera.PIXEL_FORMAT_YUV420SP);     // PIXEL_FORMAT_NV21
            return;
        }

        this.mUVCFrameCallback = frameCallback;

        // set frame fromat to YUV420SP, because anyrtc use YUV420SP default.
        // anyrtc's comment say it use NV21, but when set camera pixel format to NV21
        // the video is black and white, but when set camera pixel format to YUV420SP
        // the video is colorful. so anyrtc need YUV420SP pixel format
        mUVCCamera.setFrameCallback(mIFrameCallback, UVCCamera.PIXEL_FORMAT_YUV420SP);  // PIXEL_FORMAT_YUV420SP
    }

    @Override
    public void setUVCPreviewTexture(SurfaceTexture texture) {
        if (DEBUG) Log.d(TAG, "UVCCamera setUVCPreviewTexture: " + texture);
        this.mPreviewTexture = texture;
        this.mSurface = new Surface(this.mPreviewTexture);
        mUVCCamera.setPreviewDisplay(this.mSurface);
    }

    @Override
    public void setUVCPreviewSizeRate(int width, int height, int minFps, int maxFps) {
        if (mUVCCamera == null) {
            if (DEBUG) Log.e(TAG, "UVCCamera is null when set uvc preview size and rate!");
            return;
        } else {
            if (DEBUG) Log.d(TAG, "set UVC Preview Size and Rate");
        }

        List<Size>  supportedSize = mUVCCamera.getSupportedSizeList();
        if (DEBUG) {
            Log.d(TAG, "UVCCamera Supported Size List:\n" +
                "    " + supportedSize);
        }

        mUVCCamera.setPreviewSize(width, height, minFps, maxFps,
                UVCCamera.FRAME_FORMAT_YUYV, UVCCamera.DEFAULT_BANDWIDTH);
    }

    @Override
    public void startUVCPreview() {
        if (DEBUG) Log.d(TAG, "start UVC Camera Preview");
        mUVCCamera.startPreview();
    }

    @Override
    public void stopUVCPreview() {

    }

    @Override
    public List<CameraEnumerationAndroid.CaptureFormat> getSupportedFormats() {

        final List<CameraEnumerationAndroid.CaptureFormat> formatList = new ArrayList<>();

        if (mUVCCamera == null) return formatList;

        int minFps = UVCCamera.DEFAULT_PREVIEW_MIN_FPS;
        int maxFps = UVCCamera.DEFAULT_PREVIEW_MAX_FPS;

        List<Size> uvcSizes = mUVCCamera.getSupportedSizeList();

        for (Size uvcSize : uvcSizes) {
            formatList.add(new CameraEnumerationAndroid.CaptureFormat(uvcSize.width,
                    uvcSize.height, minFps, maxFps));
        }

        return formatList;
    }

    public UVCVideoRecorder getVideoRecorder(){
        if (sVideoRecorder == null) {
            synchronized (UVCCameraEnumerator.class) {
                if (sVideoRecorder == null) {
                    sVideoRecorder = new UVCVideoRecorder();
                }
            }
        }
        return sVideoRecorder;
    }

    public class UVCVideoRecorder {
        String mVideoStorePath;
        boolean isRecording = false;
        MediaRecorder mMediaRecorder;
        MediaCodecVideoEncoder mVideoEncoder;
//        MediaCodec
//        MediaCodecVideoDecoder

        public UVCVideoRecorder() {
            mMediaRecorder = new MediaRecorder();
            mVideoEncoder = new MediaCodecVideoEncoder();
        }

        public void setVideoStorePath(String path) {
            mVideoStorePath = path;
        }

        public void startRecord() {
            if (mSurface == null) {
                return;
            }

        }

    }

}
